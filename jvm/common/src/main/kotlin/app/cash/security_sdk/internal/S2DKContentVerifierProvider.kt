package app.cash.security_sdk.internal

import com.google.crypto.tink.BinaryKeysetReader
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.PublicKeyVerify
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.operator.ContentVerifier
import org.bouncycastle.operator.ContentVerifierProvider

/**
 * Internal s2dk class to enable bouncycastle to delegate signing to Tink primitives rather than
 * requiring Tink key users to extract the material. This should enable s2dk Clients to supply Tink
 * keys directly without needing to reason about their internals.
 */
internal class S2DKContentVerifierProvider(
  private val subjectPublicKeyInfo: SubjectPublicKeyInfo
) : ContentVerifierProvider {

  override fun get(algorithmIdentifer: AlgorithmIdentifier): ContentVerifier {
    // TODO(dcashman): this is effectively ignored at the moment, but we should ensure that the
    // the algorithm passed-in matches our expected Tink algorithm ID.
    if (!algorithmIdentifer.equals(AlgorithmIdentifier(ASN1ObjectIdentifier(TinkContentSigner.ED25519_OID)))
      || !algorithmIdentifer.equals(subjectPublicKeyInfo.algorithm)) {
      throw UnsupportedOperationException("Unknown/unsupported AlgorithmId provided to obtain S2DK ContentVerifier")
    }

    // Create PublicKeyVerify instance for verifier.
    return TinkContentVerifier(
      CleartextKeysetHandle.read(BinaryKeysetReader.withBytes(subjectPublicKeyInfo.publicKeyData.bytes))
        .getPrimitive(PublicKeyVerify::class.java)
    )
  }

  override fun getAssociatedCertificate(): X509CertificateHolder? {
    return null
  }

  override fun hasAssociatedCertificate(): Boolean {
    return false
  }
}
