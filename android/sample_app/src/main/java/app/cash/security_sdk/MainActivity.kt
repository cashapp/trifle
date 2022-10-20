package app.cash.security_sdk.sample_app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.cash.security_sdk.LibraryVersion

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

