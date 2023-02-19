package app.cash.security_sdk

import app.cash.security_sdk.internal.providers.TrifleContentVerifierProvider
import org.bouncycastle.pkcs.PKCS10CertificationRequest

data class CertificateRequest(
  val csr: PKCS10CertificationRequest
) {
  fun verify(contentVerifierProvider: TrifleContentVerifierProvider): Boolean {
    return csr.isSignatureValid(contentVerifierProvider)
  }
}
