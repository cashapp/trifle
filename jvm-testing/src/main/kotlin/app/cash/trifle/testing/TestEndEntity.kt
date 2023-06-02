package app.cash.trifle.testing

import app.cash.trifle.Certificate
import app.cash.trifle.CertificateRequest
import app.cash.trifle.SignedData
import app.cash.trifle.Trifle

/**
 * Trifle End Entity used strictly for testing.
 */
data class TestEndEntity internal constructor(
  private val endEntity: Trifle.EndEntity,
  val certChain: List<Certificate>,
  val certRequest: CertificateRequest,
  val certificate: Certificate = certChain.first()
) {
  fun createSignedData(
    data: ByteArray,
    certChain: List<Certificate> = emptyList()
  ): SignedData = endEntity.createSignedData(data, certChain.ifEmpty { this.certChain })
}
