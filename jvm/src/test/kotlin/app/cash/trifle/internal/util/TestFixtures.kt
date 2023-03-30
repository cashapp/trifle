package app.cash.trifle.internal.util

import app.cash.trifle.Certificate
import app.cash.trifle.CertificateRequest
import app.cash.trifle.protos.api.alpha.SignedData
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.proto.EcdsaKeyFormat
import com.google.crypto.tink.proto.EcdsaParams
import com.google.crypto.tink.proto.EcdsaSignatureEncoding
import com.google.crypto.tink.proto.EllipticCurveType
import com.google.crypto.tink.proto.HashType
import com.google.crypto.tink.signature.SignatureConfig
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

  val CERT_REQUEST: CertificateRequest = CertificateRequest.deserialize(
    Base64.getDecoder().decode(
      """
        CAASzwEwgcwwcwIBADARMQ8wDQYDVQQDEwZlbnRpdHkwWTATBgcqhkjOPQIBB
        ggqhkjOPQMBBwNCAgTWDpceUUbTF6Akm4c6UsIyiVQmibFIU6tnOUX74w8FX7
        3L4LCIhzMqG0QgyGhrdUm6zdW334lOYAalPngdnAxUoAAwCgYIKoZIzj0EAwI
        DSQAwRgIhAOzNAUMZX4Gl6HfAjAkkTrNzEFdE1RXDdng7SEY9jtwRAiEA1sSG
        39LYve+sgkZl/mUOBIoWyrVjYSbqHpQe2Bai3K4=
      """.filterNot { it.isWhitespace() }
    )
  )

  val SIGNED_DATA: SignedData = SignedData.ADAPTER.decode(
    Base64.getDecoder().decode(
      """
        ChEIABABGgtoZWxsbyB3b3JsZBJIMEYCIQC6sVAW0Eywl2x8WWt6h6SOrMhL0
        /jgpd1mfYAbLgKYXQIhAMn5nahPynglT2jBZaPd2aXD8ngUKkI38s5FmhLOnQ
        ykGpUCCAASkAIwggEMMIGyoAMCAQICBgGHI/31KTAKBggqhkjOPQQDAjAYMRY
        wFAYDVQQDDA1pc3N1aW5nRW50aXR5MB4XDTIzMDMyNzE2NTQxOFoXDTIzMDky
        MzE2NTQxOFowAjEAMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgEE4BzOricju
        neL2x5xrHO9bzzAdc7Ujo/TBkb9Hjs2G5LY842m5/euCmkgLFavVdjL7aQGe8
        PwVtcRs+vA2cXxEjAKBggqhkjOPQQDAgNJADBGAiEA/ShhUCz24imaCEF8a8+
        W89Y8UYLHyTl46L3GAbAXvFQCIQDY202vuMx06tpVlXqMAfMSoxqaaX/L0Qt9
        z6Jr1L8JPhrtAggAEugCMIIBZDCCAQqgAwIBAgIBATAKBggqhkjOPQQDAjAYM
        RYwFAYDVQQDDA1pc3N1aW5nRW50aXR5MB4XDTIzMDMyNzE2NTIxNFoXDTI1MT
        IyMTE2NTIxNFowGDEWMBQGA1UEAwwNaXNzdWluZ0VudGl0eTBZMBMGByqGSM4
        9AgEGCCqGSM49AwEHA0IABBFxFhSwrwROj0VBbuz9kJM74f0sbi35y1W1HH0N
        baqzImTER7V06rxHlDGYC8o6nWnVijodCjlu0yKO0h0eOu2jRTBDMA8GA1UdE
        wEB/wQFMAMBAf8wDgYDVR0PAQH/BAQDAgIEMCAGA1UdDgEB/wQWBBSUlrcm/B
        oAeAGlNA05wGEYyveTmDAKBggqhkjOPQQDAgNIADBFAiEAiXXdQrMkyjdCRUw
        7CcMxsYIUobNLajKE/i8zCmo6MncCIEKT9to/dbjJekdexnbeE3r4QTawL3fl
        tENkcAx75UHF
      """.filterNot { it.isWhitespace() }
    )
  )

  val CERT_CHAIN: List<Certificate> = SIGNED_DATA.certificates.map {
    Certificate(it.certificate!!.toByteArray())
  }

  val CERT_ANCHOR: Certificate = Certificate.deserialize(
    Base64.getDecoder().decode(
      """
        CAAS6AIwggFkMIIBCqADAgECAgEBMAoGCCqGSM49BAMCMBgxFjAUBgNVBAMMDW
        lzc3VpbmdFbnRpdHkwHhcNMjMwMzI3MTY1MjE0WhcNMjUxMjIxMTY1MjE0WjAY
        MRYwFAYDVQQDDA1pc3N1aW5nRW50aXR5MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQ
        cDQgAEEXEWFLCvBE6PRUFu7P2Qkzvh/SxuLfnLVbUcfQ1tqrMiZMRHtXTqvEeU
        MZgLyjqdadWKOh0KOW7TIo7SHR467aNFMEMwDwYDVR0TAQH/BAUwAwEB/zAOBg
        NVHQ8BAf8EBAMCAgQwIAYDVR0OAQH/BBYEFJSWtyb8GgB4AaU0DTnAYRjK95OY
        MAoGCCqGSM49BAMCA0gAMEUCIQCJdd1CsyTKN0JFTDsJwzGxghShs0tqMoT+Lz
        MKajoydwIgQpP22j91uMl6R17Gdt4TevhBNrAvd+W0Q2RwDHvlQcU=
      """.filterNot { it.isWhitespace() }
    )
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
