plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish.base")
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
    api(project(":common"))

    implementation(libs.bcProv)
    implementation(libs.bcPkix)
    implementation(libs.tink)

    testImplementation(libs.junitApi)
    testImplementation(libs.junitEngine)
    testImplementation(project(":jvm-testing"))
}

// Manually add .proto files to the .jar.
sourceSets {
    main {
        resources {
            srcDir("../proto")
        }
    }
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    configure(
        com.vanniktech.maven.publish.KotlinJvm(
            javadocJar = com.vanniktech.maven.publish.JavadocJar.Dokka("dokkaHtml")
        )
    )
}