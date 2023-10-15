package app.cash.trifle.validators

import app.cash.trifle.TrifleErrors.CSRMismatch
import app.cash.trifle.TrifleErrors.ExpiredCertificate
import app.cash.trifle.TrifleErrors.NotValidYetCertificate
import app.cash.trifle.testing.TestCertificateAuthority
import app.cash.trifle.validators.CertificateValidator.X509CertificateValidator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.Date

internal class CertificateValidatorTests {
  @Nested
  @DisplayName("X509 Certificate Validator Tests")
  inner class X509CertChainValidatorTests {
    @Test
    fun `test validate() succeeds for valid certificate`() {
      Assertions.assertTrue(validator.validate(null).isSuccess)
    }

    @Test
    fun `test validate() succeeds for matching attributes from certificate request`() {
      Assertions.assertTrue(validator.validate(endEntity.certRequest).isSuccess)
    }

    @Test
    fun `test validate() fails with ExpiredCertificate`() {
      val result = validator.validate(Date.from(Instant.now().plus(Duration.ofDays(365))))
      Assertions.assertTrue(result.isFailure)
      Assertions.assertTrue(result.exceptionOrNull() is ExpiredCertificate)
    }

    @Test
    fun `test validate() fails with NotValidYetCertificate`() {
      val result = validator.validate(Date.from(Instant.now().minus(Duration.ofDays(1))))
      Assertions.assertTrue(result.isFailure)
      Assertions.assertTrue(result.exceptionOrNull() is NotValidYetCertificate)
    }

    @Test
    fun `test validate() fails with mismatched attributes from certificate request`() {
      val result = validator.validate(otherEndEntity.certRequest)
      Assertions.assertTrue(result.isFailure)
      Assertions.assertTrue(result.exceptionOrNull() is CSRMismatch)
    }
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority()
    private val endEntity = certificateAuthority.createTestEndEntity()
    private val otherEndEntity = certificateAuthority.createTestEndEntity()
    private val validator = X509CertificateValidator(endEntity.certificate)
  }
}