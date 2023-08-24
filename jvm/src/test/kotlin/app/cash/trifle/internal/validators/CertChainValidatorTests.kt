package app.cash.trifle.internal.validators

import app.cash.trifle.TrifleErrors.InvalidCertPath
import app.cash.trifle.TrifleErrors.NoTrustAnchor
import app.cash.trifle.testing.TestCertificateAuthority
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CertChainValidatorTests {
  @Nested
  @DisplayName("X509 Certificate Chain Validator Tests")
  inner class X509CertChainValidatorTests {
    @Test
    fun `test validate() succeeds for valid certificate chain`() {
      assertTrue(validator.validate(endEntity.certChain).isSuccess)
    }

    @Test
    fun `test validate() fails with NoTrustAnchor for invalid certificate chain`() {
      val result = validator.validate(TestCertificateAuthority().createTestEndEntity().certChain)
      assertTrue(result.isFailure)
      assertTrue(result.exceptionOrNull() is NoTrustAnchor)
    }

    @Test
    fun `test validate() fails with InvalidCertPath for empty certificate chain`() {
      val result = validator.validate(emptyList())
      assertTrue(result.isFailure)
      assertTrue(result.exceptionOrNull() is InvalidCertPath)
    }

    @Test
    fun `test validate() fails with InvalidCertPath if certificate chain only contains root certificate`() {
      val result = validator.validate(listOf(certificateAuthority.rootCertificate))
      assertTrue(result.isFailure)
      assertTrue(result.exceptionOrNull() is InvalidCertPath)
    }
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority()
    private val endEntity = certificateAuthority.createTestEndEntity()
    private val validator = X509CertChainValidator(certificateAuthority.rootCertificate)
  }
}
