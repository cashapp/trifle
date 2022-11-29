package app.cash.security_sdk.internal

import com.google.crypto.tink.PublicKeySign
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.operator.ContentSigner
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Internal s2dk class to enable bouncycastle to delegate signing to Tink primitives rather than
 * requiring Tink key users to extract the material. This should enable s2dk Clients to supply Tink
 * keys directly without needing to reason about their internals.
 */
internal class TinkContentSigner(
  private val publicKeySign: PublicKeySign,
  private val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
) : ContentSigner {
  val tinkAlgorithmIdentifier: AlgorithmIdentifier
    //TODO(dcashman): Add support for different algorithms.
    get() = AlgorithmIdentifier(ASN1ObjectIdentifier(ED25519_OID))

  override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
    return tinkAlgorithmIdentifier
  }

  override fun getOutputStream(): OutputStream {
    return outputStream
  }

  override fun getSignature(): ByteArray {
    val signedBytes = publicKeySign.sign(outputStream.toByteArray())
    outputStream.reset()
    return signedBytes
  }

  internal companion object {
    // Defined in https://www.rfc-editor.org/rfc/rfc8420.html
    // Registry http://oid-info.com/cgi-bin/display?oid=1.3.101.112
    internal const val ED25519_OID = "1.3.101.112"
  }
}
