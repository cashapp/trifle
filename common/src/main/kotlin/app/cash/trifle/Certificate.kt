package app.cash.trifle

import okio.ByteString.Companion.toByteString
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
