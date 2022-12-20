package app.cash.security_sdk

import org.bouncycastle.pkcs.PKCS10CertificationRequest

data class CertificateRequest(
  val csr: PKCS10CertificationRequest,
)
