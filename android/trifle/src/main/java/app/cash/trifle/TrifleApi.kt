package app.cash.trifle

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
   * @param certificates - certificate chain to be included in the SignedData message. Must match the key in keyHandle.
   *
   * @return A signed data message in the Trifle format [SignedData].
   */
  fun createSignedData(
    data: ByteArray,
    keyHandle: KeyHandle,
    certificates: List<Certificate>
  ): SignedData = Trifle.EndEntity(keyHandle.keyPair).createSignedData(data, certificates)
}
