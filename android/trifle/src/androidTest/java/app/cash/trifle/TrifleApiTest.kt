package app.cash.trifle

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException


class TrifleApiTest {
  @JvmField
  @Rule
  val thrown: ExpectedException = ExpectedException.none()

  private lateinit var keyHandle: KeyHandle
  @Before fun setUp() {
    keyHandle = TrifleApi.generateKeyHandle("test-alias")
  }

  @Test fun testGenerateKeyHandle() {
    assertNotNull(keyHandle)
  }

  @Test fun testIsValid_forCreatedKeyHandle_returnsTrue() {
    assertTrue(TrifleApi.isValid(keyHandle))
  }

  @Test fun testIsValid_forDeletedKeyHandle_returnsFalse() {
    TrifleApi.delete(keyHandle)
    assertFalse(TrifleApi.isValid(keyHandle))
  }

  @Test fun testDeleteKeyHandle() {
    TrifleApi.delete(keyHandle)
  }
}
