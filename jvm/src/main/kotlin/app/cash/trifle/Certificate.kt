package app.cash.trifle

import okio.ByteString.Companion.toByteString
import app.cash.trifle.protos.api.alpha.Certificate as CertificateProto

/*
 * Class representing a Trifle certificate.
 *
 * @property certificate - bytearray encoding of an x.509 certificate. Subject to change.
 */
class Certificate internal constructor(internal val certificate: ByteArray) {
  /**
   * Serialize the Trifle Certificate to a ByteArray so clients can store it for inclusion in future
   * signing operations or provide it as a root certificate to verify signed messages.
   */
  fun serialize(): ByteArray =
    CertificateProto(
      version = CERTIFICATE_VERSION,
      certificate = certificate.toByteString())
      .encode()

  companion object {
    private const val CERTIFICATE_VERSION: Int = 0

    /**
     * Create a Trifle Certificate from its binary representation, which is a serialized proto for
     * this purpose containing an x.509 cert.
     */
    fun deserialize(bytes: ByteArray): Certificate =
      Certificate(CertificateProto.ADAPTER.decode(bytes).certificate!!.toByteArray())
  }
}
