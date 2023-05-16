package app.cash.trifle

import app.cash.trifle.delegate.TinkDelegate
import app.cash.trifle.internal.providers.JCAContentVerifierProvider
import app.cash.trifle.internal.util.TestFixtures.CERT_ANCHOR
import app.cash.trifle.internal.util.TestFixtures.CERT_REQUEST
import app.cash.trifle.internal.util.TestFixtures.RAW_ECDSA_P256_KEY_TEMPLATE
import app.cash.trifle.internal.util.TestFixtures.SIGNED_DATA
import app.cash.trifle.protos.api.alpha.MobileCertificateRequest
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.signature.SignatureConfig
import okio.ByteString.Companion.encodeUtf8
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import java.lang.IllegalStateException
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.time.Duration
import java.time.Period

internal class TrifleTest {
  @Nested
  @DisplayName("Certificate Authority#createRootSigningCertificate() Tests")
  inner class CreateRootSigningCertificateTests {
    private val certHolder = X509CertificateHolder(
      certificateAuthority.createRootSigningCertificate(
        "entity", Period.ofDays(1)
      ).certificate
    )

    @Test
    fun `test validity period`() {
      val duration =
        Duration.ofMillis(
          certHolder.notAfter.toInstant()
            .minusMillis(certHolder.notBefore.toInstant().toEpochMilli())
            .toEpochMilli()
        )
      assertEquals(1, duration.toDays())
    }

    @Test
    fun `test certificate entity`() {
      assertEquals("CN=entity", certHolder.subject.toString())
    }

    @Test
    fun `test certificate issuer`() {
      assertEquals("CN=entity", certHolder.issuer.toString())
    }

    @Test
    fun `test isSignatureValid() returns true for certificate signature`() {
      // Self-signed cert should verify when presented with itself.
      assertTrue(
        certHolder.isSignatureValid(
          JCAContentVerifierProvider(certHolder.subjectPublicKeyInfo)
        )
      )
    }

    @Test
    fun `test isSignatureValid() returns false due to a different issuer`() {
      // Different key should not verify
      val otherCertificateAuthority = Trifle.CertificateAuthority(
        TinkDelegate(
          KeysetHandle.generateNew(RAW_ECDSA_P256_KEY_TEMPLATE)
        )
      )
      val otherCert = otherCertificateAuthority.createRootSigningCertificate(
        "entity", Period.ofDays(1),
      )

      assertFalse(
        certHolder.isSignatureValid(
          JCAContentVerifierProvider(
            X509CertificateHolder(
              otherCert.certificate
            ).subjectPublicKeyInfo
          )
        )
      )
    }
  }

  @Nested
  @DisplayName("Certificate Authority#signCertificate() Tests")
  inner class SignCertificateTests {
    // Create local copy of issuingCert for use in verifying signature.
    private val issuingCert = certificateAuthority.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(365)
    )

    // Extract the x.509 certificate from our object.
    private val certHolder = X509CertificateHolder(
      certificateAuthority.signCertificate(issuingCert, CERT_REQUEST).certificate
    )

    @Test
    fun `test validity period`() {
      val duration =
        Duration.ofMillis(
          certHolder.notAfter.toInstant()
            .minusMillis(certHolder.notBefore.toInstant().toEpochMilli())
            .toEpochMilli()
        )
      assertEquals(180, duration.toDays())
    }

    @Test
    fun `test certificate entity`() {
      assertEquals("CN=entity", certHolder.subject.toString())
    }

    @Test
    fun `test certificate issuer`() {
      assertEquals("CN=issuingEntity", certHolder.issuer.toString())
    }

    @Test
    fun `test isSignatureValid() returns true for properly signed certificate`() {
      val issuingCertHolder = X509CertificateHolder(issuingCert.certificate)

      // Cert should verify when presented with issuing cert
      assertTrue(
        certHolder.isSignatureValid(
          JCAContentVerifierProvider(issuingCertHolder.subjectPublicKeyInfo)
        )
      )
    }

