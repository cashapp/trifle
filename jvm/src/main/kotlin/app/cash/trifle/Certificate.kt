package app.cash.trifle

import app.cash.trifle.CertificateRequest.PKCS10Request
import app.cash.trifle.internal.validators.CertChainValidatorFactory
import okio.ByteString.Companion.toByteString
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import java.security.cert.X509Certificate
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
   * @param certificateChain - list of certificates with each subsequent certificate signing the one
   *   before it, starting with the parent of this certificate.
   * @return - false if certificate is not signed by the given root certificate, or if the
   *   information present doesn't match that of the certificateRequest.
   *   //TODO(dcashman): Return errors specific to each type of failure so clients can remediate.
   */
  fun verify(
    certificateRequest: CertificateRequest,
    certificateChain: List<Certificate>,
    rootCertificate: Certificate
  ): Boolean {
    // First check to see if the certificate chain matches
    val validator = CertChainValidatorFactory.get(rootCertificate)
    if (!validator.validate(listOf(this) + certificateChain)) return false

    // Certificate chain matches, check with certificate request.
    // TODO(dcashman): Check other attributes as well.
    val x509Certificate = X509CertificateHolder(certificate)
    return when (certificateRequest) {
      is PKCS10Request -> {
        certificateRequest.pkcs10Req.subject.equals(x509Certificate.subject) && certificateRequest.pkcs10Req.subjectPublicKeyInfo.equals(
          x509Certificate.subjectPublicKeyInfo
        )
      }
      else -> false
    }
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
