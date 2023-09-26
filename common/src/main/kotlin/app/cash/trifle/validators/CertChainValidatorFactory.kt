package app.cash.trifle.validators

import app.cash.trifle.Certificate
import app.cash.trifle.Certificate.Companion.CERTIFICATE_VERSION
import app.cash.trifle.validators.CertChainValidator.X509CertChainValidator
import java.util.Date

/**
 * Factory class that determines a specific certificate chain validator from the certificate
 * anchor's version.
 */
internal object CertChainValidatorFactory {

  /**
   * Return a certificate validator matching the provided certAnchor.
   * @param certAnchor - The certificate to use as the root certificate in the certificate chain. Its
   *   format will determine how verification should be performed.
   * @param date - The date to use for verification against certificates' validity windows. If null,
   *   the current time is used.
   */
  internal fun get(certAnchor: Certificate, date: Date? = null): CertChainValidator {
    return when (certAnchor.version) {
      CERTIFICATE_VERSION -> X509CertChainValidator(certAnchor, date)
      else -> throw UnsupportedOperationException("Unsupported version of Trifle Certificate")
    }
  }
}
