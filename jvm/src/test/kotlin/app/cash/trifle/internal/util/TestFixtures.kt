package app.cash.trifle.internal.util

import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.proto.EcdsaKeyFormat
import com.google.crypto.tink.proto.EcdsaParams
import com.google.crypto.tink.proto.EcdsaSignatureEncoding
import com.google.crypto.tink.proto.EllipticCurveType
import com.google.crypto.tink.proto.HashType
import com.google.crypto.tink.signature.SignatureConfig
import java.util.Base64

internal object TestFixtures {
  // A PKCS#10 Certificate Request (PEM without the header and footer)
  // generated from the iOS SDK.
  val PKCS10Request: ByteArray = Base64.getDecoder().decode(
    """
    MIIBHjCBxAIBADBHMUUwCQYDVQQGEwJVUzAPBgNVBAMTCGNhc2guYXBwMBEGA1UE
    CBMKQ2FsaWZvcm5pYTAUBgNVBAcTDVNhbiBGcmFuY2lzY28wWTATBgcqhkjOPQIB
    BggqhkjOPQMBBwNCAAQNkp7f37RmuWxmybKevG8sCxu7tam07HDZuKpw35l41llH
    39mgNDsNZ9xgK87Ix5q1WGIWbsKLsEjdvpg/d8uboBswGQYJKoZIhvcNAQkHMQwW
    CmhlbGxvd29ybGQwCgYIKoZIzj0EAwIDSQAwRgIhAJ3KaP7tghkQz9cbOhjBbhsO
    o4LRj3nIy5bhctwKTlNcAiEAw4LV2zc4lembWpFQk1f2d9ukLjkaVSAoSbviVdpf
    e70=
    """.filterNot { it.isWhitespace() }
  )

  // Tink Java does not surface a default template for ecdsa p-256 key template with
  // DER signature encoding and RAW output prefix type (excludes Tink's preamble)
  val RAW_ECDSA_P256_KEY_TEMPLATE: KeyTemplate get() {
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
}
