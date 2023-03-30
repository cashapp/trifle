package app.cash.trifle.internal

import app.cash.trifle.protos.api.alpha.SignedData
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

sealed class TrifleAlgorithmIdentifier(
  oid: String,
  params: ASN1ObjectIdentifier? = null
) : AlgorithmIdentifier(ASN1ObjectIdentifier(oid), params) {
  // Defined in https://www.rfc-editor.org/rfc/rfc8420
  // Registry http://oid-info.com/cgi-bin/display?oid=1.3.101.112&a=display
  object EdDSAAlgorithmIdentifier: TrifleAlgorithmIdentifier(oid = "1.3.101.112")

  // Defined in https://www.rfc-editor.org/rfc/rfc5758
  // Registry http://oid-info.com/cgi-bin/display?oid=1.2.840.10045.4.3.2&a=display
  object ECDSASha256AlgorithmIdentifier: TrifleAlgorithmIdentifier(oid = "1.2.840.10045.4.3.2")

  // Defined in https://datatracker.ietf.org/doc/html/draft-josefsson-pkix-newcurves-01
  // Registry http://oid-info.com/cgi-bin/display?oid=1.3.6.1.4.1.11591.15.1&a=display
  object Ed25519AlgorithmIdentifier: TrifleAlgorithmIdentifier(oid = "1.3.6.1.4.1.11591.15.1")

  // Defined in https://www.rfc-editor.org/rfc/rfc5480
  // Registry http://oid-info.com/cgi-bin/display?oid=1.2.840.10045.3.1.7&a=display
  object P256v1AlgorithmIdentifier: TrifleAlgorithmIdentifier(oid = "1.2.840.10045.3.1.7")

  // Defined in https://www.rfc-editor.org/rfc/rfc3279
  // Registry http://oid-info.com/cgi-bin/display?oid=1.2.840.10045.2.1&a=display
  class ECPublicKeyAlgorithmIdentifier(curve: TrifleAlgorithmIdentifier)
    : TrifleAlgorithmIdentifier(oid = "1.2.840.10045.2.1", params = curve.algorithm)

  internal companion object {
    fun convert(signingAlgorithm: SignedData.Algorithm): TrifleAlgorithmIdentifier {
      return when (signingAlgorithm) {
        SignedData.Algorithm.ECDSA_SHA256 -> ECDSASha256AlgorithmIdentifier
        else -> {
          throw Exception("Unsupported signing algorithm")
        }
      }
    }
  }
}
