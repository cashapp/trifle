package app.cash.trifle

import app.cash.trifle.internal.signers.TinkContentSigner
import app.cash.trifle.internal.signers.TrifleContentSigner
import app.cash.trifle.internal.util.TestFixtures
import com.google.crypto.tink.signature.SignatureConfig
import okio.ByteString.Companion.toByteString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.DefaultSignatureNameFinder
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import org.bouncycastle.pkcs.PKCSException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import java.security.spec.ECGenParameterSpec

internal class CertificateRequestTest {

  @BeforeEach
  fun setUp() {
    SignatureConfig.register()
    Security.addProvider(BouncyCastleProvider())
  }

  @Test
  fun verifySignedRequest() {
    val certificateRequest = CertificateRequest.PKCS10Request(TestFixtures.PKCS10Request.toByteString())
    assertTrue(certificateRequest.verify())
  }

  @Test
  fun verifyIncorrectlySignedRequest() {
    val ecSpec = ECGenParameterSpec("secp256r1")
    val generator = KeyPairGenerator.getInstance("EC")
    generator.initialize(ecSpec, SecureRandom())

    val keyPair: KeyPair = generator.generateKeyPair()
    val keyPair2: KeyPair = generator.generateKeyPair()
    val outputStream = ByteArrayOutputStream()
    val noopTrifleContentSigner = object: TrifleContentSigner() {
      override fun subjectPublicKeyInfo(): SubjectPublicKeyInfo =
        SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)

      override fun getAlgorithmIdentifier(): AlgorithmIdentifier = subjectPublicKeyInfo().algorithm

      override fun getOutputStream(): OutputStream = outputStream

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

    val pkcs10Request = PKCS10CertificationRequestBuilder(
      X500Name("CN=testName"),
      noopTrifleContentSigner.subjectPublicKeyInfo()
    ).build(noopTrifleContentSigner)
    val certificateRequest = CertificateRequest.PKCS10Request(pkcs10Request.encoded.toByteString())
    val exception = assertThrows<PKCSException> {
      certificateRequest.verify()
    }
    assertEquals(
      "unable to process signature: Unknown/unsupported AlgorithmId provided to obtain Trifle ContentVerifier",
      exception.message)

    assertFalse(certificateRequest.verify())
  }

  @Test
  fun verifyUnknownAlgorithm() {
    // Choose an unsupported signing algorithm type.
    val keyPair = KeyPairGenerator.getInstance("DSA").generateKeyPair();
    val outputStream = ByteArrayOutputStream()
    val unsupportedTrifleContentSigner = object: TrifleContentSigner() {
      override fun subjectPublicKeyInfo(): SubjectPublicKeyInfo =
        SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)

      override fun getAlgorithmIdentifier(): AlgorithmIdentifier = subjectPublicKeyInfo().algorithm

      override fun getOutputStream(): OutputStream = outputStream

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

    val pkcs10Request = PKCS10CertificationRequestBuilder(
      X500Name("CN=testName"),
      unsupportedTrifleContentSigner.subjectPublicKeyInfo()
    ).build(unsupportedTrifleContentSigner)
    val certificateRequest = CertificateRequest.PKCS10Request(pkcs10Request.encoded.toByteString())
    val exception = assertThrows<PKCSException> {
      certificateRequest.verify()
    }
    assertEquals(
      "unable to process signature: Unknown/unsupported AlgorithmId provided to obtain Trifle ContentVerifier",
      exception.message)
  }
}