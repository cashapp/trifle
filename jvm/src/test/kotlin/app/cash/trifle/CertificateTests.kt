package app.cash.trifle

import app.cash.trifle.testing.TestCertificateAuthority
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.security.*

internal class CertificateTests {
  @Nested
  @DisplayName("Certificate#verify() Tests")
  inner class CertificateVerifyTests {
    @Test
    fun `test verify() returns true for a properly issued certificate`() {
      assertTrue(
        endEntity.certificate.verify(
          endEntity.certRequest,
          endEntity.certChain.drop(1),
          certificateAuthority.rootCertificate
        )
      )
    }

    @Test
    fun `test verify() fails for a legitimate certificate from the same CA, but wrong entity`() {
      val otherEndEntity = certificateAuthority.createTestEndEntity("other_entity")
      assertFalse(
        endEntity.certificate.verify(
          otherEndEntity.certRequest,
          otherEndEntity.certChain.drop(1),
          certificateAuthority.rootCertificate
        )
      )
    }

    @Test
    fun `test verify() fails for a different root certificate`() {
      assertFalse(
        endEntity.certificate.verify(
          endEntity.certRequest,
          endEntity.certChain.drop(1),
          TestCertificateAuthority("otherIssuingEntity").rootCertificate
        )
      )
    }
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority("issuingEntity")
    private val endEntity = certificateAuthority.createTestEndEntity("entity")
  }
}
