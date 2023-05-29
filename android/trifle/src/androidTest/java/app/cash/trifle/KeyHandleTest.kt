package app.cash.trifle

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KeyHandleTest {
  @Test fun serializeDeserialize() {
    val keyHandle = KeyHandle.generateKeyHandle("test-alias")
    val serializedBytes = keyHandle.serialize()
    assertThat(KeyHandle.deserialize(serializedBytes)).isEqualTo(keyHandle)
  }
}
