package app.cash.trifle.internal.providers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier.TinkAlgorithmIdentifier
import com.google.crypto.tink.BinaryKeysetReader
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeyVerify
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.operator.ContentVerifier
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.security.GeneralSecurityException

/**
 * Internal trifle class to enable bouncycastle to delegate signing to Tink primitives rather than
 * requiring Tink key users to extract the material. This should enable trifle Clients to supply
 * Tink keys directly without needing to reason about their internals.
 */
internal class TinkContentVerifierProvider(
  private val subjectPublicKeyInfo: SubjectPublicKeyInfo,
) : TrifleContentVerifierProvider() {
  private val tinkAlgorithmIdentifier = TinkAlgorithmIdentifier
  private val publicKeysetHandle: KeysetHandle by lazy {
    CleartextKeysetHandle.read(
      BinaryKeysetReader.withBytes(subjectPublicKeyInfo.publicKeyData.bytes)
    )
  }

  override fun get(algorithmIdentifer: AlgorithmIdentifier): ContentVerifier {
    // TODO(dcashman): this is effectively ignored at the moment, but we should ensure that
    // the algorithm passed-in matches our expected Tink algorithm ID.
    if (algorithmIdentifer != tinkAlgorithmIdentifier) {
      throw UnsupportedOperationException(
        "Unknown/unsupported AlgorithmId provided to obtain Trifle ContentVerifier"
      )
    }

    return TinkContentVerifier()
  }

  override fun getAssociatedCertificate(): X509CertificateHolder? {
    return null
  }

  override fun hasAssociatedCertificate(): Boolean {
    return false
  }

  /**
   * Internal trifle class to enable bouncycastle to delegate verifying to Tink primitives rather
   * than requiring Tink key users to extract the material. This should enable trifle Clients to
   * supply Tink keys directly without needing to reason about their internals.
   */
  internal inner class TinkContentVerifier : ContentVerifier {
    private val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
    private val publicKeyVerify: PublicKeyVerify by lazy {
      publicKeysetHandle.getPrimitive(PublicKeyVerify::class.java)
    }

    override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
      return tinkAlgorithmIdentifier
    }

    override fun getOutputStream(): OutputStream {
      return outputStream
    }

    override fun verify(expected: ByteArray): Boolean {
      return try {
        publicKeyVerify.verify(expected, outputStream.toByteArray())
        true
      } catch (e: GeneralSecurityException) {
        // Signature did not verify.
        false
      } finally {
        outputStream.reset()
      }
    }
  }
}
