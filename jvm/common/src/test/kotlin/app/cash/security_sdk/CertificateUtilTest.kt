package app.cash.security_sdk

import app.cash.security_sdk.internal.S2DKContentVerifierProvider
import app.cash.security_sdk.internal.toSubjectPublicKeyInfo
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.signature.SignatureConfig
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

  @BeforeEach
  fun setUp() {
    SignatureConfig.register()
    ed25519PrivateKeysetHandle = KeysetHandle.generateNew(KeyTemplates.get("ED25519"))
    p256PrivateKeysetHandle = KeysetHandle.generateNew(KeyTemplates.get("ECDSA_P256"))
  }

  @Test
  fun `test createRootSigningCertificate validity period`() {
    val cert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), ed25519PrivateKeysetHandle
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
      "entity", Period.ofDays(1), ed25519PrivateKeysetHandle
    )

    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(cert.certificate.toByteArray())
    assertEquals("CN=entity", certHolder.subject.toString())
  }

  @Test
  fun `test createRootSigningCertificate certificate issuer`() {
    val cert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), ed25519PrivateKeysetHandle
    )

    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(cert.certificate.toByteArray())
    assertEquals("CN=entity", certHolder.issuer.toString())
  }

  @Test
  fun `test createRootSigningCertificate signing`() {
    // Create root signing cert using our keyset.  Root cert is self-signed.
    val signingCert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), ed25519PrivateKeysetHandle
    )

    // Extract the x.509 certificate from our object.
    val certHolder = X509CertificateHolder(signingCert.certificate.toByteArray())

    // Self-signed cert should verify when presented with itself.
    assertTrue(certHolder.isSignatureValid(S2DKContentVerifierProvider(certHolder.subjectPublicKeyInfo)))

    // Different key should not verify
    val otherCert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), KeysetHandle.generateNew(KeyTemplates.get("ED25519"))
    )
    assertFalse(
      certHolder.isSignatureValid(
        S2DKContentVerifierProvider(
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
      CertificateUtil.createMobileCertRequest("entity", p256PrivateKeysetHandle)

    assertEquals(0u, mobileCertRequest.version!!.toUInt())
  }

  @Test
  fun `test createMobileCertRequest entity`() {
    // Create mobile signing certificate, which represents the request which would come from a
    // mobile client.
    val mobileCertRequest =
      CertificateUtil.createMobileCertRequest("entity", p256PrivateKeysetHandle)

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
      CertificateUtil.createMobileCertRequest("entity", p256PrivateKeysetHandle)

    val pkcs10CertificateRequest =
      PKCS10CertificationRequest(mobileCertRequest.pkcs10_request!!.toByteArray())

    // Make sure that the resulting pkcs10 request is signed by the appropriate key.
    assertTrue(
      pkcs10CertificateRequest.isSignatureValid(
        S2DKContentVerifierProvider(
          p256PrivateKeysetHandle.toSubjectPublicKeyInfo()
        )
      )
    )

    // While we're here, make sure that any other keysets don't verify.
    assertFalse(
      pkcs10CertificateRequest.isSignatureValid(
        S2DKContentVerifierProvider(
          ed25519PrivateKeysetHandle.toSubjectPublicKeyInfo()
        )
      )
    )
  }

  @Test
  fun `test signCertificate validity period`() {
    val certHolder = X509CertificateHolder(createTestMobileSignedCert().certificate.toByteArray())
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
    val certHolder = X509CertificateHolder(createTestMobileSignedCert().certificate.toByteArray())
    assertEquals("CN=entity", certHolder.subject.toString())
  }

  @Test
  fun `test signCertificate certificate issuer`() {
    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(createTestMobileSignedCert().certificate.toByteArray())
    assertEquals("CN=issuingEntity", certHolder.issuer.toString())
  }

  @Test
  fun `test signCertificate signing`() {
    // Create local copy of issuingCert for use in verifying signature.
    val issuingCert = CertificateUtil.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1), ed25519PrivateKeysetHandle
    )

    // Extract the x.509 certificate from our object.
    val certHolder = X509CertificateHolder(createTestMobileSignedCert().certificate.toByteArray())
    val issuingCertHolder = X509CertificateHolder(issuingCert.certificate.toByteArray())

    // Cert should verify when presented with issuing cert
    assertTrue(certHolder.isSignatureValid(S2DKContentVerifierProvider(issuingCertHolder.subjectPublicKeyInfo)))

    // Different key should not verify (in this case it is not self signed)
    assertFalse(
      certHolder.isSignatureValid(
        S2DKContentVerifierProvider(
          certHolder.subjectPublicKeyInfo
        )
      )
    )
  }

  private fun createTestMobileSignedCert(): SigningCert {
    val issuingCert = CertificateUtil.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1), ed25519PrivateKeysetHandle
    )

    val certRequest = CertificateUtil.mobileCertRequestToCertRequest(
      CertificateUtil.createMobileCertRequest(
        "entity",
        p256PrivateKeysetHandle
      ).encodeByteString()
    )

    return CertificateUtil.signCertificate(ed25519PrivateKeysetHandle, issuingCert, certRequest)
  }
}
