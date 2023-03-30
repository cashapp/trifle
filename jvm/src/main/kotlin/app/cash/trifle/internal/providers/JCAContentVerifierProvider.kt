package app.cash.trifle.internal.providers

import app.cash.trifle.Certificate
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.EdDSAAlgorithmIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.Certificate as X509Certificate
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.ContentVerifier
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder

/**
 * Internal trifle class to enable delegation to bouncycastle for signing with raw JCE keys.
 */
internal class JCAContentVerifierProvider(
  private val subjectPublicKeyInfo: SubjectPublicKeyInfo,
) : TrifleContentVerifierProvider() {
  internal constructor(certificate: Certificate) : this(
    X509Certificate.getInstance(certificate.certificate).subjectPublicKeyInfo
  )

  private val delegateProvider by lazy {
    JcaContentVerifierProviderBuilder()
      .setProvider(BouncyCastleProvider())
      .build(subjectPublicKeyInfo)
  }

  override fun get(algorithmIdentifer: AlgorithmIdentifier): ContentVerifier {
    if (algorithmIdentifer != ECDSASha256AlgorithmIdentifier
      && algorithmIdentifer != EdDSAAlgorithmIdentifier
    ) {
      throw UnsupportedOperationException(
        "Unknown/unsupported AlgorithmId provided to obtain Trifle ContentVerifier"
      )
    }

    return delegateProvider.get(algorithmIdentifer)
  }

  override fun getAssociatedCertificate(): X509CertificateHolder? = null

  override fun hasAssociatedCertificate(): Boolean = false
}
