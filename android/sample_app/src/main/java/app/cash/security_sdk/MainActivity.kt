package app.cash.security_sdk

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val version = LibraryVersion()
    Log.v(TAG, "Security SDK version: $version")

    setContentView(R.layout.activity_main)
  }

  private companion object {
    private const val TAG = "MainActivity"
  }
}