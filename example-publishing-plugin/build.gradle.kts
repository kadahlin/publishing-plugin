plugins {
    kotlin("jvm") version "1.9.0"
    `java-gradle-plugin`
    `maven-publish`
}

group = "org.yourorg"
version = "1.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create("libraryPlugin") {
            id = "org.yourorg.library.publishing"
            displayName = "YourOrg Kotlin publishing plugin"
            description =
                "Configure a kotlin module (JVM, Android, or Multiplatform) with standard parameters to be published"
            implementationClass = "org.yourorg.PublishingPlugin"
        }
    }
}


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:1.9.0")
}