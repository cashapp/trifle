package app.cash.trifle

import app.cash.trifle.TrifleErrors.InvalidSignature
import app.cash.trifle.extensions.CertificateChain
import app.cash.trifle.providers.jca.JCAContentVerifierProvider
import app.cash.trifle.validators.CertChainValidatorFactory
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
  internal val certificates: CertificateChain
) {
  /**
   * Verify that the signed data is valid by checking over the certificate chain and verifying the
   * signature.
   *
   * @param certAnchor - the trust anchor against which we would like to verify the certificates.
   *   This may be the terminal (root) certificate of the chain or may be an intermediate
   *   certificate in the chain which is already trusted.
   *
   * @return - [Result] indicating [Result.isSuccess] or [Result.isFailure]:
   * - success value is expressed as a [Unit] (Nothing)
   * - failure value is expressed as a [TrifleErrors]
   */
  fun verify(certAnchor: Certificate): Result<Unit> {
    // First check to see if the certificate chain validates
    val certChainResult = CertChainValidatorFactory.get(certAnchor).validate(certificates)

    // Next check to see if the signature matches
    return certChainResult.mapCatching {
      val contentVerifier = JCAContentVerifierProvider(certificates.first())
        .get(envelopedData.signingAlgorithm)

      // Verify the signature
      contentVerifier.outputStream.use {
        it.write(envelopedData.serialize())
      }
      val isVerified = contentVerifier.verify(signature)

      if (!isVerified) {
        throw InvalidSignature
      }
    }
  }

  /**
   * VerifyAndExtract performs [SignedData.verify] and extracts the underlying result
   * as a [VerifiedData].
   *
   * @param certAnchor - the trust anchor against which we would like to verify the certificates.
   *   This may be the terminal (root) certificate of the chain or may be an intermediate
   *   certificate in the chain which is already trusted.
   *
   * @return - [Result] indicating [Result.isSuccess] or [Result.isFailure]:
   * - success value is expressed as a [VerifiedData]
   * - failure value is expressed as a [TrifleErrors]
   */
  fun verifyAndExtract(certAnchor: Certificate): Result<VerifiedData> =
    verify(certAnchor).map {
      VerifiedData(envelopedData.data, certificates)
    }

  fun serialize(): ByteArray = SignedDataProto(
    enveloped_data = envelopedData.serialize().toByteString(),
    signature = signature.toByteString(),
    certificates = certificates.map {
      CertificateProto.ADAPTER.decode(it.serialize())
    }
  ).encode()

  fun toPlaintextString(): String =
    "SignedData(enveloped_data=${envelopedData.toPlaintextString()}, " +
    "signature=$signature, " +
    "certificates=$certificates)"

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

    fun toPlaintextString(): String =
      "EnvelopedData(version=$version, signingAlgorithm=$signingAlgorithm, data=$data)"

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

    override fun toString(): String =
      "EnvelopedData(version=$version, signingAlgorithm=$signingAlgorithm, data=[REDACTED])"

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
