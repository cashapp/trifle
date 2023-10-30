package app.cash.trifle

import app.cash.trifle.TrifleErrors.CSRMismatch
import app.cash.trifle.TrifleErrors.ExpiredCertificate
import app.cash.trifle.TrifleErrors.NoTrustAnchor
import app.cash.trifle.TrifleErrors.NotValidYetCertificate
import app.cash.trifle.testing.TestCertificateAuthority
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID

class TrifleTest {
  @JvmField
  @Rule
  val thrown: ExpectedException = ExpectedException.none()

  private val trifle = Trifle("app.cash.trifle.keys")
  private lateinit var keyHandle: KeyHandle
  @Before fun setUp() {
    keyHandle = trifle.generateKeyHandle()
  }

  @Test fun testGenerateKeyHandle() {
    assertNotNull(keyHandle)
    val uuid = keyHandle.tag.substringAfterLast('.')
    assertNotNull(UUID.fromString(uuid))
  }

  @Test fun testIsValid_forCreatedKeyHandle_returnsTrue() {
    assertTrue(trifle.isValid(keyHandle))
  }

  @Test fun testIsValid_forDeletedKeyHandle_returnsFalse() {
    trifle.delete(keyHandle)
    assertFalse(trifle.isValid(keyHandle))
  }

  @Test fun testDeleteKeyHandle() {
    trifle.delete(keyHandle)
  }

  @Test
  fun testVerify_succeeds() {
    val result = trifle.verifyChain(
      certificateChain = endEntity.certChain
    )
    assertTrue(result.isSuccess)
  }

  @Test
  fun testVerifyCertificateValidity_succeeds() {
    val result = trifle.verifyValidity(
      endEntity.certificate
    )
    assertTrue(result.isSuccess)
  }

  @Test
  fun testVerifyAttributes_succeeds() {
    val result = trifle.verifyCertRequestResponse(
      endEntity.certificate,
      endEntity.certRequest
    )
    assertTrue(result.isSuccess)
  }

  @Test
  fun testVerify_failsWithNoTrustAnchorForADifferentRootCertificate() {
    val result = trifle.verifyChain(
      certificateChain = listOf(endEntity.certificate) + otherEndEntity.certChain.drop(1)
    )
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is NoTrustAnchor)
  }

  @Test
  fun testVerify_failsWithExpiredCertificateForAnExpiredCertificate() {
    val result = trifle.verifyChain(
      certificateChain = endEntity.certChain,
      date = Date.from(Instant.now().plus(Duration.ofDays(365)))
    )
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is ExpiredCertificate)
  }

  @Test
  fun testVerify_failsWithExpiredCertificateForAnExpiredStoredCertificate() {
    val result = trifle.verifyValidity(
      endEntity.certificate,
      Date.from(Instant.now().plus(Duration.ofDays(365)))
    )
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is ExpiredCertificate)
  }

  @Test
  fun testVerify_failsWithNotYetValidCertificateForAStoredCertificateYetToBeValid() {
    val result = trifle.verifyValidity(
      endEntity.certificate,
      date = Date.from(Instant.now().minus(Duration.ofDays(1)))
    )
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is NotValidYetCertificate)
  }

  @Test
  fun testVerifyAttributes_failsWithCSRMismatch() {
    val result = trifle.verifyCertRequestResponse(
      endEntity.certificate,
      otherEndEntity.certRequest
    )
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is CSRMismatch)
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority("issuingEntity")
    private val otherCertificateAuthority = TestCertificateAuthority("otherIssuingEntity")
    private val endEntity = certificateAuthority.createTestEndEntity("entity")
    private val otherEndEntity = otherCertificateAuthority.createTestEndEntity("otherEntity")
  }
}
