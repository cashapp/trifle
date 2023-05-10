package app.cash.trifle.internal.signers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.EdDSAAlgorithmIdentifier
import app.cash.trifle.internal.util.TestFixtures.RAW_ECDSA_P256_KEY_TEMPLATE
import app.cash.trifle.internal.util.TestFixtures.RAW_EDDSA_ED25519_KEY_TEMPLATE
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class TinkContentSignerTests {
  private val ed25519ContentSigner: TinkContentSigner
  private val ed25519AlgorithmIdentifier: TrifleAlgorithmIdentifier

  private val ecdsaContentSigner: TinkContentSigner
  private val ecdsaAlgorithmIdentifier: TrifleAlgorithmIdentifier

  private lateinit var publicKeySign: PublicKeySign
  private lateinit var publicKeyVerify: PublicKeyVerify

  init {
    ed25519AlgorithmIdentifier = EdDSAAlgorithmIdentifier
    ed25519ContentSigner = TinkContentSigner(
      KeysetHandle.generateNew(RAW_EDDSA_ED25519_KEY_TEMPLATE)
    )

    ecdsaAlgorithmIdentifier = ECDSASha256AlgorithmIdentifier
    ecdsaContentSigner = TinkContentSigner(
      KeysetHandle.generateNew(RAW_ECDSA_P256_KEY_TEMPLATE)
    )
  }

  @Nested
  @DisplayName("ED25519 Tink Content Signer Tests")
  inner class ED25519TinkContentSignerTest
    : TrifleContentSignerTests(ed25519ContentSigner, ed25519AlgorithmIdentifier) {
    init {
      publicKeySign = ed25519ContentSigner.getPublicKeySign()
      publicKeyVerify = ed25519ContentSigner.getPublicKeyVerify()
    }

    @Test
    fun `test sign verifies tink signature with ed25519 raw key template`() {
      val outputStream = ed25519ContentSigner.outputStream
      outputStream.write(data)
      val signedOutput = ed25519ContentSigner.signature

      // EdDSA is deterministic
      assertArrayEquals(signedOutput, publicKeySign.sign(data))

      // Verify does not return anything, but throws exception if signature does not verify correctly.
      assertDoesNotThrow { publicKeyVerify.verify(signedOutput, data) }
    }
  }

  @Nested
  @DisplayName("ECDSA Tink Content Signer Tests")
  inner class ECDSATinkContentSignerTests
    : TrifleContentSignerTests(ecdsaContentSigner, ecdsaAlgorithmIdentifier) {
    init {
      publicKeySign = ecdsaContentSigner.getPublicKeySign()
      publicKeyVerify = ecdsaContentSigner.getPublicKeyVerify()
    }

    @Test
    fun `test sign verifies tink signature with ecdsa raw key template`() {
      val outputStream = ecdsaContentSigner.outputStream
      outputStream.write(data)
      val signedOutput = ecdsaContentSigner.signature

      // Verify does not return anything, but throws exception if signature does not verify correctly.
      assertDoesNotThrow { publicKeyVerify.verify(signedOutput, data) }
    }
  }
}
