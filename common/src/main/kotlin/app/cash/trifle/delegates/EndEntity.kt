package app.cash.trifle.delegates

import app.cash.trifle.CertificateRequest
import app.cash.trifle.SignedData
import app.cash.trifle.extensions.CertificateChain
import app.cash.trifle.signers.TrifleContentSigner
import app.cash.trifle.signers.jca.JCAContentSigner
import okio.ByteString.Companion.toByteString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import java.security.KeyPair

class EndEntity internal constructor(private val contentSigner: TrifleContentSigner) {
  /**
   * Create a mobile signing certificate request with a Trifle Content Signer.
   *
   * @param entityName the name with which we'll associate the public key.
   */
  fun createCertRequest(
    entityName: String,
  ): CertificateRequest {
    val subjectName = X500Name("CN=$entityName")
    val subjectPublicKeyInfo = contentSigner.subjectPublicKeyInfo()
    val pkcS10CertificationRequest = PKCS10CertificationRequestBuilder(
      subjectName, subjectPublicKeyInfo
    ).build(contentSigner)
    return CertificateRequest.PKCS10Request(
      pkcS10CertificationRequest.encoded.toByteString()
    )
  }

  /**
   * Create a signed data with a Trifle Content Signer.
   *
   * @param data raw data to be signed.
   * @param certificates certificate chain to be included in the SignedData message.
   */
  fun createSignedData(
    data: ByteArray,
    certificates: CertificateChain
  ): SignedData {
    check(certificates.isNotEmpty()) { "Certificates should not be empty." }

    val envelopedData = SignedData.EnvelopedData(
      version = SignedData.EnvelopedData.ENVELOPED_DATA_VERSION,
      signingAlgorithm = contentSigner.algorithmIdentifier,
      data = data
    )
    val signature = contentSigner.outputStream.use {
      it.write(envelopedData.serialize())
      contentSigner.signature
    }
    return SignedData(
      envelopedData = envelopedData,
      signature = signature,
      certificates = certificates
    ).also {
      it.verify(certificates.last()).getOrThrow()
    }
  }

  object Factory {
    fun get(keyPair: KeyPair): EndEntity = EndEntity(JCAContentSigner(keyPair))
  }
}
