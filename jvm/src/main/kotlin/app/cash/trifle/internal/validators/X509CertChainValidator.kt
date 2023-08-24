package app.cash.trifle.internal.validators

import app.cash.trifle.Certificate
import app.cash.trifle.TrifleErrors.ExpiredCertificate
import app.cash.trifle.TrifleErrors.InvalidCertPath
import app.cash.trifle.TrifleErrors.InvalidSignature
import app.cash.trifle.TrifleErrors.NoTrustAnchor
import app.cash.trifle.TrifleErrors.UnspecifiedFailure
import java.security.cert.CertPathValidatorException
import java.security.cert.CertPathValidatorException.BasicReason
import java.security.cert.CertificateFactory
import java.security.cert.PKIXParameters
import java.security.cert.PKIXReason
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import java.util.Date
import java.security.cert.CertPathValidator as JCACertPathValidator

/**
 * X.509 specific implementation for validating certificate chains (certificate paths) with
 * a specific set of PKIX parameters.
 */
internal class X509CertChainValidator(
  certAnchor: Certificate,
  date: Date? = null
) : CertChainValidator {
  private val pkixParams: PKIXParameters = PKIXParameters(
    setOf(
      TrustAnchor(
        certAnchor.certificate.inputStream().use {
          X509FACTORY.generateCertificate(it)
        } as X509Certificate,
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
      val x509Certs = certChain.map { trifleCert ->
        trifleCert.certificate.inputStream().use {
          X509FACTORY.generateCertificate(it) as X509Certificate
        }
      }.maybeDropRoot()

      if (x509Certs.isEmpty()) {
        Result.failure(InvalidCertPath)
      } else {
        PATH_VALIDATOR.validate(X509FACTORY.generateCertPath(x509Certs), pkixParams)
        Result.success(Unit)
      }
    } catch (e: CertPathValidatorException) {
      // https://docs.oracle.com/javase/8/docs/api/java/security/cert/PKIXReason.html
      // https://docs.oracle.com/javase/8/docs/api/java/security/cert/CertPathValidatorException.BasicReason.html
      when (e.reason) {
        BasicReason.EXPIRED -> Result.failure(ExpiredCertificate)
        BasicReason.INVALID_SIGNATURE -> Result.failure(InvalidSignature)
        PKIXReason.NO_TRUST_ANCHOR -> Result.failure(NoTrustAnchor)
        else -> Result.failure(
          UnspecifiedFailure("Unspecified Trifle verification failure", e)
        )
      }
    } catch (e: Exception) {
      Result.failure(UnspecifiedFailure("Unspecified Trifle verification failure", e))
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
    private val PATH_VALIDATOR = JCACertPathValidator.getInstance(
      JCACertPathValidator.getDefaultType()
    )
    private val X509FACTORY = CertificateFactory.getInstance("X509")
  }
}
