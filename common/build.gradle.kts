plugins {
  kotlin("jvm")
  id("org.jetbrains.dokka")
  id("com.squareup.wire")
  id("com.vanniktech.maven.publish.base")
}

dependencies {
  // https://mvnrepository.com/artifact/com.squareup.wire/wire-runtime
  implementation(libs.wire)
  implementation(libs.bcProv)
  implementation(libs.bcPkix)

  // Use the Kotlin test library.
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation(libs.junitApi)
  testImplementation(libs.junitEngine)
  testImplementation(project(":jvm-testing"))
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
}

tasks.jar {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version
      )
    )
  }
}

tasks.test {
  useJUnitPlatform()
}

apply(plugin = "kotlin")

val protosSrc = "../proto/"

// Manually add .proto files to the .jar.
sourceSets {
  main {
    resources {
      srcDir("../proto")
    }
  }
}

wire {
  sourcePath {
    srcDir(protosSrc)
  }
  protoPath {
  }
  kotlin {
    javaInterop = true
  }
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
  configure(
    com.vanniktech.maven.publish.KotlinJvm(
      javadocJar = com.vanniktech.maven.publish.JavadocJar.Dokka("dokkaHtml")
    )
  )
}
