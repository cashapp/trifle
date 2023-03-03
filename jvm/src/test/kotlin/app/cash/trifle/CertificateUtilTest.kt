package app.cash.trifle

import app.cash.trifle.internal.TrifleAlgorithmIdentifier.Ed25519AlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.P256v1AlgorithmIdentifier
import app.cash.trifle.internal.providers.TinkContentVerifierProvider
import app.cash.trifle.internal.signers.TinkContentSigner
import app.cash.trifle.internal.util.TestFixtures
import app.cash.trifle.internal.util.toSubjectPublicKeyInfo
import app.cash.trifle.protos.api.alpha.MobileCertificateRequest
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.signature.SignatureConfig
import okio.ByteString.Companion.toByteString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Period

internal class CertificateUtilTest {
  private lateinit var ed25519PrivateKeysetHandle: KeysetHandle
  private lateinit var p256PrivateKeysetHandle: KeysetHandle
  private lateinit var ed25519ContentSigner: TinkContentSigner
  private lateinit var p256ContentSigner: TinkContentSigner

  @BeforeEach
  fun setUp() {
    SignatureConfig.register()
    ed25519PrivateKeysetHandle = KeysetHandle.generateNew(KeyTemplates.get("ED25519"))
    p256PrivateKeysetHandle = KeysetHandle.generateNew(KeyTemplates.get("ECDSA_P256"))

    ed25519ContentSigner = TinkContentSigner(ed25519PrivateKeysetHandle)
    p256ContentSigner = TinkContentSigner(p256PrivateKeysetHandle)
  }

