package app.cash.trifle.internal.signers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.ECPublicKeyAlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.P256v1AlgorithmIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.operator.DefaultSignatureNameFinder
import java.io.ByteArrayOutputStream
import java.io.OutputStream
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
  private val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()

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

  override fun getOutputStream(): OutputStream = outputStream

  override fun getSignature(): ByteArray {
    val signature = Signature.getInstance(
      DefaultSignatureNameFinder()
        .getAlgorithmName(algorithmIdentifier)
    )
    signature.initSign(keyPair.private)
    signature.update(outputStream.toByteArray())
    outputStream.reset()

    return signature.sign()
  }

  internal fun getPublicKey(): PublicKey = keyPair.public
}
