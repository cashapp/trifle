package app.cash.trifle.common

import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.operator.DefaultSignatureNameFinder

object Util {

  fun getAlgorithmIdentifier(algorithmIdentifier: AlgorithmIdentifier): AlgorithmIdentifier {
    when (algorithmIdentifier) {
      TrifleAlgorithmIdentifier.ECPublicKeyAlgorithmIdentifier(TrifleAlgorithmIdentifier.P256v1AlgorithmIdentifier) ->
        return TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier

      else -> throw UnsupportedOperationException(
        "Default signature algorithm is not supported for key algorithm: $algorithmIdentifier"
      )
    }
  }

  fun getSignatureName(algorithmIdentifier: AlgorithmIdentifier): String{
    return DefaultSignatureNameFinder()
      .getAlgorithmName(algorithmIdentifier)
  }
}