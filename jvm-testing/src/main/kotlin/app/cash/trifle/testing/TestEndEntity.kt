package app.cash.trifle.testing

import app.cash.trifle.Certificate
import app.cash.trifle.CertificateRequest
import app.cash.trifle.SignedData
import app.cash.trifle.delegates.EndEntity
import app.cash.trifle.extensions.CertificateChain

/**
 * Trifle End Entity used strictly for testing.
 */
data class TestEndEntity internal constructor(
  private val endEntity: EndEntity,
  val certChain: CertificateChain,
  val certRequest: CertificateRequest,
  val certificate: Certificate = certChain.first()
) {
  fun createSignedData(
    data: ByteArray,
    certChain: CertificateChain = emptyList()
  ): SignedData = endEntity.createSignedData(data, certChain.ifEmpty { this.certChain })
}
