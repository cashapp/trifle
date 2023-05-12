package app.cash.trifle.internal.signers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.operator.ContentSigner

internal interface TrifleContentSigner : ContentSigner {
  fun subjectPublicKeyInfo(): SubjectPublicKeyInfo

  override fun getAlgorithmIdentifier(): TrifleAlgorithmIdentifier
}
