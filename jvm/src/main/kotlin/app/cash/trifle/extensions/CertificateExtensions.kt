package app.cash.trifle.extensions

import app.cash.trifle.Certificate
import app.cash.trifle.CertificateRequest
import app.cash.trifle.validators.CertChainValidatorFactory
import app.cash.trifle.validators.CertificateValidatorFactory
import org.bouncycastle.cert.X509CertificateHolder
import java.security.cert.CertPath
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date

typealias CertificateChain = List<Certificate>

internal typealias CertificateAnchor = Certificate

object CertificateExtensions {
  fun Certificate.toX509Certificate() = certificate.inputStream().use {
    X509FACTORY.generateCertificate(it) as X509Certificate
  }

  fun Certificate.toX509CertificateHolder() = X509CertificateHolder(certificate)

  fun Certificate.validate(date: Date?) = CertificateValidatorFactory.get(this).validate(date)

  fun Certificate.validate(certificateRequest: CertificateRequest) =
    CertificateValidatorFactory.get(this).validate(certificateRequest)

  fun CertificateAnchor.validate(certificateChain: CertificateChain, date: Date?) =
    CertChainValidatorFactory.get(this, date).validate(certificateChain)

  internal fun List<X509Certificate>.generateCertPath(): CertPath =
    X509FACTORY.generateCertPath(this)

  private val X509FACTORY = CertificateFactory.getInstance("X509")
}
