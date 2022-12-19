package app.cash.security_sdk

import app.cash.security_sdk.internal.S2DKContentVerifierProvider
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.signature.SignatureConfig
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Period

internal class CertificateUtilTest {
  private lateinit var ed25519PrivateKeysetHandle: KeysetHandle

  @BeforeEach
  fun setUp() {
    SignatureConfig.register()
    ed25519PrivateKeysetHandle = KeysetHandle.generateNew(KeyTemplates.get("ED25519"))
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
    assertEquals("CN=entity", certHolder.issuer.toString())
  }

  @Test
  fun `test createRootSigningCertificate certificate issuer`() {
    val cert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), ed25519PrivateKeysetHandle
    )

    // Extract cert and then entity value.
    val certHolder = X509CertificateHolder(cert.certificate.toByteArray())
    assertEquals("CN=entity", certHolder.subject.toString())
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
}
