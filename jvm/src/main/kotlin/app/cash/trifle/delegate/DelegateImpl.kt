package app.cash.trifle.delegate

import app.cash.trifle.Certificate
import app.cash.trifle.CertificateRequest
import app.cash.trifle.SignedData
import app.cash.trifle.SignedData.EnvelopedData.Companion.ENVELOPED_DATA_VERSION
import app.cash.trifle.internal.signers.TrifleContentSigner
import okio.ByteString.Companion.toByteString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import java.math.BigInteger
import java.time.Instant
import java.time.Period
import java.util.Date

/**
 * Shared implementation for certificate enrollment using Trifle Content Signer.
 */
internal open class DelegateImpl(
  private val contentSigner: TrifleContentSigner
) : CertificateAuthorityDelegate, EndEntityDelegate {
  private val x509BCUtils = JcaX509ExtensionUtils()

  override fun signCertificate(
    issuerCertificate: Certificate,
    certificateRequest: CertificateRequest,
    validity: Period,
  ): Certificate = when (certificateRequest) {
    is CertificateRequest.PKCS10Request -> {
      val creationTime = Instant.now()
      val issuerCertHolder = X509CertificateHolder(issuerCertificate.certificate)
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

  override fun createRootSigningCertificate(
    entityName: String,
    validityPeriod: Period,
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

  override fun createCertRequest(
    entityName: String,
  ): CertificateRequest {
    val subjectName = X500Name("CN=$entityName")
    val subjectPublicKeyInfo = contentSigner.subjectPublicKeyInfo()
    val pkcS10CertificationRequest = PKCS10CertificationRequestBuilder(
      subjectName, subjectPublicKeyInfo
    ).build(contentSigner)
    return CertificateRequest.PKCS10Request(
      pkcS10CertificationRequest.encoded.toByteString()
    )
  }

  override fun createSignedData(
    data: ByteArray,
    certificates: List<Certificate>
  ): SignedData {
    check(certificates.isNotEmpty()) { "Certificates should not be empty." }

    val envelopedData = SignedData.EnvelopedData(
      version = ENVELOPED_DATA_VERSION,
      signingAlgorithm = contentSigner.algorithmIdentifier,
      data = data
    )
    val signature = contentSigner.outputStream.use {
      it.write(envelopedData.serialize())
      contentSigner.signature
    }
    return SignedData(
      envelopedData = envelopedData,
      signature = signature,
      certificates = certificates
    ).also {
      if (!it.verify(certificates.last())) {
        throw IllegalStateException("Signed data output is invalid.")
      }
    }
  }

  private fun SubjectPublicKeyInfo.toAuthorityKeyIdentifier(): AuthorityKeyIdentifier =
    x509BCUtils.createAuthorityKeyIdentifier(this)

  private fun SubjectPublicKeyInfo.toSubjectKeyIdentifier(): SubjectKeyIdentifier =
    x509BCUtils.createSubjectKeyIdentifier(this)
}
