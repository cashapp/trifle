plugins {
  id("com.android.library")
  id("kotlin-android")
  id("com.vanniktech.maven.publish.base")
}

android {
  compileSdkVersion(33)
  buildToolsVersion("30.0.2")

  defaultConfig {
    minSdkVersion(24)
    targetSdkVersion(33)

    buildConfigField("String", "VERSION_CODE", "\"${project.property("VERSION_CODE")}\"")
    buildConfigField("String", "VERSION_NAME", "\"${project.property("VERSION_NAME")}\"")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
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
  kotlinOptions {
    jvmTarget = "11"
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
  implementation("androidx.core:core-ktx:1.3.1")
  implementation("androidx.appcompat:appcompat:1.2.0")
  implementation("com.google.android.material:material:1.2.1")
  testImplementation("junit:junit:4.+")
  androidTestImplementation("androidx.test.ext:junit:1.1.2")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

  implementation(project(":jvm"))
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
  configure(
    com.vanniktech.maven.publish.AndroidSingleVariantLibrary(
      publishJavadocJar = false,
    )
  )
}
