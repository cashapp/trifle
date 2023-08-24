package app.cash.trifle

import app.cash.trifle.TrifleErrors.ExpiredCertificate
import app.cash.trifle.TrifleErrors.NoTrustAnchor
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

class TrifleApiTest {
  @JvmField
  @Rule
  val thrown: ExpectedException = ExpectedException.none()

  private lateinit var keyHandle: KeyHandle
  @Before fun setUp() {
    keyHandle = TrifleApi.generateKeyHandle("test-alias")
  }

  @Test fun testGenerateKeyHandle() {
    assertNotNull(keyHandle)
  }

  @Test fun testIsValid_forCreatedKeyHandle_returnsTrue() {
    assertTrue(TrifleApi.isValid(keyHandle))
  }

  @Test fun testIsValid_forDeletedKeyHandle_returnsFalse() {
    TrifleApi.delete(keyHandle)
    assertFalse(TrifleApi.isValid(keyHandle))
  }

  @Test fun testDeleteKeyHandle() {
    TrifleApi.delete(keyHandle)
  }

  @Test
  fun `testVerify_failsWithNoTrustAnchorForADifferentRootCertificate`() {
    val result = TrifleApi.verify(
      endEntity.certificate,
      endEntity.certRequest,
      otherEndEntity.certChain.drop(1),
      otherCertificateAuthority.rootCertificate
    )
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is NoTrustAnchor)
  }

  @Test
  fun testVerify_failsWithExpiredCertificateForAnExpiredCertificate() {
    val result = TrifleApi.verify(
      endEntity.certificate,
      endEntity.certRequest,
      endEntity.certChain.drop(1),
      certificateAuthority.rootCertificate,
      Date.from(Instant.now().plus(Duration.ofDays(365)))
    )
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is ExpiredCertificate)
  }

  companion object {
    private val certificateAuthority = TestCertificateAuthority("issuingEntity")
    private val otherCertificateAuthority = TestCertificateAuthority("otherIssuingEntity")
    private val endEntity = certificateAuthority.createTestEndEntity("entity")
    private val otherEndEntity = otherCertificateAuthority.createTestEndEntity("otherEntity")
  }
}
