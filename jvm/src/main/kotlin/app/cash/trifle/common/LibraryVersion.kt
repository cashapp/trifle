package app.cash.trifle.common

class LibraryVersion : Version {

  // TODO(dcashman): Get this value from the gradle build setup.
  private val recordedVersion: String = "0.1.8"

  override fun complete(): String = recordedVersion

  override fun major(): Int = recordedVersion.split('.').firstOrNull()?.toInt() ?: -1

  override fun minor(): Int = recordedVersion.split('.').elementAtOrNull(1)?.toInt() ?: -1
}
