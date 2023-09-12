package app.cash.trifle

import app.cash.trifle.validators.CertChainValidatorFactory
import app.cash.trifle.validators.CertificateValidatorFactory
import java.util.Date

object TrifleApi {
  /**
   * Create a new mobile Trifle keypair for which can be used to create a
   * certificate request and to sign messages. The library (Trifle) will
   * automatically try to choose the best algorithm and key type available on
   * this device.
   *
   * @param alias - key alias used for identifying a keyhandle.
   *
   * @return An opaque Trifle representation [KeyHandle] of the key-pair, which the client will need to store.
   */
  fun generateKeyHandle(alias: String): KeyHandle = KeyHandle.generateKeyHandle(alias)

  /**
   * [KeyHandle] is valid iff [KeyHandle.alias] exists in the Key Store.
   *
   * @param keyHandle - key handle that is to be validated.
   *
   * @returns - boolean value for validity
   */
  fun isValid(keyHandle: KeyHandle): Boolean = KeyHandle.containsAlias(keyHandle.alias)

  /**
   * Deletes the [KeyHandle] from the Key Store.
   *
   * @param keyHandle - key handle that is to be deleted.
   */
  fun delete(keyHandle: KeyHandle) = KeyHandle.deleteAlias(keyHandle.alias)

  /**
   * Generate a Trifle [CertificateRequest], signed by the provided
   * keyHandle, that can be presented to the Certificate Authority (CA) for
   * verification.
   *
   * @param entity - the name associated with the public key.
   * @param keyHandle - key handle used for the signing.
   *
   * @returns An opaque Trifle representation [CertificateRequest] of the certificate request.
   */
  fun generateMobileCertificateRequest(
    entity: String,
    keyHandle: KeyHandle
  ): CertificateRequest = Trifle.EndEntity(keyHandle.keyPair).createCertRequest(entity)

  /**
   * Sign the provided data with the provided key, including appropriate Trifle
   * metadata, such as the accompanying certificate.
   *
   * @param data - raw data to be signed.
   * @param keyHandle - key handle used for the signing.
   * @param certificates - certificate chain to be included in the SignedData message.
   *   Must match the key in keyHandle.
   *
   * @return A signed data message in the Trifle format [SignedData].
   */
  fun createSignedData(
    data: ByteArray,
    keyHandle: KeyHandle,
    certificates: List<Certificate>
  ): SignedData = Trifle.EndEntity(keyHandle.keyPair).createSignedData(data, certificates)

  /**
   * Verify that the provided Trifle Certificate Chain is valid.
   *
   * @param certificateChain - list of certificates. Namely, the first
   *   entry should be the certificate corresponding to the subject, and the subsequent being
   *   the issuer of the former certificate, and each thereafter should be the issuer of the
   *   one before it.
   * @param anchorCertificate - the trust anchor against which we would like to verify the
   *   certificateChain instead. This may be the terminal (root) certificate of the chain or may be
   *   an intermediate certificate in the chain which is already trusted.
   * @param date - The date to use for verification against certificates' validity windows. If null,
   *   the current time is used.
   *
   * @return - [Result] indicating [Result.isSuccess] or [Result.isFailure]:
   * - success value is expressed as a [Unit] (Nothing)
   * - failure value is expressed as a [TrifleErrors]
   */
  fun verifyChain(
    certificateChain: List<Certificate>,
    anchorCertificate: Certificate? = null,
    date: Date? = null
  ): Result<Unit> = CertChainValidatorFactory.get(
    certAnchor = anchorCertificate ?: certificateChain.last(),
    date = date
  ).validate(certificateChain)

  /**
   * Verify that the provided Trifle Certificate is valid.
   *
   * @param certificate - the certificate to verify
   * @param date - The date to use for verification against certificate' validity windows. If null,
   *   the current time is used.
   *
   * @return - [Result] indicating [Result.isSuccess] or [Result.isFailure]:
   * - success value is expressed as a [Unit] (Nothing)
   * - failure value is expressed as a [TrifleErrors]
   */
  fun verifyValidity(
    certificate: Certificate,
    date: Date? = null
  ): Result<Unit> = CertificateValidatorFactory.get(certificate).validate(date)

  /**
   * Verify that the provided Trifle Certificate matches the Certificate Requests' attributes.
   *
   * @param certificate - the certificate to verify
   * @param certificateRequest - request used to generate this certificate
   *
   * @return - [Result] indicating [Result.isSuccess] or [Result.isFailure]:
   * - success value is expressed as a [Unit] (Nothing)
   * - failure value is expressed as a [TrifleErrors]
   */
  fun verifyCertRequestResponse(
    certificate: Certificate,
    certificateRequest: CertificateRequest
  ): Result<Unit> = CertificateValidatorFactory.get(certificate).validate(certificateRequest)
}
