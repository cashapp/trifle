package app.cash.security_sdk

import app.cash.security_sdk.internal.TinkContentSigner
import app.cash.security_sdk.internal.toSubjectPublicKeyInfo
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.squareup.protos.cash.s2dk.api.alpha.MobileCertificateRequest
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509v3CertificateBuilder
import java.math.BigInteger
import java.time.Instant
import java.time.Period
import java.util.Date

/**
 * Server S2DK utility class for certificate enrollment.
 */
object CertificateUtil {
  /**
   * Produces the CertificateRequest from decoding the byte blob and ingesting the subject info.
   *
   * @param payload byte string that decodes to a MobileCertificateRequest
   * @param subjectInfo contains additional required information for the certificate
   */
  fun getCertificateRequest(
    payload: ByteString,
    subjectInfo: Map<String, String>
  ): CertificateRequest = CertificateRequest(
    MobileCertificateRequest.ADAPTER.decode(payload).encodeByteString(),
    subjectInfo
  )

  /**
   * Signs CertificateRequest using the private key generated from the given keyset source.
   *
   * @param signingKeySecret secret value used to seed the signing private key
   * @param certificateRequest certificate request used to generate a new certificate
   */
  fun signCertificate(
    signingKeySecret: BigInteger,
    certificateRequest: CertificateRequest
  ): SigningCert = SigningCert(ByteString.EMPTY)

  /**
   * Creates a self-signed certificate with the provided key and name.
   *
   * @param entityName the name with which we'll associate the public key.
   * @param validityPeriod the length of time for which this certificate should be accepted after
   * issuance.
   * @param tinkSigningKeyset the Tink key used to sign the certificate.
   */
  fun createRootSigningCertificate(
    entityName: String,
    validityPeriod: Period,
    tinkSigningKeyset: KeysetHandle,
  ): SigningCert {
    // s2dk Certificates are just wrappers around X.509 certificates. Create one with the
    // given name.
    val subjectName = X500Name("CN=$entityName")
    val creationTime = Instant.now()
    val cert = X509v3CertificateBuilder(
      subjectName,
      BigInteger.ONE,
      Date.from(creationTime),
      Date.from(creationTime.plus(validityPeriod)),
      subjectName,
      tinkSigningKeyset.toSubjectPublicKeyInfo()
    )
    val signedCert =
      cert.build(TinkContentSigner(tinkSigningKeyset.getPrimitive(PublicKeySign::class.java)))

    return SigningCert(signedCert.encoded.toByteString(0, signedCert.encoded.size))
  }
}
