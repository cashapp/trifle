package app.cash.security_sdk

import app.cash.security_sdk.internal.signers.TrifleContentSigner
import com.squareup.protos.cash.s2dk.api.alpha.MobileCertificateRequest
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import java.math.BigInteger
import java.time.Instant
import java.time.Period
import java.util.Date

/**
 * Server trifle utility class for certificate enrollment.
 */
object CertificateUtil {
  private const val MOBILE_CERTIFICATE_REQUEST_VERSION: UInt = 0u

  // Validity time for device-certificate, currentl scoped to 180 days, based entirely on intuition.
  private const val MOBILE_CERTIFICATE_VALIDITY_PERIOD_DAYS: Int = 180

  /**
   * Converts the given serialized MobileCertRequest into a CertificateRequest. This enables clients
   * to issue certificate requests from devices using the Mobile S2DK and have those requests be
   * augmented by additional properties provided by the certifiacte authority doing verification.
   *
   * Note: Originally the intention was to have the certificate authority populate the subject info,
   * but currently this is not used, with X.509 attributes to be used instead. This effectively
   * makes this function a simple deserialization function.
   *
   * @param payload byte string that decodes to a MobileCertificateRequest
   */
  fun mobileCertRequestToCertRequest(
    payload: ByteString
  ): CertificateRequest {
    // The byte string is simply a serialized proto containing a PKCS10 Certificate request and a
    // version.  Make sure the version is supported and then process the PKCS10 Certificate request.
    val s2dkMobileCertRequest = MobileCertificateRequest.ADAPTER.decode(payload)
    check(s2dkMobileCertRequest.version!!.toUInt() == MOBILE_CERTIFICATE_REQUEST_VERSION)

    return CertificateRequest(
      PKCS10CertificationRequest(s2dkMobileCertRequest.pkcs10_request!!.toByteArray())
    )
  }

  /**
   * Signs CertificateRequest using the provided trifle content signer and issuing certificate.
   *
   * @param issuerCertificate trifle certificate associated with the signer of this cert.
   * @param certificateRequest certificate request used to generate a new certificate
   * @param contentSigner content signer used to generate the signature validating the certificate
   */
  fun signCertificate(
    issuerCertificate: SigningCert,
    certificateRequest: CertificateRequest,
    contentSigner: TrifleContentSigner
  ): SigningCert {
    val creationTime = Instant.now()
    val pkcs10Request = certificateRequest.csr
    val issuerCertHolder = X509CertificateHolder(issuerCertificate.certificate.toByteArray())
    val signedCert = X509v3CertificateBuilder(
        issuerCertHolder.subject,
        BigInteger.valueOf(creationTime.toEpochMilli()),
        Date.from(creationTime),
        Date.from(creationTime.plus(Period.ofDays(MOBILE_CERTIFICATE_VALIDITY_PERIOD_DAYS))),
        pkcs10Request.subject,
        pkcs10Request.subjectPublicKeyInfo
      ).build(contentSigner)

    return SigningCert(signedCert.encoded.toByteString())
  }

  /**
   * Creates a self-signed certificate with the provided key and name.
   *
   * @param entityName the name with which we'll associate the public key.
   * @param validityPeriod the length of time for which this certificate should be accepted after
   * issuance.
   * @param contentSigner content signer used to generate the signature for creating
   *  the self-signed root certificate
   */
  fun createRootSigningCertificate(
    entityName: String,
    validityPeriod: Period,
    contentSigner: TrifleContentSigner
  ): SigningCert {
    // Trifle Certificates are just wrappers around X.509 certificates. Create one with the
    // given name.
    val subjectName = X500Name("CN=$entityName")
    val creationTime = Instant.now()
    val signedCert = X509v3CertificateBuilder(
      subjectName,
      BigInteger.ONE,
      Date.from(creationTime),
      Date.from(creationTime.plus(validityPeriod)),
      subjectName,
      contentSigner.subjectPublicKeyInfo()
    ).build(contentSigner)

    return SigningCert(signedCert.encoded.toByteString())
  }

  /**
   * Create a mobile signing certificate request with a Trifle Content Signer.
   *
   * @param entityName the name with which we'll associate the public key.
   * @param contentSigner content signer used to generate the signature validating the
   *  Certificate Request
   */
  fun createMobileCertRequest(
    entityName: String,
    contentSigner: TrifleContentSigner
  ): MobileCertificateRequest {
    val subjectName = X500Name("CN=$entityName")
    val subjectPublicKeyInfo = contentSigner.subjectPublicKeyInfo()
    val pkcS10CertificationRequest =
      PKCS10CertificationRequestBuilder(
        subjectName,
        subjectPublicKeyInfo
      ).build(contentSigner)
    return MobileCertificateRequest(
      MOBILE_CERTIFICATE_REQUEST_VERSION.toInt(),
      pkcS10CertificationRequest.encoded.toByteString()
    )
  }
}
