package app.cash.trifle

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.cash.trifle.sample_app.R

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val version = LibraryVersion()
    Log.v(TAG, "Trifle version: $version")

    //Example Trifle use. Shouldn't typically be on the main UI thread, but that's the only reason
    // we're here, so it's ok.
    val keyHandle = Trifle("app.cash.trifle.keys").generateKeyHandle()

    setContentView(R.layout.activity_main)
  }

  private companion object {
    private const val TAG = "MainActivity"
  }
} 

