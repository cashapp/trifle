package app.cash.trifle.internal.providers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.ContentVerifier
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder

/**
 * Internal trifle class to enable delegation to bouncycastle for signing with raw JCE keys.
 */
internal class BCContentVerifierProvider(
  private val subjectPublicKeyInfo: SubjectPublicKeyInfo,
) : TrifleContentVerifierProvider() {
  private val delegateProvider by lazy {
    JcaContentVerifierProviderBuilder()
      .setProvider(BouncyCastleProvider())
      .build(subjectPublicKeyInfo)
  }

  override fun get(algorithmIdentifer: AlgorithmIdentifier): ContentVerifier {
    if (algorithmIdentifer != ECDSASha256AlgorithmIdentifier) {
      throw UnsupportedOperationException(
        "Unknown/unsupported AlgorithmId provided to obtain Trifle ContentVerifier"
      )
    }
    return delegateProvider.get(algorithmIdentifer)
  }

  override fun getAssociatedCertificate(): X509CertificateHolder? {
    return null
  }

  override fun hasAssociatedCertificate(): Boolean {
    return false
  }
}