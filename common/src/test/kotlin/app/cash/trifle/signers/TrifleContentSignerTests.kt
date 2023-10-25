package app.cash.trifle.signers

import app.cash.trifle.TrifleAlgorithmIdentifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal abstract class TrifleContentSignerTests(
  private val contentSigner: TrifleContentSigner,
  private val algorithmIdentifier: TrifleAlgorithmIdentifier
) {
  internal val data = byteArrayOf(0x00, 0x01, 0x02, 0x03)

  @Test
  fun `test signature algorithm returns correct OID`() {
    assertEquals(algorithmIdentifier, contentSigner.algorithmIdentifier)
  }
}
