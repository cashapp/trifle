package app.cash.trifle.internal.util

import app.cash.trifle.CertificateRequest
import app.cash.trifle.protos.api.alpha.MobileCertificateRequest
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.proto.EcdsaKeyFormat
import com.google.crypto.tink.proto.EcdsaParams
import com.google.crypto.tink.proto.EcdsaSignatureEncoding
import com.google.crypto.tink.proto.EllipticCurveType
import com.google.crypto.tink.proto.HashType
import com.google.crypto.tink.signature.SignatureConfig
import okio.ByteString.Companion.toByteString
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.util.Base64

internal object TestFixtures {
  val EC_KEYPAIR: KeyPair by lazy {
    val ecSpec = ECGenParameterSpec("secp256r1")
    val generator = KeyPairGenerator.getInstance("EC")
    generator.initialize(ecSpec, SecureRandom())

    generator.generateKeyPair()
  }

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

  val CERT_REQUEST: CertificateRequest = CertificateRequest.deserialize(
    MobileCertificateRequest(
      0,
      PKCS10Request.toByteString()
    ).encode()
  )

  val SIGNED_DATA: ByteArray = Base64.getDecoder().decode(
    """
      ChEIABABGgtoZWxsbyB3b3JsZBJIMEYCIQC6sVAW0Eywl2x8WWt6h6SOrMhL0/j
      gpd1mfYAbLgKYXQIhAMn5nahPynglT2jBZaPd2aXD8ngUKkI38s5FmhLOnQykGp
      UCCAASkAIwggEMMIGyoAMCAQICBgGHI/31KTAKBggqhkjOPQQDAjAYMRYwFAYDV
      QQDDA1pc3N1aW5nRW50aXR5MB4XDTIzMDMyNzE2NTQxOFoXDTIzMDkyMzE2NTQx
      OFowAjEAMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgEE4BzOricjuneL2x5xrHO
      9bzzAdc7Ujo/TBkb9Hjs2G5LY842m5/euCmkgLFavVdjL7aQGe8PwVtcRs+vA2c
      XxEjAKBggqhkjOPQQDAgNJADBGAiEA/ShhUCz24imaCEF8a8+W89Y8UYLHyTl46
      L3GAbAXvFQCIQDY202vuMx06tpVlXqMAfMSoxqaaX/L0Qt9z6Jr1L8JPhrtAggA
      EugCMIIBZDCCAQqgAwIBAgIBATAKBggqhkjOPQQDAjAYMRYwFAYDVQQDDA1pc3N
      1aW5nRW50aXR5MB4XDTIzMDMyNzE2NTIxNFoXDTI1MTIyMTE2NTIxNFowGDEWMB
      QGA1UEAwwNaXNzdWluZ0VudGl0eTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IAB
      BFxFhSwrwROj0VBbuz9kJM74f0sbi35y1W1HH0NbaqzImTER7V06rxHlDGYC8o6
      nWnVijodCjlu0yKO0h0eOu2jRTBDMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH
      /BAQDAgIEMCAGA1UdDgEB/wQWBBSUlrcm/BoAeAGlNA05wGEYyveTmDAKBggqhk
      jOPQQDAgNIADBFAiEAiXXdQrMkyjdCRUw7CcMxsYIUobNLajKE/i8zCmo6MncCI
      EKT9to/dbjJekdexnbeE3r4QTawL3fltENkcAx75UHF
    """.filterNot { it.isWhitespace() }
  )

  val CERT_ANCHOR: ByteArray = Base64.getDecoder().decode(
    """
      CAAS6AIwggFkMIIBCqADAgECAgEBMAoGCCqGSM49BAMCMBgxFjAUBgNVBAMMDWl
      zc3VpbmdFbnRpdHkwHhcNMjMwMzI3MTY1MjE0WhcNMjUxMjIxMTY1MjE0WjAYMR
      YwFAYDVQQDDA1pc3N1aW5nRW50aXR5MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQ
      gAEEXEWFLCvBE6PRUFu7P2Qkzvh/SxuLfnLVbUcfQ1tqrMiZMRHtXTqvEeUMZgL
      yjqdadWKOh0KOW7TIo7SHR467aNFMEMwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8
      BAf8EBAMCAgQwIAYDVR0OAQH/BBYEFJSWtyb8GgB4AaU0DTnAYRjK95OYMAoGCC
      qGSM49BAMCA0gAMEUCIQCJdd1CsyTKN0JFTDsJwzGxghShs0tqMoT+LzMKajoyd
      wIgQpP22j91uMl6R17Gdt4TevhBNrAvd+W0Q2RwDHvlQcU=
    """.filterNot { it.isWhitespace() }
  )

  val RAW_EDDSA_ED25519_KEY_TEMPLATE: KeyTemplate
    get() = KeyTemplates.get("ED25519WithRawOutput")

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
}
