package app.cash.trifle.internal.signers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.EdDSAAlgorithmIdentifier
import app.cash.trifle.internal.util.TestFixtures.RAW_ECDSA_P256_KEY_TEMPLATE
import app.cash.trifle.internal.util.TestFixtures.RAW_EDDSA_ED25519_KEY_TEMPLATE
import com.google.crypto.tink.KeysetHandle
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class TinkContentSignerTests {
  @Nested
  @DisplayName("ED25519 Tink Content Signer Tests")
  inner class ED25519TinkContentSignerTest : TrifleContentSignerTests() {
    init {
      algorithmIdentifier = EdDSAAlgorithmIdentifier
      contentSigner = TinkContentSigner(KeysetHandle.generateNew(RAW_EDDSA_ED25519_KEY_TEMPLATE))
      publicKeySign = contentSigner.getPublicKeySign()
      publicKeyVerify = contentSigner.getPublicKeyVerify()
    }

    @Test
    fun `test sign verifies tink signature with ed25519 raw key template`() {
      val outputStream = contentSigner.outputStream
      outputStream.write(data)
      val signedOutput = contentSigner.signature

      // EdDSA is deterministic
      assertArrayEquals(signedOutput, publicKeySign.sign(data))

      // Verify does not return anything, but throws exception if signature does not verify correctly.
      assertDoesNotThrow { publicKeyVerify.verify(signedOutput, data) }
    }
  }

  @Nested
  @DisplayName("ECDSA Tink Content Signer Tests")
  inner class ECDSATinkContentSignerTests : TrifleContentSignerTests() {
    init {
      algorithmIdentifier = ECDSASha256AlgorithmIdentifier
      contentSigner = TinkContentSigner(
        KeysetHandle.generateNew(RAW_ECDSA_P256_KEY_TEMPLATE)
      )
      publicKeySign = contentSigner.getPublicKeySign()
      publicKeyVerify = contentSigner.getPublicKeyVerify()
    }

    @Test
    fun `test sign verifies tink signature with ecdsa raw key template`() {
      val outputStream = contentSigner.outputStream
      outputStream.write(data)
      val signedOutput = contentSigner.signature

      // Verify does not return anything, but throws exception if signature does not verify correctly.
      assertDoesNotThrow { publicKeyVerify.verify(signedOutput, data) }
    }
  }
}
