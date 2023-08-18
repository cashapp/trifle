package app.cash.trifle

import app.cash.trifle.internal.providers.JCAContentVerifierProvider
import app.cash.trifle.protos.api.alpha.MobileCertificateRequest
import app.cash.trifle.testing.TestCertificateAuthority
import okio.ByteString.Companion.encodeUtf8
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.Duration
import java.time.Period

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
      assertEquals(30, duration.toDays())
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
      val certTTL = 30
      val endEntity30 = certificateAuthority.createTestEndEntity("entity", Period.ofDays(certTTL))
      val certHolder30 = X509CertificateHolder(endEntity30.certificate.certificate)

      val duration =
        Duration.ofMillis(
          certHolder30.notAfter.toInstant()
            .minusMillis(certHolder30.notBefore.toInstant().toEpochMilli())
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
      assertEquals(1, duration.toDays())
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
    fun `test createSignedData fails`() {
      assertThrows<IllegalStateException> {
        endEntity.createSignedData(rawData, certificateAuthority.createTestEndEntity().certChain)
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
      assertTrue(signedData.verify(certificateAuthority.rootCertificate))
    }

    @Test
    fun `test verifies signed data fails with wrong anchor`() {
      assertFalse(signedData.verify(TestCertificateAuthority().rootCertificate))
    }

    @Test
    fun `test verifies and extracts appropriate data`() {
      val expected = VerifiedData(signedData.envelopedData.data, signedData.certificates)
      assertEquals(expected, signedData.verifyAndExtract(certificateAuthority.rootCertificate))
    }

    @Test
    fun `test no data extracted with bad verification`() {
      assertNull(signedData.verifyAndExtract(TestCertificateAuthority().rootCertificate))
    }
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority("issuingEntity")
    private val endEntity = certificateAuthority.createTestEndEntity("entity")
  }
}
