package app.cash.trifle.internal.util

import app.cash.trifle.internal.TrifleAlgorithmIdentifier
import app.cash.trifle.internal.TrifleAlgorithmIdentifier.ECPublicKeyAlgorithmIdentifier
import com.google.crypto.tink.BinaryKeysetWriter
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.KeysetHandle
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import java.io.ByteArrayOutputStream
import java.util.Base64

fun KeysetHandle.toSubjectPublicKeyInfo(curve: TrifleAlgorithmIdentifier): SubjectPublicKeyInfo {
  val outputStream = ByteArrayOutputStream()
  CleartextKeysetHandle.write(
    // Ensure we only write the public component of our key!
    publicKeysetHandle,
    BinaryKeysetWriter.withOutputStream(outputStream)
  )
  return SubjectPublicKeyInfo(
    ECPublicKeyAlgorithmIdentifier(curve),
    outputStream.toByteArray()
  )
}

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
}