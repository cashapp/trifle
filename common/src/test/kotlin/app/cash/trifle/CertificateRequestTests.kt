package app.cash.trifle

import app.cash.trifle.providers.jca.JCAContentVerifierProvider
import app.cash.trifle.signers.Buffer
import app.cash.trifle.signers.TrifleContentSigner
import app.cash.trifle.testing.Fixtures.GENERATOR
import app.cash.trifle.testing.TestCertificateAuthority
import okio.ByteString.Companion.toByteString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.DefaultSignatureNameFinder
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import org.bouncycastle.pkcs.PKCSException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import java.security.spec.ECGenParameterSpec

internal class CertificateRequestTests {
  @Nested
  @DisplayName("CertificateRequest#verify() Tests")
  inner class CertificateRequestVerifyTests {
    @Test
    fun `test verify() returns true for a properly signed request`() {
      assertTrue(endEntity.certRequest.verify())
    }

    @Test
    fun `test verify() returns false due to incorrectly signed request`() {
      val pkcs10Request = PKCS10CertificationRequestBuilder(
        X500Name("CN=testName"),
        mismatchedTrifleContentSigner.subjectPublicKeyInfo()
      ).build(mismatchedTrifleContentSigner)

      val certificateRequest =
        CertificateRequest.PKCS10Request(pkcs10Request.encoded.toByteString())
      assertFalse(certificateRequest.verify())
    }

    @Test
    fun `test verify() throws an exception due to unsupported algorithm`() {
      val pkcs10Request = PKCS10CertificationRequestBuilder(
        X500Name("CN=testName"),
        unsupportedTrifleContentSigner.subjectPublicKeyInfo()
      ).build(unsupportedTrifleContentSigner)

      val certificateRequest =
        CertificateRequest.PKCS10Request(pkcs10Request.encoded.toByteString())
      assertThrows<PKCSException>(
        "unable to process signature: Unknown/unsupported AlgorithmId provided to obtain " +
          "Trifle ContentVerifier"
      ) {
        certificateRequest.verify()
      }
    }
  }

  companion object {
    private lateinit var contentVerifierProvider: JCAContentVerifierProvider
    private lateinit var mismatchedTrifleContentSigner: TrifleContentSigner
    private lateinit var unsupportedTrifleContentSigner: TrifleContentSigner

    private val endEntity = TestCertificateAuthority().createTestEndEntity()

    // Registry http://oid-info.com/cgi-bin/display?oid=1.2.840.10040.4.3&a=display
    object DSAAlgorithmIdentifier: TrifleAlgorithmIdentifier("1.2.840.10040.4.3")

    @JvmStatic
    @BeforeAll
    fun setUp() {
      Security.addProvider(BouncyCastleProvider())

      mismatchedTrifleContentSigner = object : TrifleContentSigner {
        private val keyPair: KeyPair
        private val keyPair2: KeyPair
        private val outputStream = Buffer()

        init {
          val ecSpec = ECGenParameterSpec("secp256r1")
          val generator = KeyPairGenerator.getInstance("EC")
          generator.initialize(ecSpec, SecureRandom())
          keyPair = generator.generateKeyPair()
          keyPair2 = generator.generateKeyPair()
        }

        override fun subjectPublicKeyInfo(): SubjectPublicKeyInfo =
          SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)

        override fun getAlgorithmIdentifier(): TrifleAlgorithmIdentifier =
          TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier

        override fun getOutputStream(): Buffer = outputStream

        override fun getSignature(): ByteArray {
          val signature = Signature.getInstance(
            DefaultSignatureNameFinder()
              .getAlgorithmName(algorithmIdentifier)
          )
          signature.initSign(keyPair2.private)
          signature.update(outputStream.toByteArray())
          outputStream.reset()

          return signature.sign()
        }
      }

      unsupportedTrifleContentSigner = object : TrifleContentSigner {
        // Choose an unsupported signing algorithm type.
        val keyPair = KeyPairGenerator.getInstance("DSA").generateKeyPair()
        private val outputStream = Buffer()

        override fun subjectPublicKeyInfo(): SubjectPublicKeyInfo =
          SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)

        override fun getAlgorithmIdentifier(): TrifleAlgorithmIdentifier = DSAAlgorithmIdentifier

        override fun getOutputStream(): Buffer = outputStream

        override fun getSignature(): ByteArray {
          val signature = Signature.getInstance(
            DefaultSignatureNameFinder()
              .getAlgorithmName(algorithmIdentifier)
          )
          signature.initSign(keyPair.private)
          signature.update(outputStream.toByteArray())
          outputStream.reset()

          return signature.sign()
        }
      }

      contentVerifierProvider = JCAContentVerifierProvider(
        SubjectPublicKeyInfo.getInstance(GENERATOR.genKeyPair().public.encoded)
      )
    }
  }
}
