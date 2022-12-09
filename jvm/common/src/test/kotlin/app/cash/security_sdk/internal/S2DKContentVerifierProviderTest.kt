package app.cash.security_sdk.internal

import app.cash.security_sdk.CertificateUtil
import app.cash.security_sdk.SigningCert
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.signature.SignatureConfig
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Period

internal class S2DKContentVerifierProviderTest {
  private lateinit var signingCert: SigningCert
  private lateinit var publicKeySign: PublicKeySign
  private val data = byteArrayOf(0x00, 0x01, 0x02, 0x03)

  @BeforeEach
  fun setUp() {
    SignatureConfig.register()
    val ed25519PrivateKeysetHandle = KeysetHandle.generateNew(KeyTemplates.get("ED25519"))
    signingCert = CertificateUtil.createRootSigningCertificate(
      "entity", Period.ofDays(1), ed25519PrivateKeysetHandle
    )
    publicKeySign = ed25519PrivateKeysetHandle.getPrimitive(PublicKeySign::class.java)
  }

  @Test
  fun `test get() returns content verifier with appropriate key`() {
    val tinkContentVerifier = S2DKContentVerifierProvider(signingCert).get(
      AlgorithmIdentifier(
        ASN1ObjectIdentifier(TinkContentSigner.ED25519_OID)
      )
    )
    tinkContentVerifier.outputStream.write(data)
    assertTrue(tinkContentVerifier.verify(publicKeySign.sign(data)))
  }

  @Test
  fun `test getAssociatedCertificate`() {
    assertEquals(
      X509CertificateHolder(signingCert.certificate.toByteArray()),
      S2DKContentVerifierProvider(signingCert).associatedCertificate
    )
  }
}
