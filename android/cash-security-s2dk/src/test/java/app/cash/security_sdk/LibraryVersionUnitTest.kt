package app.cash.security_sdk

import org.junit.Assert.assertEquals
import org.junit.Test

internal class LibraryVersionUnitTest {
  @Test
  fun testCurrentVersionCanBeExtracted() {
    val libraryVersion = LibraryVersion()
    val versionString = libraryVersion.complete()

    assertEquals(versionString, "0.1.0-SNAPSHOT")
  }

  @Test
  fun testMajorVersionExtracts() {
    val libraryVersion = LibraryVersion()
    val majorVersion = libraryVersion.major()

    assertEquals(majorVersion, 0)
  }

  @Test
  fun testMinorVersionExtracts() {
    val libraryVersion = LibraryVersion()
    val minorVersion = libraryVersion.minor()

    assertEquals(minorVersion, 1)
  }
}