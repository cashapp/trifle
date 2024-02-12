package app.cash.trifle.validators

import app.cash.trifle.Certificate
import app.cash.trifle.TrifleErrors
import app.cash.trifle.extensions.CertificateChain
import app.cash.trifle.extensions.CertificateExtensions.generateCertPath
import app.cash.trifle.extensions.CertificateExtensions.toX509Certificate
import java.security.cert.CertPathValidator
import java.security.cert.CertPathValidatorException
import java.security.cert.PKIXParameters
import java.security.cert.PKIXReason
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import java.util.Date

internal sealed interface CertChainValidator {
  /**
   * Validates the specific list of Trifle Certificates (certificate chain)
   * against the trust anchor(s).
   *
   * @param certChain the list of certificates.
   */
  fun validate(certChain: CertificateChain): Result<Unit>

  /**
   * X.509 specific implementation for validating certificate chains (certificate paths) with
   * a specific set of PKIX parameters.
   */
  class X509CertChainValidator(certAnchor: Certificate, date: Date? = null) : CertChainValidator {
    private val pkixParams: PKIXParameters = PKIXParameters(
      setOf(
        TrustAnchor(
          certAnchor.toX509Certificate(),
          null
        )
      )
    )

    init {
      pkixParams.isRevocationEnabled = false
      pkixParams.date = date
    }

    override fun validate(certChain: CertificateChain): Result<Unit> =
      try {
        val x509Certs = certChain.map { it.toX509Certificate() }.maybeDropRoot()

        if (x509Certs.isEmpty()) {
          Result.failure(TrifleErrors.InvalidCertPath)
        } else {
          PATH_VALIDATOR.validate(x509Certs.generateCertPath(), pkixParams)
          Result.success(Unit)
        }
      } catch (e: CertPathValidatorException) {
        // https://docs.oracle.com/javase/8/docs/api/java/security/cert/PKIXReason.html
        // https://docs.oracle.com/javase/8/docs/api/java/security/cert/CertPathValidatorException.BasicReason.html
        Result.failure(
          when (e.reason) {
            CertPathValidatorException.BasicReason.EXPIRED -> TrifleErrors.ExpiredCertificate
            CertPathValidatorException.BasicReason.NOT_YET_VALID -> TrifleErrors.NotValidYetCertificate
            CertPathValidatorException.BasicReason.INVALID_SIGNATURE -> TrifleErrors.InvalidSignature
            PKIXReason.NO_TRUST_ANCHOR -> TrifleErrors.NoTrustAnchor
            else -> TrifleErrors.UnspecifiedFailure("Unspecified Trifle verification failure", e)
          }
        )
      } catch (e: Exception) {
        Result.failure(
          TrifleErrors.UnspecifiedFailure(
            "Unspecified Trifle verification failure",
            e
          )
        )
      }

    private fun List<X509Certificate>.maybeDropRoot(): List<X509Certificate> {
      // Remove root and all certs following it in the chain
      val rootIndex = indexOfFirst {
        it.keyUsage != null && it.keyUsage[KEY_CERT_SIGN]
      }
      return if (rootIndex > -1) {
        dropLast(size - rootIndex)
      } else {
        this
      }
    }

    private companion object {
      private const val KEY_CERT_SIGN = 5
      private val PATH_VALIDATOR = CertPathValidator.getInstance(
        CertPathValidator.getDefaultType()
      )
    }
  }
}
