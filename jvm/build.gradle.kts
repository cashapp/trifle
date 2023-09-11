plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("com.squareup.wire") version "4.4.3"
    id("com.vanniktech.maven.publish.base")
}

dependencies {
    // https://mvnrepository.com/artifact/com.squareup.wire/wire-runtime
    implementation("com.squareup.wire:wire-runtime:4.4.3")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Add junit5 dependency as described in:
    // https://docs.gradle.org/current/userguide/java_testing.html#using_junit5
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")

    testImplementation(project(":jvm-testing"))

    // Add legacy junit dependencies to ensure existing junit4 tests still compile.
    testCompileOnly("junit:junit:4.13")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
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

repositories {
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk15to18:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.70")
    implementation("com.google.crypto.tink:tink:1.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
}

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
