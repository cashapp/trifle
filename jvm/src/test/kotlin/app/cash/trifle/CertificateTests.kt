package app.cash.trifle

import app.cash.trifle.TrifleErrors.*
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
    fun `test verify() succeeds for a properly issued certificate`() {
      val result = endEntity.certificate.verify(
        endEntity.certRequest,
        endEntity.certChain.drop(1),
        certificateAuthority.rootCertificate
      )
      assertTrue(result.isSuccess)
    }

    @Test
    fun `test verify() fails with CSRMismatch for a legitimate certificate from the same CA, but wrong entity`() {
      val result = endEntity.certificate.verify(
        otherEndEntity.certRequest,
        endEntity.certChain.drop(1),
        certificateAuthority.rootCertificate
      )
      assertTrue(result.isFailure)
      assertTrue(result.exceptionOrNull() is CSRMismatch)
    }

    @Test
    fun `test verify() fails with NoTrustAnchor for a different root certificate`() {
      val result = endEntity.certificate.verify(
        endEntity.certRequest,
        otherEndEntity.certChain.drop(1),
        otherCertificateAuthority.rootCertificate
      )
      assertTrue(result.isFailure)
      assertTrue(result.exceptionOrNull() is NoTrustAnchor)
    }

    @Test
    fun `test verify() fails with ExpiredCertificate for an expired certificate`() {
      val result = endEntity.certificate.verify(
        endEntity.certRequest,
        endEntity.certChain.drop(1),
        certificateAuthority.rootCertificate,
        Date.from(Instant.now().plus(Duration.ofDays(365)))
      )
      assertTrue(result.isFailure)
      assertTrue(result.exceptionOrNull() is ExpiredCertificate)
    }
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority("issuingEntity")
    private val otherCertificateAuthority = TestCertificateAuthority("otherIssuingEntity")
    private val endEntity = certificateAuthority.createTestEndEntity("entity")
    private val otherEndEntity = otherCertificateAuthority.createTestEndEntity("otherEntity")
  }
}
