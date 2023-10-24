package app.cash.trifle.delegate

import app.cash.trifle.CertificateRequest
import app.cash.trifle.SignedData
import app.cash.trifle.extensions.CertificateChain

interface EndEntityDelegate {
  /**
   * Create a mobile signing certificate request with a Trifle Content Signer.
   *
   * @param entityName the name with which we'll associate the public key.
   */
  fun createCertRequest(
    entityName: String,
  ): CertificateRequest

  /**
   * Create a signed data with a Trifle Content Signer.
   *
   * @param data raw data to be signed.
   * @param certificates certificate chain to be included in the SignedData message.
   */
  fun createSignedData(
    data: ByteArray,
    certificates: CertificateChain
  ): SignedData
}
