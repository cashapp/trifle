package app.cash.trifle.internal.signers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier.ECPublicKeyAlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.Ed25519AlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.TinkAlgorithmIdentifier
import com.google.crypto.tink.BinaryKeysetWriter
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Internal trifle class to enable bouncycastle to delegate signing to Tink primitives rather than
 * requiring Tink key users to extract the material. This should enable trifle Clients to supply
 * Tink keys directly without needing to reason about their internals.
 */
internal class TinkContentSigner(
  private val privateKeysetHandle: KeysetHandle,
) : TrifleContentSigner() {
  private val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
  private val tinkAlgorithmIdentifier = TinkAlgorithmIdentifier
  private val publicKeySign: PublicKeySign by lazy {
    privateKeysetHandle.getPrimitive(PublicKeySign::class.java)
  }
  private val publicKeysetHandle: KeysetHandle by lazy {
    privateKeysetHandle.publicKeysetHandle
  }

  override fun subjectPublicKeyInfo(): SubjectPublicKeyInfo {
    val outputStream = ByteArrayOutputStream()
    CleartextKeysetHandle.write(
      // Ensure we only write the public component of our key!
      publicKeysetHandle,
      BinaryKeysetWriter.withOutputStream(outputStream)
    )
    return SubjectPublicKeyInfo(
      ECPublicKeyAlgorithmIdentifier(Ed25519AlgorithmIdentifier),
      outputStream.toByteArray()
    )
  }

  override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
    return tinkAlgorithmIdentifier
  }

  override fun getOutputStream(): OutputStream {
    return outputStream
  }

  override fun getSignature(): ByteArray {
    val signedBytes = publicKeySign.sign(outputStream.toByteArray())
    outputStream.reset()
    return signedBytes
  }

  internal fun getPublicKeyVerify(): PublicKeyVerify =
    publicKeysetHandle.getPrimitive(PublicKeyVerify::class.java)

  internal fun getPublicKeySign(): PublicKeySign = publicKeySign
}
