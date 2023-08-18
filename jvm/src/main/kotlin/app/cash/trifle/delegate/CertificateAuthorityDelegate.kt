package app.cash.trifle.delegate

import app.cash.trifle.Certificate
import app.cash.trifle.CertificateRequest
import java.time.Period

interface CertificateAuthorityDelegate {
  /**
   * Creates a self-signed certificate with the provided key and name.
   *
   * @param entityName the name with which we'll associate the public key.
   * @param validityPeriod the length of time for which this certificate should be accepted after
   * issuance.
   */
  fun createRootSigningCertificate(
    entityName: String,
    validityPeriod: Period,
  ): Certificate

  /**
   * Signs CertificateRequest using the provided trifle content signer and issuing certificate.
   *
   * @param issuerCertificate trifle certificate associated with the signer of this cert.
   * @param certificateRequest certificate request used to generate a new certificate
   * @param validity of the certificate before it expires (days)
   */
  fun signCertificate(
    issuerCertificate: Certificate,
    certificateRequest: CertificateRequest,
    validity: Period = Period.ofDays(MOBILE_CERTIFICATE_VALIDITY_PERIOD_DAYS),
  ): Certificate

  companion object {
    // Validity time for device-certificate, currently scoped to 30 day
    internal const val  MOBILE_CERTIFICATE_VALIDITY_PERIOD_DAYS: Int = 30
  }
}
