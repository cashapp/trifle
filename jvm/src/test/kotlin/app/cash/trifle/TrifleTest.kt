package app.cash.trifle

import app.cash.trifle.delegate.TinkDelegate
import app.cash.trifle.internal.providers.BCContentVerifierProvider
import app.cash.trifle.internal.util.TestFixtures
import app.cash.trifle.internal.util.TestFixtures.RAW_ECDSA_P256_KEY_TEMPLATE
import app.cash.trifle.protos.api.alpha.MobileCertificateRequest
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.signature.SignatureConfig
import okio.ByteString.Companion.toByteString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.CertException
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Period

internal class TrifleTest {
  private lateinit var certificateAuthority: Trifle.CertificateAuthority
  private lateinit var mobileClient: Trifle.EndEntity

  @BeforeEach
  fun setUp() {
    SignatureConfig.register()

    certificateAuthority = Trifle.CertificateAuthority(
      KeysetHandle.generateNew(KeyTemplates.get("ED25519WithRawOutput"))
    )
    mobileClient = Trifle.EndEntity(
      KeysetHandle.generateNew(RAW_ECDSA_P256_KEY_TEMPLATE)
    )
  }

  @Test
  fun `test createRootSigningCertificate validity period`() {
    val cert = certificateAuthority.createRootSigningCertificate(
      "entity", Period.ofDays(1)
    )

    val certHolder = X509CertificateHolder(cert.certificate)
    val duration =
      Duration.ofMillis(
        certHolder.notAfter.toInstant().minusMillis(certHolder.notBefore.toInstant().toEpochMilli())
          .toEpochMilli()
      )
    assertEquals(1, duration.toDays())
  }

  @Test
  fun `test createRootSigningCertificate certificate entity`() {
    val cert = certificateAuthority.createRootSigningCertificate(
      "entity", Period.ofDays(1)
    )

    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(cert.certificate)
    assertEquals("CN=entity", certHolder.subject.toString())
  }

  @Test
  fun `test createRootSigningCertificate certificate issuer`() {
    val cert = certificateAuthority.createRootSigningCertificate(
      "entity", Period.ofDays(1)
    )

    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(cert.certificate)
    assertEquals("CN=entity", certHolder.issuer.toString())
  }

  @Test
  fun `test createRootSigningCertificate signing`() {
    // Create root signing cert using our keyset.  Root cert is self-signed.
    val signingCert = certificateAuthority.createRootSigningCertificate(
      "entity", Period.ofDays(1)
    )

    // Extract the x.509 certificate from our object.
    val certHolder = X509CertificateHolder(signingCert.certificate)

    // Self-signed cert should verify when presented with itself.
    assertTrue(
      certHolder.isSignatureValid(
        BCContentVerifierProvider(certHolder.subjectPublicKeyInfo)
      )
    )

    // Different key should not verify
    val otherCertificateAuthority = Trifle.CertificateAuthority(
      TinkDelegate(
        KeysetHandle.generateNew(KeyTemplates.get("ED25519WithRawOutput"))
      )
    )
    val otherCert = otherCertificateAuthority.createRootSigningCertificate(
      "entity", Period.ofDays(1),
    )
    assertFalse(
      certHolder.isSignatureValid(
        BCContentVerifierProvider(
          X509CertificateHolder(
            otherCert.certificate
          ).subjectPublicKeyInfo
        )
      )
    )
  }

  @Test
  fun `test createMobileCertRequest version`() {
    // Create mobile signing certificate, which represents the request which would come from a
    // mobile client.
    val certRequest = mobileClient.createCertRequest("entity")
    val mobileCertRequest = MobileCertificateRequest.ADAPTER.decode(certRequest.serialize())

    assertEquals(0u, mobileCertRequest.version!!.toUInt())
  }

  @Test
  fun `test createCertRequest entity`() {
    // Create mobile signing certificate, which represents the request which would come from a
    // mobile client.
    val certRequest = mobileClient.createCertRequest("entity")

    check(certRequest is CertificateRequest.PKCS10Request)
    // Make sure the cert request has the appropriate entity name
    assertEquals(X500Name("CN=entity"), certRequest.pkcs10Req.subject)
  }

  @Test
  fun `test createCertRequest signing`() {
    // Create mobile signing certificate, which represents the request which would come from a
    // mobile client.
    val certRequest = mobileClient.createCertRequest("entity")

    // Make sure that the resulting pkcs10 request is signed by the appropriate key.
    assertTrue(
      certRequest.verify()
    )
  }

  @Test
  fun `test signCertificate validity period`() {
    // Create local copy of issuingCert for use in verifying signature.
    val issuingCert = certificateAuthority.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1)
    )

    val certHolder =
      X509CertificateHolder(createTestMobileSignedCert(issuingCert).certificate)
    val duration =
      Duration.ofMillis(
        certHolder.notAfter.toInstant().minusMillis(certHolder.notBefore.toInstant().toEpochMilli())
          .toEpochMilli()
      )
    assertEquals(180, duration.toDays())
  }

  @Test
  fun `test signCertificate certificate entity`() {
    // Create local copy of issuingCert for use in verifying signature.
    val issuingCert = certificateAuthority.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1)
    )

    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(
      createTestMobileSignedCert(issuingCert).certificate
    )
    assertEquals("CN=entity", certHolder.subject.toString())
  }

  @Test
  fun `test signCertificate certificate issuer`() {
    // Create local copy of issuingCert for use in verifying signature.
    val issuingCert = certificateAuthority.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1)
    )

    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(
      createTestMobileSignedCert(issuingCert).certificate
    )
    assertEquals("CN=issuingEntity", certHolder.issuer.toString())
  }

  @Test
  fun `test signCertificate signing`() {
    // Create local copy of issuingCert for use in verifying signature.
    val issuingCert = certificateAuthority.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1)
    )

    // Extract the x.509 certificate from our object.
    val certHolder = X509CertificateHolder(createTestMobileSignedCert(issuingCert).certificate)
    val issuingCertHolder = X509CertificateHolder(issuingCert.certificate)

    // Cert should verify when presented with issuing cert
    assertTrue(
      certHolder.isSignatureValid(
        BCContentVerifierProvider(issuingCertHolder.subjectPublicKeyInfo)
      )
    )

    // Different key should not verify (in this case it is not self-signed)
    assertThrows<CertException> {
      certHolder.isSignatureValid(
        BCContentVerifierProvider(certHolder.subjectPublicKeyInfo)
      )
    }
  }

  @Test
  fun `test signCertificate signing from CSR with raw p256 public key`() {
    // Create local copy of issuingCert for use in verifying signature.
    val issuingCert = certificateAuthority.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1)
    )

    // Extract the x.509 certificate from our object.
    val certHolder = X509CertificateHolder(createTestMobileSignedCert(issuingCert) {
      CertificateRequest.deserialize(
        MobileCertificateRequest(
          0,
          TestFixtures.PKCS10Request.toByteString()
        ).encode()
      )
    }.certificate)
    val issuingCertHolder = X509CertificateHolder(issuingCert.certificate)

    // Cert should verify when presented with issuing cert
    assertTrue(
      certHolder.isSignatureValid(
        BCContentVerifierProvider(issuingCertHolder.subjectPublicKeyInfo)
      )
    )
  }

  private fun createTestMobileSignedCert(
    issuingCert: Certificate,
    csrProvider: (() -> CertificateRequest)? = null
  ): Certificate {
    val certRequest = if (csrProvider == null) {
      mobileClient.createCertRequest("entity")
    } else {
      csrProvider()
    }

    return certificateAuthority.signCertificate(issuingCert, certRequest)
  }
}
