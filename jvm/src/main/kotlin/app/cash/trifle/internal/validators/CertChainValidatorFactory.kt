package app.cash.trifle.internal.validators

import app.cash.trifle.Certificate
import app.cash.trifle.Certificate.Companion.CERTIFICATE_VERSION

/**
 * Factory class that determines a specific certificate chain validator from the certificate
 * anchor's version.
 */
object CertChainValidatorFactory {
  fun get(certAnchor: Certificate): CertChainValidator {
    return when (certAnchor.version) {
      CERTIFICATE_VERSION -> X509CertChainValidator(certAnchor)
      else -> throw UnsupportedOperationException("Unsupported version of Trifle Certificate")
    }
  }
}
