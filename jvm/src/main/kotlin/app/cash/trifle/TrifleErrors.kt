package app.cash.trifle

class NoTrustAnchorException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class ExpiredCertificateException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class IncorrectSignatureException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class UnSpecifiedFailureException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
