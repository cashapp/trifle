package app.cash.trifle.signers.jca

import app.cash.trifle.TrifleAlgorithmIdentifier
import app.cash.trifle.TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
import app.cash.trifle.TrifleAlgorithmIdentifier.ECPublicKeyAlgorithmIdentifier
import app.cash.trifle.TrifleAlgorithmIdentifier.P256v1AlgorithmIdentifier
import app.cash.trifle.signers.Buffer
import app.cash.trifle.signers.TrifleContentSigner
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.operator.DefaultSignatureNameFinder
import java.security.KeyPair
import java.security.PublicKey
import java.security.Signature

/**
 * Internal trifle class that uses JCA to performing signing with a given KeyPair.
 * This should enable trifle Clients to supply an asymmetric keypair directly without needing
 * to reason about their internals.
 */
internal class JCAContentSigner(
  private val keyPair: KeyPair,
) : TrifleContentSigner {
  private val outputStream = Buffer()

  override fun subjectPublicKeyInfo(): SubjectPublicKeyInfo {
    return SubjectPublicKeyInfo.getInstance(
      keyPair.public.encoded
    )
  }

  override fun getAlgorithmIdentifier(): TrifleAlgorithmIdentifier {
    when (val algorithm = subjectPublicKeyInfo().algorithm) {
      ECPublicKeyAlgorithmIdentifier(P256v1AlgorithmIdentifier) ->
        return ECDSASha256AlgorithmIdentifier

      else -> throw UnsupportedOperationException(
        "Default signature algorithm is not supported for key algorithm: $algorithm"
      )
    }
  }

  override fun getOutputStream(): Buffer = outputStream

  override fun getSignature(): ByteArray {
    val signedBytes = outputStream.use {
      val signature = Signature.getInstance(
        DefaultSignatureNameFinder()
          .getAlgorithmName(algorithmIdentifier)
      )
      signature.initSign(keyPair.private)
      signature.update(it.toByteArray())
      signature.sign()
    }
    outputStream.reset()
    return signedBytes
  }

  internal fun getPublicKey(): PublicKey = keyPair.public
}
