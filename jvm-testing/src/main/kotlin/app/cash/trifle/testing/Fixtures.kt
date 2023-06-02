package app.cash.trifle.testing

import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.proto.EcdsaKeyFormat
import com.google.crypto.tink.proto.EcdsaParams
import com.google.crypto.tink.proto.EcdsaSignatureEncoding
import com.google.crypto.tink.proto.EllipticCurveType
import com.google.crypto.tink.proto.HashType
import com.google.crypto.tink.signature.SignatureConfig
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec

object Fixtures {
  // Tink Java does not surface a default template for ecdsa p-256 key template with
  // DER signature encoding and RAW output prefix type (excludes Tink's preamble)
  val RAW_ECDSA_P256_KEY_TEMPLATE: KeyTemplate
    get() {
      val params: EcdsaParams = EcdsaParams.newBuilder()
        .setHashType(HashType.SHA256)
        .setCurve(EllipticCurveType.NIST_P256)
        .setEncoding(EcdsaSignatureEncoding.DER)
        .build()
      val format = EcdsaKeyFormat.newBuilder().setParams(params).build()
      return KeyTemplate.create(
        SignatureConfig.ECDSA_PRIVATE_KEY_TYPE_URL,
        format.toByteArray(),
        KeyTemplate.OutputPrefixType.RAW
      )
    }

  val RAW_EDDSA_ED25519_KEY_TEMPLATE: KeyTemplate
    get() = KeyTemplates.get("ED25519WithRawOutput")

  val EC_SPEC: ECGenParameterSpec = ECGenParameterSpec("secp256r1")

  val GENERATOR: KeyPairGenerator get() {
    val generator = KeyPairGenerator.getInstance("EC")
    generator.initialize(EC_SPEC, SecureRandom())
    return generator
  }
}
