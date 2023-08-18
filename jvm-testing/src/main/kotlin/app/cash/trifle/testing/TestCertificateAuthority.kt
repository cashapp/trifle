package app.cash.trifle.testing

import app.cash.trifle.Certificate
import app.cash.trifle.Trifle
import app.cash.trifle.testing.Fixtures.EC_SPEC
import app.cash.trifle.testing.Fixtures.GENERATOR
import app.cash.trifle.testing.Fixtures.RAW_ECDSA_P256_KEY_TEMPLATE
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.signature.SignatureConfig
import java.security.SecureRandom
import java.time.Period
import kotlin.random.Random

/**
 * Trifle Certificate Authority used strictly for testing.
 */
data class TestCertificateAuthority(
  private val certAuthorityName: String = Random.nextInt().toString(),
  private val validityPeriod: Period = Period.ofDays(1)
) {
  private val certificateAuthority: Trifle.CertificateAuthority
  val rootCertificate: Certificate

  init {
    SignatureConfig.register()
    GENERATOR.initialize(EC_SPEC, SecureRandom())
    certificateAuthority = Trifle.CertificateAuthority(
      KeysetHandle.generateNew(RAW_ECDSA_P256_KEY_TEMPLATE)
    )
    rootCertificate = certificateAuthority.createRootSigningCertificate(
      certAuthorityName, validityPeriod
    )
  }

  fun createTestEndEntity(
    entityName: String = Random.nextInt().toString(),
    validity: Period? = null
  ): TestEndEntity {
    val endEntity = Trifle.EndEntity(GENERATOR.genKeyPair())
    val certRequest = endEntity.createCertRequest(entityName)
    val certificate: Certificate = if (validity == null) {
      certificateAuthority.signCertificate(rootCertificate, certRequest)
    } else {
      certificateAuthority.signCertificate(rootCertificate, certRequest, validity)
    }
    return TestEndEntity(endEntity, listOf(certificate, rootCertificate), certRequest)
  }
}
