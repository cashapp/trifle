package app.cash.trifle.internal.validators

import app.cash.trifle.Trifle
import app.cash.trifle.delegate.TinkDelegate
import app.cash.trifle.internal.util.TestFixtures.CERT_ANCHOR
import app.cash.trifle.internal.util.TestFixtures.CERT_CHAIN
import app.cash.trifle.internal.util.TestFixtures.CERT_REQUEST
import app.cash.trifle.internal.util.TestFixtures.RAW_ECDSA_P256_KEY_TEMPLATE
import com.google.crypto.tink.KeysetHandle
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Period

internal class CertChainValidatorTests {
  @Nested
  @DisplayName("X509 Certificate Chain Validator Factory Tests")
  inner class X509CertChainValidatorTests {
    @Test
    fun `test validate() returns true for valid certificate chain`() {
      assertTrue(validator.validate(CERT_CHAIN))
    }

    @Test
    fun `test validate() returns false for invalid certificate chain`() {
      val certificateAuthority = Trifle.CertificateAuthority(
        TinkDelegate(KeysetHandle.generateNew(RAW_ECDSA_P256_KEY_TEMPLATE))
      )
      val issuingCert = certificateAuthority.createRootSigningCertificate(
        "entity", Period.ofDays(1),
      )
      val signedCert = certificateAuthority.signCertificate(issuingCert, CERT_REQUEST)

      assertFalse(validator.validate(listOf(signedCert, issuingCert)))
    }

    @Test
    fun `test validate() returns false for empty certificate chain`() {
      assertFalse(validator.validate(emptyList()))
    }

    @Test
    fun `test validate() returns false if certificate chain only contains root certificate`() {
      assertFalse(validator.validate(listOf(CERT_ANCHOR)))
    }
  }

  companion object {
    private val validator = X509CertChainValidator(CERT_ANCHOR)
  }
}
