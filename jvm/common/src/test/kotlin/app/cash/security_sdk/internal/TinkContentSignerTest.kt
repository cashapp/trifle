package app.cash.security_sdk.internal

import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.signature.SignatureConfig
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class TinkContentSignerTest {
  private val data = byteArrayOf(0x00, 0x01, 0x02, 0x03)
  private lateinit var ed25519PublicKeySign: PublicKeySign
  private lateinit var ed25519PublicKeyVerify: PublicKeyVerify

  @BeforeEach
  fun setUp() {
    SignatureConfig.register()
    val ed25519PrivateKeyHandle = KeysetHandle.generateNew(KeyTemplates.get("ED25519"))
    ed25519PublicKeySign = ed25519PrivateKeyHandle.getPrimitive(PublicKeySign::class.java)
    val ed25519PublicKeyHandle = ed25519PrivateKeyHandle.publicKeysetHandle
    ed25519PublicKeyVerify = ed25519PublicKeyHandle.getPrimitive(PublicKeyVerify::class.java)
  }

  @Test
  fun `test ed25519 signature algorithm returns correct OID`() {
    val tinkContentSigner = TinkContentSigner(ed25519PublicKeySign)
    val ed25519Oid = AlgorithmIdentifier(ASN1ObjectIdentifier(TinkContentSigner.ED25519_OID))
    assertEquals(ed25519Oid, tinkContentSigner.getAlgorithmIdentifier())
  }

  @Test
  fun `test sign matches tink signature`() {
    val tinkContentSigner = TinkContentSigner(ed25519PublicKeySign)
    val outputStream = tinkContentSigner.getOutputStream()
    outputStream.write(data)
    val signedOutput = tinkContentSigner.getSignature()

    assertArrayEquals(signedOutput, ed25519PublicKeySign.sign(data))

    // Verify does not return anything, but throws exception if signature does not verify correctly.
    assertDoesNotThrow { ed25519PublicKeyVerify.verify(signedOutput, data) }
  }
}
