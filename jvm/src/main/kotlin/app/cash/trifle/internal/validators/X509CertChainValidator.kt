package app.cash.trifle.internal.validators

import app.cash.trifle.Certificate
import java.security.cert.CertPathValidatorException
import java.security.cert.CertPathValidatorException.BasicReason
import java.security.cert.CertificateFactory
import java.security.cert.PKIXParameters
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

    PATH_VALIDATOR.validate(X509FACTORY.generateCertPath(x509Certs), pkixParams)
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
