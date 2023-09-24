plugins {
    kotlin("multiplatform") version "1.9.0"
    id("com.android.library") version "7.4.0"
    id("org.yourorg.library.publishing")
}

library {
    version.set("1.0.0")
    artifactId.set("example-models")
    author.set("Some Developer")
}

kotlin {
    jvm()
    androidTarget { }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting
    }
}

android {
    namespace = "com.yourorg.example.models"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}