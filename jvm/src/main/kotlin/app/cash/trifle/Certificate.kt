package app.cash.trifle

import okio.ByteString.Companion.toByteString
import app.cash.trifle.protos.api.alpha.Certificate as CertificateProto

/*
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
      version = CERTIFICATE_VERSION,
      certificate = certificate.toByteString()
    ).encode()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Certificate

    if (!certificate.contentEquals(other.certificate)) return false

    return true
  }

  override fun hashCode(): Int {
    return certificate.contentHashCode()
  }

  companion object {
    internal const val CERTIFICATE_VERSION: Int = 0

    /**
     * Create a Trifle Certificate from its binary representation, which is a serialized proto for
     * this purpose containing an x.509 cert.
     */
    fun deserialize(bytes: ByteArray): Certificate {
      val certProto = CertificateProto.ADAPTER.decode(bytes)
      return Certificate(
        certificate = checkNotNull(certProto.certificate).toByteArray(),
        version = checkNotNull(certProto.version),
      )
    }
  }
}
