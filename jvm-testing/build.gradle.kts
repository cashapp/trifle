plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish.base")
}

dependencies {
    api(project(":jvm"))
    implementation("com.google.crypto.tink:tink:1.7.0")
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    configure(
        com.vanniktech.maven.publish.KotlinJvm(
            javadocJar = com.vanniktech.maven.publish.JavadocJar.Dokka("dokkaHtml")
        )
    )
}