    @Test
    fun `test isSignatureValid() returns false due to incorrectly signed certificate`() {
      // Different key should not verify (in this case it is not self-signed)
      assertFalse(
        certHolder.isSignatureValid(JCAContentVerifierProvider(certHolder.subjectPublicKeyInfo))
      )
    }
  }

  @Nested
  @DisplayName("End Entity#createCertRequest() Tests")
  inner class CreateCertificateRequestTests {
    // Create mobile signing certificate, which represents the request which would come from a
    // mobile client.
    private val certRequest = mobileClient.createCertRequest("entity")
    private val mobileCertRequest = MobileCertificateRequest.ADAPTER.decode(certRequest.serialize())

    @Test
    fun `test createMobileCertRequest version`() {
      assertEquals(0u, mobileCertRequest.version!!.toUInt())
    }

    @Test
    fun `test createCertRequest entity`() {
      check(certRequest is CertificateRequest.PKCS10Request)
      // Make sure the cert request has the appropriate entity name
      assertEquals(X500Name("CN=entity"), certRequest.pkcs10Req.subject)
    }

    @Test
    fun `test createCertRequest signing`() {
      // Make sure that the resulting pkcs10 request is signed by the appropriate key.
      assertTrue(certRequest.verify())
    }
  }

  @Nested
  @DisplayName("End Entity#createSignedData() Tests")
  inner class CreateSignedDataTests {
    // Create mobile signing certificate, which represents the request which would come from a
    // mobile client.
    private val certRequest = mobileClient.createCertRequest("entity")

    // Create local copy of issuingCert for use in verifying signature.
    private val issuingCert = certificateAuthority.createRootSigningCertificate(
      "issuingEntity", Period.ofDays(1)
    )
    private val mobileCert = certificateAuthority.signCertificate(issuingCert, certRequest)

    private val rawData = "hello world".encodeUtf8().toByteArray()

    @Test
    fun `test createSignedData succeeds`() {
      val signedData = assertDoesNotThrow {
        mobileClient.createSignedData(rawData, listOf(mobileCert, issuingCert))
      }

      assertEquals(signedData.envelopedData.data, rawData)
    }

    @Test
    fun `test createSignedData fails`() {
      val otherClientCert = certificateAuthority.signCertificate(issuingCert, CERT_REQUEST)

      assertThrows<IllegalStateException> {
        mobileClient.createSignedData(rawData, listOf(otherClientCert, issuingCert))
      }
    }
  }

  @Nested
  @DisplayName("SignedData#verify() Tests")
  inner class SignedDataTests {
    @Test
    fun `test verifies signed data`() {
      assertTrue(SIGNED_DATA.verify(CERT_ANCHOR))
    }

    @Test
    fun `test verifies signed data fails with wrong anchor`() {
      assertFalse(
        SIGNED_DATA.verify(
          certificateAuthority.createRootSigningCertificate(
            "issuingEntity", Period.ofDays(1)
          )
        )
      )
    }

    @Test
    fun `test verifies and extracts appropriate data`() {
      val expected = VerifiedData(SIGNED_DATA.envelopedData.data, SIGNED_DATA.certificates)
      assertEquals(expected, SIGNED_DATA.verifyAndExtract(CERT_ANCHOR))
    }

    @Test
    fun `test no data extracted with bad verification`() {
      val expected = VerifiedData(SIGNED_DATA.envelopedData.data, SIGNED_DATA.certificates)
      assertNull(
        SIGNED_DATA.verifyAndExtract(
          certificateAuthority.createRootSigningCertificate(
            "issuingEntity", Period.ofDays(1)
          )
        )
      )
    }
  }

  companion object {
    private lateinit var certificateAuthority: Trifle.CertificateAuthority
    private lateinit var mobileClient: Trifle.EndEntity

    @JvmStatic
    @BeforeAll
    fun setUp() {
      SignatureConfig.register()
      val ecSpec = ECGenParameterSpec("secp256r1")
      val generator = KeyPairGenerator.getInstance("EC")
      generator.initialize(ecSpec, SecureRandom())

      certificateAuthority = Trifle.CertificateAuthority(
        KeysetHandle.generateNew(RAW_ECDSA_P256_KEY_TEMPLATE)
      )
      mobileClient = Trifle.EndEntity(
        generator.genKeyPair()
      )
    }
  }
}
