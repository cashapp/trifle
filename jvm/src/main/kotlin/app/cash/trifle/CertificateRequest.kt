package app.cash.trifle

import app.cash.trifle.internal.providers.TrifleContentVerifierProvider
import org.bouncycastle.pkcs.PKCS10CertificationRequest

data class CertificateRequest(
  val csr: PKCS10CertificationRequest
) {
  fun verify(contentVerifierProvider: TrifleContentVerifierProvider): Boolean {
    return csr.isSignatureValid(contentVerifierProvider)
  }
}
