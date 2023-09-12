package app.cash.trifle.validators

import app.cash.trifle.Certificate
import app.cash.trifle.CertificateRequest
import app.cash.trifle.TrifleErrors
import app.cash.trifle.validators.CertificateUtil.toX509Certificate
import org.bouncycastle.cert.X509CertificateHolder
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException
import java.util.Date

sealed interface CertificateValidator {
  /**
   * Validates if the provided Trifle Certificate is within the validity window of the date.
   *
   * @param date - The date to use for verification against certificates' validity windows. If null,
   *   the current time is used.
   */
  fun validate(date: Date?): Result<Unit>

  /**
   * Validates if the provided Trifle Certificate matches the CSR that we have.
   *
   * @param certificateRequest - request used to generate this certificate.
   *
   * @return - [Result] indicating [Result.isSuccess] or [Result.isFailure]:
   * - success value is expressed as a [Unit] (Nothing)
   * - failure value is expressed as a [TrifleErrors.CSRMismatch] if attributes are mismatched
   */
  fun validate(certificateRequest: CertificateRequest): Result<Unit>

  /**
   * X.509 specific implementation for validating a Trifle Certificate.
   */
  class X509CertificateValidator(certificate: Certificate) : CertificateValidator {
    private val x509Certificate = certificate.toX509Certificate()
    private val x509CertHolder = X509CertificateHolder(certificate.certificate)
    override fun validate(date: Date?): Result<Unit> =
      try {
        x509Certificate.checkValidity(date ?: Date())
        Result.success(Unit)
      } catch (e: CertificateExpiredException) {
        Result.failure(TrifleErrors.ExpiredCertificate)
      } catch (e: CertificateNotYetValidException) {
        Result.failure(TrifleErrors.NotValidYetCertificate)
      }

    override fun validate(certificateRequest: CertificateRequest): Result<Unit> {
      when (certificateRequest) {
        is CertificateRequest.PKCS10Request -> {
          // Certificate chain matches, check with certificate request.
          // TODO(dcashman): Check other attributes as well.
          if (certificateRequest.pkcs10Req.subject != x509CertHolder.subject ||
            certificateRequest.pkcs10Req.subjectPublicKeyInfo != x509CertHolder.subjectPublicKeyInfo
          ) {
            return Result.failure(TrifleErrors.CSRMismatch)
          }
        }
      }
      return Result.success(Unit)
    }
  }
}
