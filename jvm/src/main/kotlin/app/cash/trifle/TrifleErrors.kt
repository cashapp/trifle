package app.cash.trifle

sealed class TrifleErrors(message: String, cause: Throwable? = null) : Exception(message, cause) {
  object NoTrustAnchor : TrifleErrors("No acceptable Trifle trust anchor found")
  object InvalidCertPath : TrifleErrors("Invalid Trifle certificate path found")
  object ExpiredCertificate : TrifleErrors("Expired Trifle certificate")
  object InvalidSignature : TrifleErrors("Invalid Trifle signature")
  object CSRMismatch : TrifleErrors("Trifle certificate does not match CSR")
  class UnspecifiedFailure(message: String, cause: Throwable) : TrifleErrors(message, cause)
}
