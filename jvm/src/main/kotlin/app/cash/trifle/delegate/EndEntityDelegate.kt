package app.cash.trifle.delegate

import app.cash.trifle.CertificateRequest

interface EndEntityDelegate {
  /**
   * Create a mobile signing certificate request with a Trifle Content Signer.
   *
   * @param entityName the name with which we'll associate the public key.
   */
  fun createCertRequest(
    entityName: String,
  ): CertificateRequest
}
