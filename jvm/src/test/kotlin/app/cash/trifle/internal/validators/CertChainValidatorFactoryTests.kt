package app.cash.trifle.internal.validators

import app.cash.trifle.internal.util.TestFixtures.CERT_ANCHOR
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("Certificate Chain Validator Factory Tests")
internal class CertChainValidatorFactoryTests {
  @Test
  fun `test get() returns X509 Certificate Chain Validator for version 0`() {
    val validator = assertDoesNotThrow {
      CertChainValidatorFactory.get(CERT_ANCHOR)
    }
    assertTrue(validator is X509CertChainValidator)
  }

  @Test
  fun `test get() throws for unsupported version`() {
    assertThrows<UnsupportedOperationException>(
      "Unsupported version of Trifle Certificate"
    ) { CertChainValidatorFactory.get(CERT_ANCHOR.copy(version = 1)) }
  }
}
