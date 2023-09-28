buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.gradle.plugin)
        classpath(libs.dokka.gradle.plugin)
        classpath(libs.mavenPublish.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.wire.gradle.plugin)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    group = "app.cash.trifle"
    version = project.property("VERSION_NAME") as String

    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}


allprojects {
    plugins.withId("com.vanniktech.maven.publish.base") {
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "testMaven"
                    url = file("${rootProject.buildDir}/testMaven").toURI()
                }
            }
        }
        configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.DEFAULT, automaticRelease = true)
            signAllPublications()
            pom {
                description.set("Security functionality for interoperability/interaction with core services.")
                name.set(project.name)
                url.set("https://github.com/cashapp/trifle")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("cashapp")
                        name.set("Cash App")
                    }
                }
                scm {
                    url.set("https://github.com/cashapp/trifle/")
                    connection.set("scm:git:https://github.com/cashapp/trifle.git")
                    developerConnection.set("scm:git:ssh://git@github.com/cashapp/trifle.git")
                }
            }
        }
    }
}
