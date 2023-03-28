package app.cash.trifle

import app.cash.trifle.common.Util
import app.cash.trifle.delegate.CertificateAuthorityDelegate
import app.cash.trifle.delegate.EndEntityDelegate
import app.cash.trifle.delegate.TinkDelegate
import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import app.cash.trifle.protos.api.alpha.SignedData
import com.google.crypto.tink.KeysetHandle
import org.bouncycastle.cert.X509CertificateHolder
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.Signature
import java.security.cert.CertPathValidator
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
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

  fun SignedData.verify(certAnchor: ByteArray): Boolean {
    val TRIFLE_SIGNING_MESSAGE_VERSION: Int = 0

    if (certAnchor == null || certAnchor.isEmpty()) {
      throw Exception("Invalid input root cert")
    }

    // Validate and extract signed data
    //   `        raw_data = raw_data,
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

    val cf = CertificateFactory.getInstance("X.509");
    val root: Certificate? = cf.generateCertificate(ByteArrayInputStream(certAnchor))
    val list = ArrayList<Certificate>()

    // Set up certPath. Remove root if in certPath
    for(i in 1..certificatesIn.size) {
      val cert = cf.generateCertificate( ByteArrayInputStream(certificatesIn[i-1].certificate?.toByteArray() ))
      list.add(cert)
    }
    list.remove(root)
    val certPath = cf.generateCertPath(list)

    // set up trust anchor to validate certPath
    val anchor = TrustAnchor(root as X509Certificate, null)
    val params = PKIXParameters(setOf(anchor))
    params.isRevocationEnabled = false

    val pathValidator = CertPathValidator.getInstance(CertPathValidator.getDefaultType())
    pathValidator.validate(certPath, params)

    // Verify the signature
    // get the public key needed to verify signature
    val certHolder = X509CertificateHolder(certificatesIn.first().certificate?.toByteArray())
    val publicKey = KeyFactory.getInstance("EC")
      .generatePublic(
        X509EncodedKeySpec(certHolder.subjectPublicKeyInfo.encoded)
      )

    // check if signingAlgorithm matches the public key in cert
    check(signingAlgorithm  == Util.getAlgorithmIdentifier(certHolder.subjectPublicKeyInfo.algorithm))

    val signer: Signature = Signature.getInstance(Util.getSignatureName(signingAlgorithm))
    signer.initVerify(publicKey)
    signer.update(messageIn.toByteArray())
    return signer.verify(signatureIn.toByteArray())
  }
}
