package app.cash.trifle.internal.validators

import app.cash.trifle.NoTrustAnchorException
import app.cash.trifle.testing.TestCertificateAuthority
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.cert.CertPathValidatorException

internal class CertChainValidatorTests {
  @Nested
  @DisplayName("X509 Certificate Chain Validator Tests")
  inner class X509CertChainValidatorTests {
    @Test
    fun `test validate() returns true for valid certificate chain`() {
      assertTrue(validator.validate(endEntity.certChain))
    }

    @Test
    fun `test validate() throws CertPathValidatorException for invalid certificate chain`() {
      assertThrows<NoTrustAnchorException> {
        validator.validate(TestCertificateAuthority().createTestEndEntity().certChain)
      }
    }

    @Test
    fun `test validate() returns false for empty certificate chain`() {
      assertFalse(validator.validate(emptyList()))
    }

    @Test
    fun `test validate() returns false if certificate chain only contains root certificate`() {
      assertFalse(validator.validate(listOf(certificateAuthority.rootCertificate)))
    }
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority()
    private val endEntity = certificateAuthority.createTestEndEntity()
    private val validator = X509CertChainValidator(certificateAuthority.rootCertificate)
  }
}
