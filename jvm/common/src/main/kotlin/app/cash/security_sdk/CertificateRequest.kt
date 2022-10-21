package app.cash.security_sdk

import okio.ByteString

data class CertificateRequest(
  val csr: ByteString,
  val subjectInfo: Map<String, String>,
)
