package app.cash.trifle.validators

import app.cash.trifle.Certificate
import app.cash.trifle.TrifleErrors
import app.cash.trifle.validators.CertificateUtil.generateCertPath
import app.cash.trifle.validators.CertificateUtil.toX509Certificate
import java.security.cert.CertPathValidator
import java.security.cert.CertPathValidatorException
import java.security.cert.PKIXParameters
import java.security.cert.PKIXReason
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import java.util.Date

sealed interface CertChainValidator {
  /**
   * Validates the specific list of Trifle Certificates (certificate chain)
   * against the trust anchor(s).
   *
   * @param certChain the list of certificates.
   */
  fun validate(certChain: List<Certificate>): Result<Unit>

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

    override fun validate(certChain: List<Certificate>): Result<Unit> =
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
        when (e.reason) {
          CertPathValidatorException.BasicReason.EXPIRED -> Result.failure(
            TrifleErrors.ExpiredCertificate
          )
          CertPathValidatorException.BasicReason.INVALID_SIGNATURE -> Result.failure(
            TrifleErrors.InvalidSignature
          )
          PKIXReason.NO_TRUST_ANCHOR -> Result.failure(TrifleErrors.NoTrustAnchor)
          else -> Result.failure(
            TrifleErrors.UnspecifiedFailure("Unspecified Trifle verification failure", e)
          )
        }
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
