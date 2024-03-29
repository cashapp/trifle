package app.cash.trifle.testing

import app.cash.trifle.Certificate
import app.cash.trifle.delegates.CertificateAuthority
import app.cash.trifle.delegates.EndEntity
import app.cash.trifle.signers.tink.TinkContentSigner
import app.cash.trifle.testing.Fixtures.EC_SPEC
import app.cash.trifle.testing.Fixtures.GENERATOR
import app.cash.trifle.testing.Fixtures.RAW_ECDSA_P256_KEY_TEMPLATE
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.signature.SignatureConfig
import java.security.SecureRandom
import java.time.Duration
import kotlin.random.Random

/**
 * Trifle Certificate Authority used strictly for testing.
 */
data class TestCertificateAuthority(
  private val certAuthorityName: String = Random.nextInt().toString(),
  private val validityPeriod: Duration = Duration.ofDays(1)
) {
  private val certificateAuthority: CertificateAuthority
  val rootCertificate: Certificate

  init {
    SignatureConfig.register()
    GENERATOR.initialize(EC_SPEC, SecureRandom())
    certificateAuthority = CertificateAuthority(
      TinkContentSigner(KeysetHandle.generateNew(RAW_ECDSA_P256_KEY_TEMPLATE))
    )
    rootCertificate = certificateAuthority.createRootSigningCertificate(
      certAuthorityName, validityPeriod
    )
  }

  fun createTestEndEntity(
    entityName: String = Random.nextInt().toString(),
    validity: Duration? = null
  ): TestEndEntity {
    val endEntity = EndEntity.Factory.get(GENERATOR.genKeyPair())
    val certRequest = endEntity.createCertRequest(entityName)
    val certificate: Certificate = if (validity == null) {
      certificateAuthority.signCertificate(rootCertificate, certRequest)
    } else {
      certificateAuthority.signCertificate(rootCertificate, certRequest, validity)
    }
    return TestEndEntity(endEntity, listOf(certificate, rootCertificate), certRequest)
  }
}
