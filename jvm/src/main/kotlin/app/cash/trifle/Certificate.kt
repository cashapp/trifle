package app.cash.trifle

import app.cash.trifle.Certificate.Companion.VerifyResult.Reason.CSR_MISMATCH
import app.cash.trifle.Certificate.Companion.VerifyResult.Reason.EXPIRED
import app.cash.trifle.Certificate.Companion.VerifyResult.Reason.INCORRECT_SIGNATURE
import app.cash.trifle.Certificate.Companion.VerifyResult.Reason.SUCCESS
import app.cash.trifle.Certificate.Companion.VerifyResult.Reason.UNSPECIFIED_FAILURE
import app.cash.trifle.CertificateRequest.PKCS10Request
import app.cash.trifle.internal.validators.CertChainValidatorFactory
import okio.ByteString.Companion.toByteString
import org.bouncycastle.cert.X509CertificateHolder
import java.security.cert.CertPathValidatorException
import java.security.cert.CertPathValidatorException.BasicReason
import java.util.Date
import app.cash.trifle.protos.api.alpha.Certificate as CertificateProto

/**
 * Class representing a Trifle certificate.
 *
 * @property certificate - bytearray encoding of an x.509 certificate. Subject to change.
 */
data class Certificate internal constructor(
  internal val certificate: ByteArray,
  internal val version: Int = CERTIFICATE_VERSION,
) {
  /**
   * Serialize the Trifle Certificate to a ByteArray so clients can store it for inclusion in future
   * signing operations or provide it as a root certificate to verify signed messages.
   */
  fun serialize(): ByteArray =
    CertificateProto(
      certificate = certificate.toByteString(),
      version = CERTIFICATE_VERSION
    ).encode()

  /**
   * Verify that the provided certificate matches what we expected.
   * It matches the CSR that we have and the root cert is what
   * we expect.
   *
   * @param certificateRequest -  request used to generate this certificate
   * @param ancestorCertificateChain - list of certificates preceding *this* one.  Namely, the first
   *   entry should be the certificate corresponding to the issuer of this certificate, and each
   *   thereafter should be the issuer of the one before it.
   * @param anchorCertificate - the trust anchor against which we would like to verify the
   *   ancestorCertificateChain. This may be the terminal (root) certificate of the chain or may be
   *   an intermediate certificate in the chain which is already trusted.
   * @param date - The date to use for verification against certificates' validity windows. If null,
   *   the current time is used.
   *
   * @return - Boolean status indicating error in validation or success.
   *   Exception thrown if certificate is not signed by the given root certificate, or
   *   certificate is expired or invalid or
   *   if the information present doesn't match that of the certificateRequest.
   */
  fun verify(
    certificateRequest: CertificateRequest,
    ancestorCertificateChain: List<Certificate>,
    anchorCertificate: Certificate,
    date: Date? = null
  ): Boolean {
    // First check to see if the certificate chain matches
    val validator = CertChainValidatorFactory.get(anchorCertificate, date)
    validator.validate(listOf(this) + ancestorCertificateChain)

    // Certificate chain matches, check with certificate request.
    // TODO(dcashman): Check other attributes as well.
    val x509Certificate = X509CertificateHolder(certificate)
    return when (certificateRequest) {
      is PKCS10Request -> {
        if (certificateRequest.pkcs10Req.subject == x509Certificate.subject
          && certificateRequest.pkcs10Req.subjectPublicKeyInfo == x509Certificate.subjectPublicKeyInfo
        ) {
          return true
        } else {
          throw CSRMismatchException("Trifle certificate does not match CSR")
        }
      }
    }
    throw UnSpecifiedFailureException("Unspecified Trifle verification failure")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Certificate

    if (!certificate.contentEquals(other.certificate)) return false
    if (version != other.version) return false

    return true
  }

  override fun hashCode(): Int {
    var result = certificate.contentHashCode()
    result = 31 * result + version
    return result
  }

  companion object {
    internal const val CERTIFICATE_VERSION: Int = 0

    data class VerifyResult constructor(val reason: Reason, val msg: String? = "") {

      override fun toString(): String {
        return "$reason ${msg.orEmpty()}"
      }

      enum class Reason {
        SUCCESS,
        UNSPECIFIED_FAILURE,
        EXPIRED,
        INCORRECT_SIGNATURE,
        CSR_MISMATCH
      }
    }

    /**
     * Create a Trifle Certificate from its binary representation, which is a serialized proto for
     * this purpose containing an x.509 cert.
     */
    fun deserialize(bytes: ByteArray): Certificate {
      val certProto = CertificateProto.ADAPTER.decode(bytes)
      check(certProto.version == CERTIFICATE_VERSION)

      return Certificate(
        certificate = checkNotNull(certProto.certificate).toByteArray(),
        version = certProto.version,
      )
    }
  }
}
