package app.cash.trifle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test


class KeyHandleTest {
  private lateinit var keyHandle: KeyHandle
  @Before fun setUp() {
    keyHandle = TrifleApi("app.cash.trifle.keys").generateKeyHandle()
  }

  @Test fun serializeDeserialize() {
    val serializedBytes = keyHandle.serialize()
    assertEquals(KeyHandle.deserialize(serializedBytes), keyHandle)
  }

  @Test fun failsDeserializationDueToDeletedKeyHandle() {
    val serializedBytes = keyHandle.serialize()
    KeyHandle.deleteTag(keyHandle.tag)
    assertThrows(IllegalStateException::class.java) {
      KeyHandle.deserialize(serializedBytes)
    }
  }
}
