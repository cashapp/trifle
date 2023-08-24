package app.cash.trifle

import app.cash.trifle.TrifleErrors.*
import app.cash.trifle.testing.TestCertificateAuthority
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.security.*

internal class CertificateTests {
  @Nested
  @DisplayName("Certificate#verify() Tests")
  inner class CertificateVerifyTests {
    @Test
    fun `test verify() succeeds for a properly issued certificate`() {
      val result = endEntity.certificate.verify(endEntity.certRequest)
      assertTrue(result.isSuccess)
    }

    @Test
    fun `test verify() fails with CSRMismatch for a legitimate certificate from the same CA, but wrong entity`() {
      val result = endEntity.certificate.verify(otherEndEntity.certRequest)
      assertTrue(result.isFailure)
      assertTrue(result.exceptionOrNull() is CSRMismatch)
    }
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority("issuingEntity")
    private val otherCertificateAuthority = TestCertificateAuthority("otherIssuingEntity")
    private val endEntity = certificateAuthority.createTestEndEntity("entity")
    private val otherEndEntity = otherCertificateAuthority.createTestEndEntity("otherEntity")
  }
}
