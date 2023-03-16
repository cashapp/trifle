package app.cash.trifle.internal.signers

import com.google.common.annotations.VisibleForTesting
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.operator.ContentSigner

internal interface TrifleContentSigner : ContentSigner {
  fun subjectPublicKeyInfo(): SubjectPublicKeyInfo
}
