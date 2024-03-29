package app.cash.trifle

import app.cash.trifle.TrifleErrors.InvalidSignature
import app.cash.trifle.TrifleErrors.NoTrustAnchor
import app.cash.trifle.protos.api.alpha.MobileCertificateRequest
import app.cash.trifle.providers.jca.JCAContentVerifierProvider
import app.cash.trifle.testing.TestCertificateAuthority
import okio.ByteString.Companion.encodeUtf8
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import kotlin.test.assertContains
import kotlin.test.assertContentEquals

internal class TrifleTest {
  @Nested
  @DisplayName("Certificate Authority#createRootSigningCertificate() Tests")
  inner class CreateRootSigningCertificateTests {
    private val certHolder = X509CertificateHolder(
      certificateAuthority.rootCertificate.certificate
    )

    @Test
    fun `test validity period`() {
      val duration =
        Duration.ofMillis(
          certHolder.notAfter.toInstant()
            .minusMillis(certHolder.notBefore.toInstant().toEpochMilli())
            .toEpochMilli()
        )
      assertEquals(1, duration.toDays())
    }

    @Test
    fun `test certificate entity`() {
      assertEquals("CN=issuingEntity", certHolder.subject.toString())
    }

    @Test
    fun `test certificate issuer`() {
      assertEquals("CN=issuingEntity", certHolder.issuer.toString())
    }

    @Test
    fun `test isSignatureValid() returns true for certificate signature`() {
      // Self-signed cert should verify when presented with itself.
      assertTrue(
        certHolder.isSignatureValid(
          JCAContentVerifierProvider(certHolder.subjectPublicKeyInfo)
        )
      )
    }

    @Test
    fun `test isSignatureValid() returns false due to a different issuer`() {
      // Different key should not verify
      assertFalse(
        certHolder.isSignatureValid(
          JCAContentVerifierProvider(
            X509CertificateHolder(
              TestCertificateAuthority()
                .rootCertificate.certificate
            ).subjectPublicKeyInfo
          )
        )
      )
    }
  }

  @Nested
  @DisplayName("Certificate Authority#signCertificate() Tests")
  inner class SignCertificateTests {
    private val issuingCertHolder = X509CertificateHolder(
      certificateAuthority.rootCertificate.certificate
    )
    // Extract the x.509 certificate from our object.
    private val certHolder = X509CertificateHolder(endEntity.certificate.certificate)

    @Test
    fun `test validity period`() {
      val certTTL: Long = 1
      val endEntity1 = certificateAuthority.createTestEndEntity("entity", Duration.ofDays(certTTL))
      val certHolder1 = X509CertificateHolder(endEntity1.certificate.certificate)

      val duration =
        Duration.ofMillis(
          certHolder1.notAfter.toInstant()
            .minusMillis(certHolder1.notBefore.toInstant().toEpochMilli())
            .toEpochMilli()
        )
      assertEquals(certTTL.toLong(), duration.toDays())
    }

    @Test
    fun `test default validity period`() {
      val duration =
        Duration.ofMillis(
          certHolder.notAfter.toInstant()
            .minusMillis(certHolder.notBefore.toInstant().toEpochMilli())
            .toEpochMilli()
        )
      assertEquals(30, duration.toDays())
    }

    @Test
    fun `test certificate entity`() {
      assertEquals("CN=entity", certHolder.subject.toString())
    }

    @Test
    fun `test certificate issuer`() {
      assertEquals("CN=issuingEntity", certHolder.issuer.toString())
    }

    @Test
    fun `test isSignatureValid() returns true for properly signed certificate`() {
      // Cert should verify when presented with issuing cert
      assertTrue(
        certHolder.isSignatureValid(
          JCAContentVerifierProvider(issuingCertHolder.subjectPublicKeyInfo)
        )
      )
    }

    @Test
    fun `test isSignatureValid() returns false due to incorrectly signed certificate`() {
      // Different key should not verify (in this case it is not self-signed)
      assertFalse(
        certHolder.isSignatureValid(JCAContentVerifierProvider(certHolder.subjectPublicKeyInfo))
      )
    }
  }

