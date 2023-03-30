package app.cash.trifle.internal.providers

import app.cash.trifle.CertificateRequest
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.Ed25519AlgorithmIdentifier
import app.cash.trifle.internal.util.TestFixtures
import app.cash.trifle.internal.util.TestFixtures.EC_KEYPAIR
import com.google.crypto.tink.signature.SignatureConfig
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.ECGenParameterSpec

internal class TrifleContentVerifierProviderTests {
  @Nested
  @DisplayName("JCA Content Verifier Provider Tests")
  inner class JCAContentVerifierProviderTests {
    @Test
    fun `test get() returns content verifier with supported signing algorithm`() {
      assertDoesNotThrow {
        contentVerifierProvider.get(ECDSASha256AlgorithmIdentifier)
      }
    }

    @Test
    fun `test get() throws with unsupported signing algorithm`() {
      assertThrows<UnsupportedOperationException>(
        "Unknown/unsupported AlgorithmId provided to obtain Trifle ContentVerifier"
      ) { contentVerifierProvider.get(Ed25519AlgorithmIdentifier) }
    }

    @Test
    fun `test hasAssociatedCertificate() returns false`() {
      assertFalse(contentVerifierProvider.hasAssociatedCertificate())
    }

    @Test
    fun `test getAssociatedCertificate() returns null`() {
      assertNull(contentVerifierProvider.associatedCertificate)
    }
  }

  @Nested
  @DisplayName("JCA Content Verifier Tests")
  inner class JCAContentVerifierTests {
    @Test
    fun `test verify() returns true with appropriate key`() {
      val data = byteArrayOf(0x00, 0x01, 0x02, 0x03)
      val contentVerifier = contentVerifierProvider.get(ECDSASha256AlgorithmIdentifier)
      contentVerifier.outputStream.write(data)

      val signature = Signature.getInstance("SHA256withECDSA")
      signature.initSign(EC_KEYPAIR.private)
      signature.update(data)

      assertTrue(contentVerifier.verify(signature.sign()))
    }

    @Test
    fun `test verify() returns false with different key`() {
      val data = byteArrayOf(0x00, 0x01, 0x02, 0x03)
      val contentVerifier = contentVerifierProvider.get(ECDSASha256AlgorithmIdentifier)
      contentVerifier.outputStream.write(data)

      // generate a different keypair
      val ecSpec = ECGenParameterSpec("secp256r1")
      val generator = KeyPairGenerator.getInstance("EC")
      generator.initialize(ecSpec, SecureRandom())

      val otherKeyPair = generator.generateKeyPair()

      val signature = Signature.getInstance("SHA256withECDSA")
      signature.initSign(otherKeyPair.private)
      signature.update(data)

      assertFalse(contentVerifier.verify(signature.sign()))
    }

    @Test
    fun `test isSignatureValid() with iOS PKCS10 Certificate Request`() {
      val certificateRequest = TestFixtures.CERT_REQUEST
      check(certificateRequest is CertificateRequest.PKCS10Request)
      val pkcs10CertificateRequest = certificateRequest.pkcs10Req

      // Make sure that the pkcs10 request is signature is validated
      assertTrue(
        pkcs10CertificateRequest.isSignatureValid(
          JCAContentVerifierProvider(
            pkcs10CertificateRequest.subjectPublicKeyInfo
          )
        )
      )
    }
  }

  companion object {
    private lateinit var contentVerifierProvider: JCAContentVerifierProvider

    @JvmStatic
    @BeforeAll
    fun setUp() {
      SignatureConfig.register()

      contentVerifierProvider = JCAContentVerifierProvider(
        SubjectPublicKeyInfo.getInstance(EC_KEYPAIR.public.encoded)
      )
    }
  }
}
