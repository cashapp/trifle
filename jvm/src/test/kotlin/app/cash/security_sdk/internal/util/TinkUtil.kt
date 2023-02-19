package app.cash.security_sdk.internal.util

import app.cash.security_sdk.internal.TrifleAlgorithmIdentifier
import app.cash.security_sdk.internal.TrifleAlgorithmIdentifier.ECPublicKeyAlgorithmIdentifier
import app.cash.security_sdk.internal.TrifleAlgorithmIdentifier.Ed25519AlgorithmIdentifier
import com.google.crypto.tink.BinaryKeysetWriter
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.KeysetHandle
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import java.io.ByteArrayOutputStream

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