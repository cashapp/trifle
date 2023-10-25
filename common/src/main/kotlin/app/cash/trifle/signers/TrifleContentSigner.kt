package app.cash.trifle.signers

import app.cash.trifle.TrifleAlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.operator.ContentSigner

interface TrifleContentSigner : ContentSigner {
  fun subjectPublicKeyInfo(): SubjectPublicKeyInfo

  override fun getAlgorithmIdentifier(): TrifleAlgorithmIdentifier
}
