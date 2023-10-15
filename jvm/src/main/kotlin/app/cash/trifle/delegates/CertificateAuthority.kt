package app.cash.trifle.delegates

import app.cash.trifle.Certificate
import app.cash.trifle.CertificateRequest
import app.cash.trifle.extensions.CertificateExtensions.toX509CertificateHolder
import app.cash.trifle.signers.TrifleContentSigner
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.util.Date

class CertificateAuthority(private val contentSigner: TrifleContentSigner) {
  private val x509BCUtils: JcaX509ExtensionUtils = JcaX509ExtensionUtils()

  /**
   * Creates a self-signed certificate with the provided key and name.
   *
   * @param entityName the name with which we'll associate the public key.
   * @param validityPeriod the length of time for which this certificate should be accepted after
   * issuance.
   */
  fun createRootSigningCertificate(
    entityName: String,
    validityPeriod: Duration,
  ): Certificate {
    // Trifle Certificates are just wrappers around X.509 certificates. Create one with the
    // given name.
    val subjectName = X500Name("CN=$entityName")
    val creationTime = Instant.now()
    val subjectPublicKeyInfo = contentSigner.subjectPublicKeyInfo()
    val signedCert = X509v3CertificateBuilder(
      subjectName,
      BigInteger.ONE,
      Date.from(creationTime),
      Date.from(creationTime.plus(validityPeriod)),
      subjectName,
      contentSigner.subjectPublicKeyInfo()
    ).addExtension(
      Extension.basicConstraints, true, BasicConstraints(true)
    ).addExtension(
      Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign)
    ).addExtension(
      Extension.authorityKeyIdentifier,
      false,
      subjectPublicKeyInfo.toAuthorityKeyIdentifier()
    ).addExtension(
      Extension.subjectKeyIdentifier,
      false,
      subjectPublicKeyInfo.toSubjectKeyIdentifier()
    ).build(contentSigner)

    return Certificate(signedCert.encoded)
  }

  /**
   * Signs CertificateRequest using the provided trifle content signer and issuing certificate.
   *
   * @param issuerCertificate trifle certificate associated with the signer of this cert.
   * @param certificateRequest certificate request used to generate a new certificate
   * @param validity of the certificate before it expires (days)
   */
  fun signCertificate(
    issuerCertificate: Certificate,
    certificateRequest: CertificateRequest,
    validity: Duration = Duration.ofDays(MOBILE_CERTIFICATE_VALIDITY_PERIOD_DAYS),
  ): Certificate = when (certificateRequest) {
    is CertificateRequest.PKCS10Request -> {
      val creationTime = Instant.now()
      val issuerCertHolder = issuerCertificate.toX509CertificateHolder()
      val signedCert = X509v3CertificateBuilder(
        issuerCertHolder.subject,
        BigInteger.valueOf(creationTime.toEpochMilli()),
        Date.from(creationTime),
        Date.from(creationTime.plus(validity)),
        certificateRequest.pkcs10Req.subject,
        certificateRequest.pkcs10Req.subjectPublicKeyInfo
      ).addExtension(
        Extension.authorityKeyIdentifier,
        false,
        contentSigner.subjectPublicKeyInfo().toAuthorityKeyIdentifier()
      ).addExtension(
        Extension.subjectKeyIdentifier,
        false,
        certificateRequest.pkcs10Req.subjectPublicKeyInfo.toSubjectKeyIdentifier()
      ).build(contentSigner)

      Certificate(signedCert.encoded)
    }
  }

  private fun SubjectPublicKeyInfo.toAuthorityKeyIdentifier(): AuthorityKeyIdentifier =
    x509BCUtils.createAuthorityKeyIdentifier(this)

  private fun SubjectPublicKeyInfo.toSubjectKeyIdentifier(): SubjectKeyIdentifier =
    x509BCUtils.createSubjectKeyIdentifier(this)

  companion object {
    // Validity time for device-certificate, currently scoped to 30 days
    internal const val  MOBILE_CERTIFICATE_VALIDITY_PERIOD_DAYS: Long = 30
  }
}
