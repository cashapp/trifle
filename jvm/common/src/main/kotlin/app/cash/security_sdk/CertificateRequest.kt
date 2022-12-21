package app.cash.security_sdk

import app.cash.security_sdk.internal.S2DKContentVerifierProvider
import org.bouncycastle.pkcs.PKCS10CertificationRequest

data class CertificateRequest(
  val csr: PKCS10CertificationRequest
) {
  fun verify(): Boolean {
    return csr.isSignatureValid(S2DKContentVerifierProvider(csr.subjectPublicKeyInfo))
  }
}
