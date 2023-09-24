/*
Copyright 2023 Kyle Dahlin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.yourorg

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.net.URI

class PublishingPlugin : Plugin<Project> {

    private lateinit var libraryExtension: YourOrgLibraryExtension
    override fun apply(target: Project) {
        target.plugins.apply("org.gradle.maven-publish")

        libraryExtension = target.extensions.create("library", YourOrgLibraryExtension::class.java)
        libraryExtension.groupId.set("org.yourorg")
        target.plugins.apply("org.gradle.maven-publish")

        target.extensions.configure(PublishingExtension::class.java) { extension ->
            extension.repositories {
                it.maven { repo ->
                    repo.url = URI.create("s3://your-repo.s3.us-west-2.amazonaws.com")
                    repo.credentials(AwsCredentials::class.java) { aws ->
                        aws.accessKey = System.getenv("AWS_ACCESS_KEY_ID")
                        aws.secretKey = System.getenv("AWS_SECRET_ACCESS_KEY")
                    }
                }
            }
        }

        when {
            target.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> target.configureMultiplatform()
            target.plugins.hasPlugin("com.android.library") -> target.configureAndroidLibrary()
            target.plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> target.configureJvmLibrary()
            else -> throw IllegalStateException("Publishing plugin must be applied to a multiplatform, JVM, or Android kotlin module")
        }

        target.afterEvaluate { project ->
            project.group = libraryExtension.groupId.get()
            project.version = libraryExtension.version.get()
            target.configurePom(libraryExtension)
        }
    }

    private fun Project.configurePom(extension: YourOrgLibraryExtension) {
        configure<PublishingExtension> {
            publications.withType(MavenPublication::class.java) { pub ->
//                    if (libraryExtension.dokkaHtml.isPresent) {
//                        val javadocJar by tasks.register("${pub.name}JavadocJar", Jar::class.java) {
//                            it.archiveClassifier.set("javadoc")
//                            it.group = "documentation"
//                            it.from(libraryExtension.dokkaHtml)
//                        }
//                        pub.artifact(javadocJar)
//                    }
                pub.pom { pom ->
                    pom.organization {
                        it.url.set("https://yourorg.com")
                        it.name.set("YourOrg,LLC")
                    }
                    pom.developers {
                        if (extension.author.isPresent) {
                            it.developer { developer ->
                                developer.name.set(extension.author.get())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Project.configureMultiplatform() {
        logger.info("Configuring Multiplatform library project")
        project.configure<KotlinMultiplatformExtension> {
            if (project.plugins.hasPlugin("com.android.library")) {
                androidTarget {
                    publishLibraryVariants("release", "debug")
                }
            }
        }
        project.configure<PublishingExtension> {
            publications.withType(MavenPublication::class.java) { publication ->
                afterEvaluate {
                    publication.artifactId = if (publication.name == "kotlinMultiplatform") {
                        libraryExtension.artifactId.get()
                    } else if (publication.name.startsWith("android")) {
                        if (publication.name.endsWith("Debug")) {
                            libraryExtension.artifactId.get() + "-android-debug"
                        } else {
                            libraryExtension.artifactId.get() + "-android"
                        }
                    } else {
                        publication.artifactId.replace(project.name, libraryExtension.artifactId.get())
                    }
                }
            }
        }
    }

    private fun Project.configureJvmLibrary() {
        configure<JavaPluginExtension> {
            withSourcesJar()
        }
        afterEvaluate { project ->
            logger.info("Configuring JVM library project")
            project.configure<PublishingExtension> {
                publications {
                    it.register("release", MavenPublication::class.java) { pub ->
                        pub.artifactId = libraryExtension.artifactId.get()
                        pub.from(project.components.getByName("java"))
                    }
                }
            }
        }
    }

    private fun Project.configureAndroidLibrary() {
        logger.info("Configuring Android library project")
        afterEvaluate { project ->
            project.configure<PublishingExtension> {
                publications {
                    it.register("release", MavenPublication::class.java) { pub ->
                        pub.artifactId = libraryExtension.artifactId.get()
                        pub.from(project.components.getByName("release"))
                    }
                }
            }
        }
    }
}

inline fun <reified T> Project.configure(crossinline onConfigure: T.() -> Unit) {
    extensions.configure(T::class.java) {
        it.onConfigure()
    }
}
