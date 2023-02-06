plugins {
    kotlin("jvm")
    id("com.squareup.wire") version "4.4.3"
    id("com.vanniktech.maven.publish.base")
}

dependencies {
    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:31.0.1-jre")

    // https://mvnrepository.com/artifact/com.squareup.wire/wire-runtime
    implementation("com.squareup.wire:wire-runtime:4.4.3")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Add junit5 dependency as described in:
    // https://docs.gradle.org/current/userguide/java_testing.html#using_junit5
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")

    // Add legacy junit dependencies to ensure existing junit4 tests still compile.
    testCompileOnly("junit:junit:4.13")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")
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

val protosSrc = "src/main/proto/"

repositories {
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk15to18:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.70")
    implementation("com.google.crypto.tink:tink:1.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
}

// Manually add .proto files to the .jar.
sourceSets {
    main {
        resources {
            srcDir("src/main/proto")
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
            javadocJar = com.vanniktech.maven.publish.JavadocJar.None()
        )
    )
}
