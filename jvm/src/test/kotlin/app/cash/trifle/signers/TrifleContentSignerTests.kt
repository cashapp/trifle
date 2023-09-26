package app.cash.trifle.signers

import app.cash.trifle.TrifleAlgorithmIdentifier
import com.google.crypto.tink.signature.SignatureConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal abstract class TrifleContentSignerTests(
  private val contentSigner: TrifleContentSigner,
  private val algorithmIdentifier: TrifleAlgorithmIdentifier
) {
  internal val data = byteArrayOf(0x00, 0x01, 0x02, 0x03)

  @Test
  fun `test signature algorithm returns correct OID`() {
    Assertions.assertEquals(algorithmIdentifier, contentSigner.algorithmIdentifier)
  }

  companion object {
    @JvmStatic
    @BeforeAll
    fun setUp() {
      SignatureConfig.register()
    }
  }
}