  @Test
  fun `test createRootSigningCertificate validity period`() {
    val cert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), ed25519ContentSigner
    )

    val certHolder = X509CertificateHolder(cert.certificate.toByteArray())
    val duration =
      Duration.ofMillis(
        certHolder.notAfter.toInstant().minusMillis(certHolder.notBefore.toInstant().toEpochMilli())
          .toEpochMilli()
      )
    assertEquals(1, duration.toDays())
  }

  @Test
  fun `test createRootSigningCertificate certificate entity`() {
    val cert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), ed25519ContentSigner
    )

    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(cert.certificate.toByteArray())
    assertEquals("CN=entity", certHolder.subject.toString())
  }

  @Test
  fun `test createRootSigningCertificate certificate issuer`() {
    val cert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), ed25519ContentSigner
    )

    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(cert.certificate.toByteArray())
    assertEquals("CN=entity", certHolder.issuer.toString())
  }

  @Test
  fun `test createRootSigningCertificate signing`() {
    // Create root signing cert using our keyset.  Root cert is self-signed.
    val signingCert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), ed25519ContentSigner
    )

    // Extract the x.509 certificate from our object.
    val certHolder = X509CertificateHolder(signingCert.certificate.toByteArray())

    // Self-signed cert should verify when presented with itself.
    assertTrue(
      certHolder.isSignatureValid(
        TinkContentVerifierProvider(certHolder.subjectPublicKeyInfo)
      )
    )

    // Different key should not verify
    val otherCert = CertificateUtil.createRootSigningCertificate(
      "entity",
      Period.ofDays(1),
      TinkContentSigner(KeysetHandle.generateNew(KeyTemplates.get("ED25519")))
    )
    assertFalse(
      certHolder.isSignatureValid(
        TinkContentVerifierProvider(
          X509CertificateHolder(
            otherCert.certificate.toByteArray()
          ).subjectPublicKeyInfo
        )
      )
    )
  }

  @Test
  fun `test createMobileCertRequest version`() {
    // Create mobile signing certificate, which represents the request which would come from a
    // mobile client.
    val mobileCertRequest =
      CertificateUtil.createMobileCertRequest("entity", p256ContentSigner)

    assertEquals(0u, mobileCertRequest.version!!.toUInt())
  }

  @Test
  fun `test createMobileCertRequest entity`() {
    // Create mobile signing certificate, which represents the request which would come from a
    // mobile client.
    val mobileCertRequest =
      CertificateUtil.createMobileCertRequest("entity", p256ContentSigner)

    val pkcs10CertificateRequest =
      PKCS10CertificationRequest(mobileCertRequest.pkcs10_request!!.toByteArray())

    // Make sure the cert request has the appropriate entity name
    assertEquals(X500Name("CN=entity"), pkcs10CertificateRequest.subject)
  }

  @Test
  fun `test createMobileCertRequest signing`() {
    // Create mobile signing certificate, which represents the request which would come from a
    // mobile client.
    val mobileCertRequest =
      CertificateUtil.createMobileCertRequest("entity", p256ContentSigner)

    val pkcs10CertificateRequest =
      PKCS10CertificationRequest(mobileCertRequest.pkcs10_request!!.toByteArray())

    // Make sure that the resulting pkcs10 request is signed by the appropriate key.
    assertTrue(
      pkcs10CertificateRequest.isSignatureValid(
        TinkContentVerifierProvider(
          p256PrivateKeysetHandle.toSubjectPublicKeyInfo(P256v1AlgorithmIdentifier)
        )
      )
    )

    // While we're here, make sure that any other keysets don't verify.
    assertFalse(
      pkcs10CertificateRequest.isSignatureValid(
        TinkContentVerifierProvider(
          ed25519PrivateKeysetHandle.toSubjectPublicKeyInfo(Ed25519AlgorithmIdentifier)
        )
      )
    )
  }

  @Test
  fun `test signCertificate validity period`() {
    val certHolder =
      X509CertificateHolder(createTestMobileSignedCert().certificate.toByteArray())
    val duration =
      Duration.ofMillis(
        certHolder.notAfter.toInstant().minusMillis(certHolder.notBefore.toInstant().toEpochMilli())
          .toEpochMilli()
      )
    assertEquals(180, duration.toDays())
  }

  @Test
  fun `test signCertificate certificate entity`() {
    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(
      createTestMobileSignedCert().certificate.toByteArray()
    )
    assertEquals("CN=entity", certHolder.subject.toString())
  }

  @Test
  fun `test signCertificate certificate issuer`() {
    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(
      createTestMobileSignedCert().certificate.toByteArray()
    )
    assertEquals("CN=issuingEntity", certHolder.issuer.toString())
  }

  @Test
  fun `test signCertificate signing`() {
    // Create local copy of issuingCert for use in verifying signature.
    val issuingCert = CertificateUtil.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1), ed25519ContentSigner
    )

    // Extract the x.509 certificate from our object.
    val certHolder = X509CertificateHolder(createTestMobileSignedCert().certificate.toByteArray())
    val issuingCertHolder = X509CertificateHolder(issuingCert.certificate.toByteArray())

    // Cert should verify when presented with issuing cert
    assertTrue(
      certHolder.isSignatureValid(
        TinkContentVerifierProvider(issuingCertHolder.subjectPublicKeyInfo)
      )
    )

    // Different key should not verify (in this case it is not self-signed)
    assertFalse(
      certHolder.isSignatureValid(
        TinkContentVerifierProvider(certHolder.subjectPublicKeyInfo)
      )
    )
  }

  @Test
  fun `test signCertificate signing from CSR with raw p256 public key`() {
    // Create local copy of issuingCert for use in verifying signature.
    val issuingCert = CertificateUtil.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1), ed25519ContentSigner
    )

    // Extract the x.509 certificate from our object.
    val certHolder = X509CertificateHolder(createTestMobileSignedCert {
      CertificateUtil.mobileCertRequestToCertRequest(
        MobileCertificateRequest(
          0,
          TestFixtures.PKCS10Request.toByteString()
        ).encodeByteString()
      )
    }.certificate.toByteArray())
    val issuingCertHolder = X509CertificateHolder(issuingCert.certificate.toByteArray())

    // Cert should verify when presented with issuing cert
    assertTrue(
      certHolder.isSignatureValid(
        TinkContentVerifierProvider(issuingCertHolder.subjectPublicKeyInfo)
      )
    )
  }

  private fun createTestMobileSignedCert(
    csrProvider: (() -> CertificateRequest)? = null
  ): SigningCert {
    val issuingCert = CertificateUtil.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1), ed25519ContentSigner
    )

    val certRequest = if (csrProvider == null) {
      CertificateUtil.mobileCertRequestToCertRequest(
        CertificateUtil.createMobileCertRequest(
          "entity",
          p256ContentSigner
        ).encodeByteString()
      )
    } else {
      csrProvider()
    }

    return CertificateUtil.signCertificate(issuingCert, certRequest, ed25519ContentSigner)
  }
}
