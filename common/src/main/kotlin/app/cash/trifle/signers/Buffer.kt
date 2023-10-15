package app.cash.trifle.signers

import org.bouncycastle.util.Arrays
import java.io.ByteArrayOutputStream

/**
 * Buffer extends [ByteArrayOutputStream] that zeroes out the underlying buffer whenever
 *  reset() is called.
 */
class Buffer : ByteArrayOutputStream() {
  override fun reset() {
    Arrays.fill(buf, 0, count, 0.toByte())
    count = 0;
  }
}
