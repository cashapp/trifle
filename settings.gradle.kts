rootProject.name = "cash-s2dk"

plugins {
    id("com.gradle.enterprise") version ("3.9")
}

include("android")
include("android:sample_app")
include("android:cash-security-s2dk")
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
