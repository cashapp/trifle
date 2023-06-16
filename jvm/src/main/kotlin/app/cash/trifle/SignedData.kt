package app.cash.trifle

import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import app.cash.trifle.internal.providers.JCAContentVerifierProvider
import app.cash.trifle.internal.validators.CertChainValidatorFactory
import okio.ByteString.Companion.toByteString
import app.cash.trifle.protos.api.alpha.Certificate as CertificateProto
import app.cash.trifle.protos.api.alpha.SignedData as SignedDataProto
import app.cash.trifle.protos.api.alpha.SignedData.EnvelopedData as EnvelopedDataProto

/**
 * Class representing a Trifle Signed Data.
 *
 * @property envelopedData - The data over which the signature was generated.
 * @property signature - The digital signature output of the enveloped data.
 * @property certificates - List of certificates representing the certificate chain.
 */
data class SignedData internal constructor(
  internal val envelopedData: EnvelopedData,
  internal val signature: ByteArray,
  internal val certificates: List<Certificate>
) {
  fun verify(certAnchor: Certificate): Boolean {
    val validator = CertChainValidatorFactory.get(certAnchor)
    try {
      if (!validator.validate(certificates)) return false
    } catch (e: Exception) {
      return false
    }

    val contentVerifier = JCAContentVerifierProvider(certificates.first())
      .get(envelopedData.signingAlgorithm)

    // Verify the signature
    val sigOut = contentVerifier.outputStream
    sigOut.write(envelopedData.serialize())
    val isVerified = contentVerifier.verify(signature)
    sigOut.close()

    return isVerified
  }

  fun verifyAndExtract(certAnchor: Certificate): VerifiedData? =
    if (verify(certAnchor)) VerifiedData(envelopedData.data, certificates) else null

  fun serialize(): ByteArray = SignedDataProto(
    enveloped_data = envelopedData.serialize().toByteString(),
    signature = signature.toByteString(),
    certificates = certificates.map {
      CertificateProto.ADAPTER.decode(it.serialize())
    }
  ).encode()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SignedData

    if (envelopedData != other.envelopedData) return false
    if (!signature.contentEquals(other.signature)) return false
    if (certificates != other.certificates) return false

    return true
  }

  override fun hashCode(): Int {
    var result = envelopedData.hashCode()
    result = 31 * result + signature.contentHashCode()
    result = 31 * result + certificates.hashCode()
    return result
  }

  companion object {
    fun deserialize(bytes: ByteArray): SignedData {
      val signedDataProto = SignedDataProto.ADAPTER.decode(bytes)
      return SignedData(
        envelopedData = EnvelopedData.deserialize(
          checkNotNull(signedDataProto.enveloped_data).toByteArray()
        ),
        signature = checkNotNull(signedDataProto.signature).toByteArray(),
        certificates = signedDataProto.certificates.map { Certificate.deserialize(it.encode()) }
      )
    }
  }

  /**
   * Class representing a (Signed) Trifle Enveloped Data.
   *
   * @property version - The version number of the enveloped data.
   * @property signingAlgorithm - The Trifle algorithm identifier representing the supported
   *   digital signature algorithm.
   * @property data - The client data to be signed.
   */
  data class EnvelopedData internal constructor(
    internal val version: Int,
    internal val signingAlgorithm: TrifleAlgorithmIdentifier,
    internal val data: ByteArray
  ) {
    fun serialize(): ByteArray = EnvelopedDataProto(
      version = version,
      signing_algorithm = signingAlgorithm.encode(),
      data_ = data.toByteString()
    ).encode()

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as EnvelopedData

      if (version != other.version) return false
      if (signingAlgorithm != other.signingAlgorithm) return false
      if (!data.contentEquals(other.data)) return false

      return true
    }

    override fun hashCode(): Int {
      var result = version
      result = 31 * result + signingAlgorithm.hashCode()
      result = 31 * result + data.contentHashCode()
      return result
    }

    internal companion object {
      internal const val ENVELOPED_DATA_VERSION: Int = 0

      fun deserialize(bytes: ByteArray): EnvelopedData {
        val envelopedDataProto = EnvelopedDataProto.ADAPTER.decode(bytes)
        check(envelopedDataProto.version == ENVELOPED_DATA_VERSION)

        return EnvelopedData(
          version = envelopedDataProto.version,
          signingAlgorithm = TrifleAlgorithmIdentifier.decode(
            checkNotNull(envelopedDataProto.signing_algorithm)
          ),
          data = checkNotNull(envelopedDataProto.data_).toByteArray()
        )
      }
    }
  }
}
