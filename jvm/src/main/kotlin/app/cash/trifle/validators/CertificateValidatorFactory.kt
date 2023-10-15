package app.cash.trifle.validators

import app.cash.trifle.Certificate
import app.cash.trifle.validators.CertificateValidator.X509CertificateValidator

internal object CertificateValidatorFactory {
  /**
   * Return a certificate validator matching the provided certificate.
   * @param certificate - The certificate to validate. Its format will determine how
   *   verification should be performed.
   */
  fun get(certificate: Certificate): CertificateValidator {
    return when (certificate.version) {
      Certificate.CERTIFICATE_VERSION -> X509CertificateValidator(certificate)
      else -> throw UnsupportedOperationException("Unsupported version of Trifle Certificate")
    }
  }
}