  @Nested
  @DisplayName("End Entity#createCertRequest() Tests")
  inner class CreateCertificateRequestTests {
    // Create mobile signing certificate, which represents the request which would come from an
    // end entity.
    private val certRequest = endEntity.certRequest
    private val mobileCertRequest = MobileCertificateRequest.ADAPTER.decode(
      certRequest.serialize()
    )

    @Test
    fun `test createMobileCertRequest version`() {
      assertEquals(0u, mobileCertRequest.version!!.toUInt())
    }

    @Test
    fun `test createCertRequest entity`() {
      check(certRequest is CertificateRequest.PKCS10Request)
      // Make sure the cert request has the appropriate entity name
      assertEquals(X500Name("CN=entity"), certRequest.pkcs10Req.subject)
    }

    @Test
    fun `test createCertRequest signing`() {
      // Make sure that the resulting pkcs10 request is signed by the appropriate key.
      assertTrue(certRequest.verify())
    }
  }

  @Nested
  @DisplayName("End Entity#createSignedData() Tests")
  inner class CreateSignedDataTests {
    private val rawData = "hello world".encodeUtf8().toByteArray()

    @Test
    fun `test createSignedData succeeds`() {
      assertEquals(endEntity.createSignedData(rawData).envelopedData.data, rawData)
    }

    @Test
    fun `test createSignedData succeeds with SignedData#toString properly redacted`() {
      val signedData = endEntity.createSignedData(rawData)

      assertTrue(signedData.toString().contains("[REDACTED]"))
      assertTrue(signedData.envelopedData.toString().contains("[REDACTED]"))

      assertFalse(signedData.toString().contains(rawData.toString()))
      assertFalse(signedData.envelopedData.toString().contains(rawData.toString()))
    }

    @Test
    fun `test createSignedData succeeds with SignedData#toPlaintextString not redacted`() {
      val signedData = endEntity.createSignedData(rawData)

      assertTrue(signedData.toPlaintextString().contains(rawData.toString()))
      assertTrue(signedData.envelopedData.toPlaintextString().contains(rawData.toString()))

      assertFalse(signedData.toPlaintextString().contains("[REDACTED]"))
      assertFalse(signedData.envelopedData.toPlaintextString().contains("[REDACTED]"))
    }

    @Test
    fun `test createSignedData fails with InvalidSignature due to signature not matching on verify`() {
      assertThrows<InvalidSignature> {
        endEntity.createSignedData(rawData, certificateAuthority.createTestEndEntity().certChain)
      }
    }

    @Test
    fun `test createSignedData fails with NoTrustAnchor due to invalid chain on verify`() {
      val otherCertificateAuthority = TestCertificateAuthority("otherIssuingEntity")
        .createTestEndEntity()
      val invalidCertChain = listOf(endEntity.certificate) +
        otherCertificateAuthority.certChain.drop(1)
      assertThrows<NoTrustAnchor> {
        endEntity.createSignedData(
          rawData,
          invalidCertChain
        )
      }
    }
  }

  @Nested
  @DisplayName("SignedData#verify() Tests")
  inner class SignedDataTests {
    private val signedData = endEntity.createSignedData(
      "hello world".encodeUtf8().toByteArray()
    )

    @Test
    fun `test verifies signed data`() {
      assertTrue(signedData.verify(certificateAuthority.rootCertificate).isSuccess)
    }

    @Test
    fun `test verifies signed data fails with wrong anchor`() {
      val result = signedData.verify(TestCertificateAuthority().rootCertificate)
      assertTrue(result.isFailure)
      assertTrue(result.exceptionOrNull() is NoTrustAnchor)
    }

    @Test
    fun `test verifies and extracts appropriate data`() {
      val expected = VerifiedData(signedData.envelopedData.data, signedData.certificates)
      assertEquals(
        expected,
        signedData.verifyAndExtract(certificateAuthority.rootCertificate).getOrNull()
      )
    }

    @Test
    fun `test no data extracted with bad verification`() {
      val result = signedData.verifyAndExtract(TestCertificateAuthority().rootCertificate)
      assertTrue(result.isFailure)
      assertTrue(result.exceptionOrNull() is NoTrustAnchor)
    }
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority("issuingEntity")
    private val endEntity = certificateAuthority.createTestEndEntity("entity")
  }
}
