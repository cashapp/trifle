package app.cash.trifle.validators

import app.cash.trifle.testing.TestCertificateAuthority
import app.cash.trifle.validators.CertificateValidator.X509CertificateValidator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("Certificate Validator Factory Tests")
class CertificateValidatorFactoryTests {
  @Test
  fun `test get() returns X509 Certificate Validator for version 0`() {
    val validator = assertDoesNotThrow {
      CertificateValidatorFactory.get(certAnchor)
    }
    Assertions.assertTrue(validator is X509CertificateValidator)
  }

  @Test
  fun `test get() throws for unsupported version`() {
    assertThrows<UnsupportedOperationException>(
      "Unsupported version of Trifle Certificate"
    ) { CertificateValidatorFactory.get(certAnchor.copy(version = 1)) }
  }

  private companion object {
    private val certAnchor = TestCertificateAuthority().rootCertificate
  }
}