package app.cash.trifle

object TrifleApi {
  fun generateKeyHandle(alias: String): KeyHandle = KeyHandle.generateKeyHandle(alias)

  fun generateMobileCertificateRequest(
    entity: String,
    keyHandle: KeyHandle
  ): CertificateRequest = Trifle.EndEntity(keyHandle.keyPair).createCertRequest(entity)
}
