package app.cash.trifle.internal.providers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier.Ed25519AlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.EdDSAAlgorithmIdentifier
import app.cash.trifle.internal.util.toSubjectPublicKeyInfo
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.signature.SignatureConfig
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TinkContentVerifierProviderTest {
  private val data = byteArrayOf(0x00, 0x01, 0x02, 0x03)
  private lateinit var ed25519PublicKeySign: PublicKeySign
  private lateinit var contentVerifierProvider: TinkContentVerifierProvider

  @BeforeEach
  fun setUp() {
    SignatureConfig.register()
    val ed25519PrivateKeyHandle = KeysetHandle.generateNew(KeyTemplates.get("ED25519WithRawOutput"))
    ed25519PublicKeySign = ed25519PrivateKeyHandle.getPrimitive(PublicKeySign::class.java)
    contentVerifierProvider = TinkContentVerifierProvider(
      ed25519PrivateKeyHandle.toSubjectPublicKeyInfo(Ed25519AlgorithmIdentifier)
    )
  }

  @Test
  fun `test get() returns content verifier with appropriate key`() {
    val tinkContentVerifier = contentVerifierProvider.get(EdDSAAlgorithmIdentifier)
    tinkContentVerifier.outputStream.write(data)
    assertTrue(tinkContentVerifier.verify(ed25519PublicKeySign.sign(data)))
  }

  @Test
  fun `test getAssociatedCertificate`() {
    assertNull(contentVerifierProvider.associatedCertificate)
  }
}
