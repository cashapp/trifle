package app.cash.trifle

import app.cash.trifle.delegate.CertificateAuthorityDelegate
import app.cash.trifle.delegate.EndEntityDelegate
import app.cash.trifle.delegate.TinkDelegate
import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import app.cash.trifle.internal.providers.BCContentVerifierProvider
import app.cash.trifle.protos.api.alpha.SignedData
import com.google.crypto.tink.KeysetHandle
import org.bouncycastle.cert.X509CertificateHolder
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

object Trifle {
  class CertificateAuthority internal constructor(delegate: CertificateAuthorityDelegate) :
    CertificateAuthorityDelegate by delegate {
    constructor(keysetHandle: KeysetHandle) : this(TinkDelegate(keysetHandle))
  }

  class EndEntity internal constructor(delegate: EndEntityDelegate) :
    EndEntityDelegate by delegate {
    constructor(keysetHandle: KeysetHandle) : this(TinkDelegate(keysetHandle))
  }

  fun SignedData.verify(): Boolean {
    val TRIFLE_SIGNING_MESSAGE_VERSION: Int = 0

    // Validate and extract signed data
    //           raw_data = raw_data,
    //           signature = signature,
    //           certificates = certificates,
    val messageIn = checkNotNull(enveloped_data)
    val signatureIn = checkNotNull(signature)
    val certificatesIn = certificates

    val signingMessage = SignedData.EnvelopedData.ADAPTER.decode(messageIn)

    // Validate and extract signing message
    //       builder.version = version
    //       builder.signing_algorithm = signing_algorithm
    //       builder.data_ = data_
    check(signingMessage.version == TRIFLE_SIGNING_MESSAGE_VERSION)
    val signingAlgorithm = when (signingMessage.signing_algorithm) {
      SignedData.Algorithm.ECDSA_SHA256 -> TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
      else -> {
        throw Exception("Unsupported signing algorithm")
      }
    }

    // Extract the x.509 certificate leaf from certificate list passed in
    val certHolder = X509CertificateHolder(certificatesIn[0].certificate?.toByteArray())
    val publicKey = KeyFactory.getInstance("EC")
      .generatePublic(
        X509EncodedKeySpec(certHolder.subjectPublicKeyInfo.encoded)
      )
    // check if signingAlgorithm matches the public key in cert
    // parse the algorithm oid to a trifle algorithm identifier
    // check(signingAlgorithm == certHolder.subjectPublicKeyInfo.algorithm)

    // validate chain cert



    val signer: Signature = Signature.getInstance("SHA256withECDSA")
    signer.initVerify(publicKey)
    signer.update(messageIn.toByteArray())
    return signer.verify(signatureIn.toByteArray())
  }
}
