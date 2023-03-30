package app.cash.trifle

import app.cash.trifle.delegate.CertificateAuthorityDelegate
import app.cash.trifle.delegate.EndEntityDelegate
import app.cash.trifle.delegate.TinkDelegate
import app.cash.trifle.internal.validators.CertChainValidatorFactory
import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import app.cash.trifle.internal.providers.JCAContentVerifierProvider
import app.cash.trifle.protos.api.alpha.SignedData
import com.google.crypto.tink.KeysetHandle

object Trifle {
  class CertificateAuthority internal constructor(delegate: CertificateAuthorityDelegate) :
    CertificateAuthorityDelegate by delegate {
    constructor(keysetHandle: KeysetHandle) : this(TinkDelegate(keysetHandle))
  }

  class EndEntity internal constructor(delegate: EndEntityDelegate) :
    EndEntityDelegate by delegate {
    constructor(keysetHandle: KeysetHandle) : this(TinkDelegate(keysetHandle))
  }

  fun SignedData.verify(certAnchor: Certificate): Boolean {
    val signedEnvelopedData = SignedData.EnvelopedData.ADAPTER.decode(checkNotNull(enveloped_data))
    check(signedEnvelopedData.version == TRIFLE_SIGNING_MESSAGE_VERSION)

    val validator = CertChainValidatorFactory.get(certAnchor)
    val certChain = certificates.map { Certificate.deserialize(it.encode()) }
    check(validator.validate(certChain)) { "Invalid certificate path"}

    val signingAlgorithm = TrifleAlgorithmIdentifier.convert(
      checkNotNull(signedEnvelopedData.signing_algorithm)
    )
    val contentVerifier = JCAContentVerifierProvider(certChain.first()).get(signingAlgorithm)

    // Verify the signature
    val sigOut = contentVerifier.outputStream
    sigOut.write(enveloped_data.toByteArray())
    val isVerified = contentVerifier.verify(checkNotNull(signature).toByteArray())
    sigOut.close()

    return isVerified
  }

  private const val TRIFLE_SIGNING_MESSAGE_VERSION: Int = 0
}
