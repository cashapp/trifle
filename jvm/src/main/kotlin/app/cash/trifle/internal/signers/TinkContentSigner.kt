package app.cash.trifle.internal.signers

import app.cash.trifle.internal.TrifleAlgorithmIdentifier.EdDSAAlgorithmIdentifier
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.proto.Ed25519PublicKey
import com.google.crypto.tink.tinkkey.KeyAccess
import com.google.crypto.tink.tinkkey.ProtoKey
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Internal trifle class to enable bouncycastle to delegate signing to Tink primitives rather than
 * requiring Tink key users to extract the material. This should enable trifle Clients to supply
 * Tink keys directly without needing to reason about their internals.
 */
internal class TinkContentSigner(
  private val privateKeysetHandle: KeysetHandle,
) : TrifleContentSigner() {
  private val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
  private val publicKeySign: PublicKeySign by lazy {
    privateKeysetHandle.getPrimitive(PublicKeySign::class.java)
  }
  private val publicKeysetHandle: KeysetHandle by lazy {
    privateKeysetHandle.publicKeysetHandle
  }
  private val tinkKey: ProtoKey by lazy {
    val protoKey = publicKeysetHandle
      .primaryKey()
      .getKey(KeyAccess.publicAccess()) as ProtoKey

    // ensure tink key was provisioned with a raw output prefix type which allows
    // interoperability with JCA
    check(protoKey.outputPrefixType == KeyTemplate.OutputPrefixType.RAW)
    protoKey
  }

  override fun subjectPublicKeyInfo(): SubjectPublicKeyInfo {
    val (keyAlgorithm, rawKeyValue) = when (val typeUrl = tinkKey.protoKey.typeUrl) {
      "type.googleapis.com/google.crypto.tink.Ed25519PublicKey" ->
        Pair(
          EdDSAAlgorithmIdentifier,
          Ed25519PublicKey.parseFrom(tinkKey.protoKey.value).keyValue.toByteArray()
        )

      else -> throw UnsupportedOperationException("key type $typeUrl is not supported")
    }

    return SubjectPublicKeyInfo(keyAlgorithm, rawKeyValue)
  }

  override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
    when (val typeUrl = tinkKey.protoKey.typeUrl) {
      "type.googleapis.com/google.crypto.tink.Ed25519PublicKey" -> return EdDSAAlgorithmIdentifier
      else -> throw UnsupportedOperationException(
        "Default signature algorithm is not supported for key type: $typeUrl"
      )
    }
  }

  override fun getOutputStream(): OutputStream {
    return outputStream
  }

  override fun getSignature(): ByteArray {
    val signedBytes = publicKeySign.sign(outputStream.toByteArray())
    outputStream.reset()
    return signedBytes
  }

  internal fun getPublicKeyVerify(): PublicKeyVerify =
    publicKeysetHandle.getPrimitive(PublicKeyVerify::class.java)

  internal fun getPublicKeySign(): PublicKeySign = publicKeySign
}
