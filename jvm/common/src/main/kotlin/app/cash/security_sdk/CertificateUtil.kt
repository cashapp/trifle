package app.cash.security_sdk

import com.squareup.protos.cash.s2dk.api.alpha.MobileCertificateRequest
import okio.ByteString
import java.math.BigInteger

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
}
