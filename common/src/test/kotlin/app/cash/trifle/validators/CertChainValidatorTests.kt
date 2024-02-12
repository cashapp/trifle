package app.cash.trifle.validators

import app.cash.trifle.TrifleErrors.NotValidYetCertificate
import app.cash.trifle.TrifleErrors.ExpiredCertificate
import app.cash.trifle.TrifleErrors.InvalidCertPath
import app.cash.trifle.TrifleErrors.NoTrustAnchor
import app.cash.trifle.testing.TestCertificateAuthority
import app.cash.trifle.validators.CertChainValidator.X509CertChainValidator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.Date

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

    @Test
    fun `test validate() fails with NotValidYetCertificate`() {
      val validator = X509CertChainValidator(
        certificateAuthority.rootCertificate,
        Date.from(Instant.now().minus(Duration.ofDays(1)))
      )
      val result = validator.validate(endEntity.certChain)
      assertTrue(result.exceptionOrNull() is NotValidYetCertificate)
    }

    @Test
    fun `test validate() fails with ExpiredCertificate`() {
      val validator = X509CertChainValidator(
        certificateAuthority.rootCertificate,
        Date.from(Instant.now().plus(Duration.ofDays(2)))
      )
      val result = validator.validate(endEntity.certChain)
      assertTrue(result.exceptionOrNull() is ExpiredCertificate)
    }
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority()
    private val endEntity = certificateAuthority.createTestEndEntity(
      validity = Duration.ofDays(1)
    )
    private val validator = X509CertChainValidator(certificateAuthority.rootCertificate)
  }
}
