package app.cash.trifle

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec
import java.util.UUID

data class KeyHandle internal constructor(val tag: String) {
  init {
    if (!containsTag(tag)) {
      // Need to generate a new key for the generated tag in the keystore.
      val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_EC,
        ANDROID_KEYSTORE_TYPE
      )
      val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
        tag,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
      ).run {
        setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
        setUserAuthenticationRequired(false)
        setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
        build()
      }

      kpg.initialize(parameterSpec)
      kpg.generateKeyPair()
      Log.i("TRIFLE", "Created KeyHandle with tag $tag")
    }
  }

  internal val keyPair: KeyPair by lazy {
    val exceptionMessage = String.format(EXCEPTION_MSG, tag)
    try {
      val entry: KeyStore.Entry = KEY_STORE.getEntry(tag, null)
      if (entry is KeyStore.PrivateKeyEntry) {
        return@lazy KeyPair(entry.certificate.publicKey, entry.privateKey)
      }
    } catch (e: Exception) {
      throw IllegalStateException(exceptionMessage, e)
    }
    throw IllegalStateException(exceptionMessage)
  }

  fun serialize(): ByteArray = tag.toByteArray(Charsets.UTF_8)

  companion object {
    // Throw an illegal state exception if we can't get hold of the proper key material. This
    // *should never happen* since the only way to obtain a KeyHandle is to deserialize one, which
    // should have already checked for this, or to generate a new one.
    private const val EXCEPTION_MSG =
      "Android KeyStore does not contain a keypair corresponding to the %s tag"
    private const val ANDROID_KEYSTORE_TYPE: String = "AndroidKeyStore"
    private val KEY_STORE = KeyStore.getInstance(ANDROID_KEYSTORE_TYPE).apply {
      load(null)
    }

    fun deserialize(bytes: ByteArray): KeyHandle {
      val tag = bytes.toString(Charsets.UTF_8)
      if (!containsTag(tag)) {
        throw IllegalStateException(String.format(EXCEPTION_MSG, tag))
      }
      return KeyHandle(tag)
    }

    //TODO(dcashman): Consolidate API surface with iOS surface.
    internal fun generateKeyHandle(reverseDomain: String): KeyHandle =
      KeyHandle("$reverseDomain.sign.${UUID.randomUUID()}")

    internal fun containsTag(tag: String): Boolean = KEY_STORE.containsAlias(tag)

    internal fun deleteTag(tag: String) {
      if (containsTag(tag)) KEY_STORE.deleteEntry(tag)
    }
  }
}
