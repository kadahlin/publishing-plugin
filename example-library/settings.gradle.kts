rootProject.name = "example"

pluginManagement {
    includeBuild("../example-publishing-plugin")

    repositories {
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
