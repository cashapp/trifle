package app.cash.trifle.internal.validators

import app.cash.trifle.Certificate

internal sealed interface CertChainValidator {
  /**
   * Validates the specific list of Trifle Certificates (certificate chain)
   * against the trust anchor(s).
   *
   * @param certChain the list of certificates.
   */
  fun validate(certChain: List<Certificate>): Boolean
}
