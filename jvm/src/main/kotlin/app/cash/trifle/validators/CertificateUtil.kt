package app.cash.trifle.validators

import app.cash.trifle.Certificate
import java.security.cert.CertPath
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

internal object CertificateUtil {
  internal fun Certificate.toX509Certificate() = certificate.inputStream().use {
    X509FACTORY.generateCertificate(it) as X509Certificate
  }

  internal fun List<X509Certificate>.generateCertPath(): CertPath =
    X509FACTORY.generateCertPath(this)

  private val X509FACTORY = CertificateFactory.getInstance("X509")
}
