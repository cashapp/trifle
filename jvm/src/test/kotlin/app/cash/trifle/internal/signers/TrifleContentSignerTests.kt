package app.cash.trifle.internal.signers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.signature.SignatureConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal abstract class TrifleContentSignerTests {
  internal val data = byteArrayOf(0x00, 0x01, 0x02, 0x03)

  internal lateinit var algorithmIdentifier: TrifleAlgorithmIdentifier
  internal lateinit var contentSigner: TinkContentSigner
  internal lateinit var publicKeySign: PublicKeySign
  internal lateinit var publicKeyVerify: PublicKeyVerify

  @Test
  fun `test signature algorithm returns correct OID`() {
    assertEquals(algorithmIdentifier, contentSigner.algorithmIdentifier)
  }

  companion object {
    @JvmStatic
    @BeforeAll
    fun setUp() {
      SignatureConfig.register()
    }
  }
}
