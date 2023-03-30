package app.cash.trifle

import app.cash.trifle.internal.providers.JCAContentVerifierProvider
import app.cash.trifle.protos.api.alpha.MobileCertificateRequest
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.bouncycastle.pkcs.PKCS10CertificationRequest

sealed interface CertificateRequest {
  class PKCS10Request internal constructor(encoded: ByteString) : CertificateRequest {
    val pkcs10Req = PKCS10CertificationRequest(encoded.toByteArray())
  }

  fun serialize(): ByteArray =
    when (this) {
      is PKCS10Request -> MobileCertificateRequest(
        version = MOBILE_CERTIFICATE_REQUEST_VERSION,
        pkcs10_request = pkcs10Req.encoded.toByteString()
      ).encode()
    }

  fun verify(): Boolean {
    return when (this) {
      is PKCS10Request -> pkcs10Req.isSignatureValid(
        JCAContentVerifierProvider(pkcs10Req.subjectPublicKeyInfo)
      )
    }
  }

  companion object {
    internal const val MOBILE_CERTIFICATE_REQUEST_VERSION: Int = 0

    // Validity time for device-certificate, currently scoped to 180 days, based entirely on
    // intuition. This is not currently used, and should be configurable by the client when usage is
    // desired.
    internal const val MOBILE_CERTIFICATE_VALIDITY_PERIOD_DAYS: Int = 180

    /**
     * Converts the given serialized MobileCertRequest into a CertificateRequest. This enables clients
     * to issue certificate requests from devices using the Mobile SDK and have those requests be
     * augmented by additional properties provided by the certificate authority doing verification.
     *
     * Note: Originally the intention was to have the certificate authority populate the subject info,
     * but currently this is not used, with X.509 attributes to be used instead. This effectively
     * makes this function a simple deserialization function.
     *
     * @param bytes byte array that decodes to a MobileCertificateRequest
     */
    fun deserialize(bytes: ByteArray): CertificateRequest {
      // The byte string is simply a serialized proto containing a PKCS10 Certificate request and a
      // version.  Make sure the version is supported and then process the PKCS10 Certificate request.
      val mobileCertRequest = MobileCertificateRequest.ADAPTER.decode(bytes)
      check(mobileCertRequest.version!! == MOBILE_CERTIFICATE_REQUEST_VERSION)

      return if (mobileCertRequest.pkcs10_request != null) {
        PKCS10Request(mobileCertRequest.pkcs10_request)
      } else {
        throw UnsupportedOperationException("Certificate request type not recognized")
      }
    }
  }
}
