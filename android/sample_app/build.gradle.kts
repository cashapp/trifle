plugins {
  id("com.android.application")
  id("kotlin-android")
}

android {
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "app.cash.trifle"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.compileSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  packagingOptions {
    exclude("META-INF/DEPENDENCIES")
  }
}

dependencies {
  implementation(libs.androidMaterial)
  implementation(libs.androidxCoreKtx)
  implementation(libs.androidxAppcompat)
  implementation(libs.androidxConstraintlayout)
  implementation(project(":android:trifle"))

  constraints {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0") {
      because("kotlin-stdlib-jdk7 is now a part of kotlin-stdlib")
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0") {
      because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib")
    }
  }
}
