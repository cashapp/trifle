package app.cash.security_sdk.internal.signers

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.operator.ContentSigner

sealed class TrifleContentSigner : ContentSigner {
  abstract fun subjectPublicKeyInfo(): SubjectPublicKeyInfo
}
