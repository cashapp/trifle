package app.cash.trifle.signers.jca

import app.cash.trifle.TrifleAlgorithmIdentifier
import app.cash.trifle.signers.TrifleContentSignerTests
import org.bouncycastle.operator.DefaultSignatureNameFinder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.ECGenParameterSpec

internal class JCAContentSignerTests {
  private val contentSigner: JCAContentSigner
  private val algorithmIdentifier: TrifleAlgorithmIdentifier

  init {
    val ecSpec = ECGenParameterSpec("secp256r1")
    val generator = KeyPairGenerator.getInstance("EC")
    generator.initialize(ecSpec, SecureRandom())

    contentSigner = JCAContentSigner(generator.genKeyPair())
    algorithmIdentifier = TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
  }

  @Nested
  @DisplayName("ECDSA JCA Content Signer Tests")
  inner class ECDSAJCAContentSignerTests
    : TrifleContentSignerTests(contentSigner, algorithmIdentifier) {
    @Test
    fun `test sign verifies tink signature with ecdsa raw key template`() {
      val outputStream = contentSigner.outputStream
      outputStream.write(data)
      val signedOutput = contentSigner.signature

      val signature = Signature.getInstance(
        DefaultSignatureNameFinder()
          .getAlgorithmName(algorithmIdentifier)
      )
      signature.initVerify(contentSigner.getPublicKey())
      signature.update(data)

      assertTrue { signature.verify(signedOutput) }
    }
  }
}
