package app.cash.trifle

import app.cash.trifle.testing.TestCertificateAuthority
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.security.*
import java.time.Duration
import java.time.Instant
import java.util.Date

internal class CertificateTests {
  @Nested
  @DisplayName("Certificate#verify() Tests")
  inner class CertificateVerifyTests {
    @Test
    fun `test verify() returns true for a properly issued certificate`() {
      assertDoesNotThrow {
        endEntity.certificate.verify(
          endEntity.certRequest,
          endEntity.certChain.drop(1),
          certificateAuthority.rootCertificate
        )
      }
    }

    @Test
    fun `test verify() fails for a legitimate certificate from the same CA, but wrong entity`() {
      assertThrows<CSRMismatchException> {
        endEntity.certificate.verify(
          otherEndEntity.certRequest,
          endEntity.certChain.drop(1),
          certificateAuthority.rootCertificate
        )
      }
    }

    @Test
    fun `test verify() fails for a different root certificate`() {
      assertThrows<NoTrustAnchorException> {
        endEntity.certificate.verify(
          endEntity.certRequest,
          otherEndEntity.certChain.drop(1),
          otherCertificateAuthority.rootCertificate
        )
      }
    }

    @Test
    fun `test verify() fails for an expired certificate`() {
      assertThrows<ExpiredCertificateException> {
        endEntity.certificate.verify(
          endEntity.certRequest,
          endEntity.certChain.drop(1),
          certificateAuthority.rootCertificate,
          Date.from(Instant.now().plus(Duration.ofDays(365)))
          )
      }
    }
  }
  companion object {
    private val certificateAuthority = TestCertificateAuthority("issuingEntity")
    private val otherCertificateAuthority = TestCertificateAuthority("otherIssuingEntity")
    private val endEntity = certificateAuthority.createTestEndEntity("entity")
    private val otherEndEntity = otherCertificateAuthority.createTestEndEntity("otherEntity")
  }
}
