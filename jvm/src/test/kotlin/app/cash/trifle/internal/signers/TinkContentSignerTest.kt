package app.cash.trifle.internal.signers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier.TinkAlgorithmIdentifier
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.signature.SignatureConfig
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class TinkContentSignerTest {
  private val data = byteArrayOf(0x00, 0x01, 0x02, 0x03)
  private lateinit var contentSigner: TinkContentSigner
  private lateinit var ed25519PublicKeySign: PublicKeySign
  private lateinit var ed25519PublicKeyVerify: PublicKeyVerify

  @BeforeEach
  fun setUp() {
    SignatureConfig.register()
    contentSigner = TinkContentSigner(KeysetHandle.generateNew(KeyTemplates.get("ED25519")))
    ed25519PublicKeySign = contentSigner.getPublicKeySign()
    ed25519PublicKeyVerify = contentSigner.getPublicKeyVerify()
  }

  @Test
  fun `test ed25519 signature algorithm returns correct OID`() {
    assertEquals(TinkAlgorithmIdentifier, contentSigner.algorithmIdentifier)
  }

  @Test
  fun `test sign matches tink signature`() {
    val outputStream = contentSigner.outputStream
    outputStream.write(data)
    val signedOutput = contentSigner.signature

    assertArrayEquals(signedOutput, ed25519PublicKeySign.sign(data))

    // Verify does not return anything, but throws exception if signature does not verify correctly.
    assertDoesNotThrow { ed25519PublicKeyVerify.verify(signedOutput, data) }
  }
}
