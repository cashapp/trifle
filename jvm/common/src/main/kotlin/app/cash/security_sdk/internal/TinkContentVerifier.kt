package app.cash.security_sdk.internal

import app.cash.security_sdk.internal.TinkContentSigner.Companion.ED25519_OID
import com.google.crypto.tink.PublicKeyVerify
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.operator.ContentVerifier
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.security.GeneralSecurityException

/**
 * Internal s2dk class to enable bouncycastle to delegate verifying to Tink primitives rather than
 * requiring Tink key users to extract the material. This should enable s2dk Clients to supply Tink
 * keys directly without needing to reason about their internals.
 */
internal class TinkContentVerifier(
  private val publicKeyVerify: PublicKeyVerify,
  private val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
) : ContentVerifier {
  private val tinkAlgorithmIdentifier: AlgorithmIdentifier
    //TODO(dcashman): Add support for different algorithms.
    get() = AlgorithmIdentifier(ASN1ObjectIdentifier(ED25519_OID))

  override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
    return tinkAlgorithmIdentifier
  }

  override fun getOutputStream(): OutputStream {
    return outputStream
  }

  override fun verify(expected: ByteArray): Boolean {
    return try {
      publicKeyVerify.verify(expected, outputStream.toByteArray())
      true
    } catch (e: GeneralSecurityException) {
      // Signature did not verify.
      false;
    } finally {
      outputStream.reset()
    }
  }
}
