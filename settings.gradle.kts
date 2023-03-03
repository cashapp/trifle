rootProject.name = "trifle"

plugins {
    id("com.gradle.enterprise") version ("3.9")
}

include("android")
include("android:sample_app")
include("android:trifle")
include("jvm")

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}
