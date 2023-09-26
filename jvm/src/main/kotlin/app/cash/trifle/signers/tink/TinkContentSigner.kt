package app.cash.trifle.signers.tink

import app.cash.trifle.TrifleAlgorithmIdentifier
import app.cash.trifle.TrifleAlgorithmIdentifier.ECDSASha256AlgorithmIdentifier
import app.cash.trifle.TrifleAlgorithmIdentifier.EdDSAAlgorithmIdentifier
import app.cash.trifle.signers.Buffer
import app.cash.trifle.signers.TrifleContentSigner
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.proto.EcdsaPublicKey
import com.google.crypto.tink.proto.Ed25519PublicKey
import com.google.crypto.tink.proto.EllipticCurveType
import com.google.crypto.tink.signature.SignatureConfig.ECDSA_PUBLIC_KEY_TYPE_URL
import com.google.crypto.tink.signature.SignatureConfig.ED25519_PUBLIC_KEY_TYPE_URL
import com.google.crypto.tink.tinkkey.KeyAccess
import com.google.crypto.tink.tinkkey.internal.ProtoKey
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

/**
 * Internal trifle class to enable bouncycastle to delegate signing to Tink primitives rather than
 * requiring Tink key users to extract the material. This should enable trifle Clients to supply
 * Tink keys directly without needing to reason about their internals.
 */
class TinkContentSigner(
  private val privateKeysetHandle: KeysetHandle,
) : TrifleContentSigner {
  private val outputStream = Buffer()
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
    return when (val typeUrl = tinkKey.protoKey.typeUrl) {
      ED25519_PUBLIC_KEY_TYPE_URL ->
        SubjectPublicKeyInfo(EdDSAAlgorithmIdentifier, ed25519KeyValue())
      ECDSA_PUBLIC_KEY_TYPE_URL ->
        SubjectPublicKeyInfo.getInstance(ecdsaKeyValue())
      else ->
        throw UnsupportedOperationException("key type $typeUrl is not supported")
    }
  }

  override fun getAlgorithmIdentifier(): TrifleAlgorithmIdentifier {
    return when (val typeUrl = tinkKey.protoKey.typeUrl) {
      ED25519_PUBLIC_KEY_TYPE_URL -> EdDSAAlgorithmIdentifier
      ECDSA_PUBLIC_KEY_TYPE_URL -> ECDSASha256AlgorithmIdentifier
      else -> throw UnsupportedOperationException(
        "Default signature algorithm is not supported for key type: $typeUrl"
      )
    }
  }

  override fun getOutputStream(): Buffer = outputStream

  override fun getSignature(): ByteArray {
    val signedBytes = outputStream.use {
      publicKeySign.sign(it.toByteArray())
    }
    outputStream.reset()
    return signedBytes
  }

  internal fun getPublicKeyVerify(): PublicKeyVerify =
    publicKeysetHandle.getPrimitive(PublicKeyVerify::class.java)

  internal fun getPublicKeySign(): PublicKeySign = publicKeySign

  private fun ecdsaKeyValue(): ByteArray {
    val protoKey = EcdsaPublicKey.parseFrom(tinkKey.protoKey.value)
    val params = protoKey.params // check this?

    val ecPoint = ECPoint(
      BigInteger(protoKey.x.toByteArray()),
      BigInteger(protoKey.y.toByteArray())
    )

    val curveName = when (params.curve) {
      EllipticCurveType.NIST_P256 -> "secp256r1"
      else -> throw UnsupportedOperationException("Curve type ${params.curve} is not supported")
    }
    val parameterSpec = ECNamedCurveTable.getParameterSpec(curveName)
    val spec = ECNamedCurveSpec(
      curveName,
      parameterSpec.curve,
      parameterSpec.g,
      parameterSpec.n,
      parameterSpec.h,
      parameterSpec.seed
    )

    return KeyFactory.getInstance("EC")
      .generatePublic(ECPublicKeySpec(ecPoint, spec))
      .encoded
  }

  private fun ed25519KeyValue(): ByteArray =
    Ed25519PublicKey.parseFrom(tinkKey.protoKey.value).keyValue.toByteArray()
}
