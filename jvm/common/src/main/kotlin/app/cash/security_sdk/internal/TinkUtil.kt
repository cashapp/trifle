package app.cash.security_sdk.internal

import com.google.crypto.tink.BinaryKeysetWriter
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.KeysetHandle
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import java.io.ByteArrayOutputStream

fun KeysetHandle.toSubjectPublicKeyInfo(): SubjectPublicKeyInfo {
  val outputStream = ByteArrayOutputStream()
  CleartextKeysetHandle.write(
    // Ensure we only write the public component of our key!
    publicKeysetHandle,
    BinaryKeysetWriter.withOutputStream(outputStream)
  )
  return SubjectPublicKeyInfo(
    // TODO(dcashman): Define a custom OID based on tink primitives.
    AlgorithmIdentifier(ASN1ObjectIdentifier(TinkContentSigner.ED25519_OID)),
    outputStream.toByteArray()
  )
}
