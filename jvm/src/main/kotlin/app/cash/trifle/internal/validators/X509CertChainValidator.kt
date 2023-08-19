package app.cash.trifle.internal.validators

import app.cash.trifle.*
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
internal class X509CertChainValidator(certAnchor: Certificate, date: Date? = null) : CertChainValidator {

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

  override fun validate(certChain: List<Certificate>): Boolean {
    val x509Certs = certChain.map { trifleCert ->
      trifleCert.certificate.inputStream().use {
        X509FACTORY.generateCertificate(it) as X509Certificate
      }
    }.maybeDropRoot()

    if (x509Certs.isEmpty()) {
      return false
    }

    try {
      PATH_VALIDATOR.validate(X509FACTORY.generateCertPath(x509Certs), pkixParams)
    } catch (e: CertPathValidatorException) {
      val reason = e.reason
      // https://docs.oracle.com/javase/8/docs/api/java/security/cert/PKIXReason.html
      // https://docs.oracle.com/javase/8/docs/api/java/security/cert/CertPathValidatorException.BasicReason.html
      when (reason) {
          BasicReason.EXPIRED -> throw ExpiredCertificateException("Expired Trifle certificate", e)
          BasicReason.INVALID_SIGNATURE -> throw IncorrectSignatureException("Invalid Trifle signature", e)
          PKIXReason.NO_TRUST_ANCHOR -> throw NoTrustAnchorException("No acceptable Trifle trust anchor found", e)
          else -> throw UnSpecifiedFailureException("Unspecified Trifle verification failure", e)
      }
    } catch (e: Exception) {
      throw UnSpecifiedFailureException("Unspecified Trifle verification failure", e)
    }
    return true
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
