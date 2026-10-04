package io.github.composefluent.windows.toolkit.gradle

import java.nio.file.Files
import java.nio.file.Path
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigurationTimeResolutionTest {
    // The Android Gradle plugin rejects every configuration that is resolved before the projects
    // are evaluated when android.dependencyResolutionAtConfigurationTime.disallow is set. The
    // fixture applies the same rule without the Android plugin.
    @Test
    fun projections_are_generated_where_configurations_must_not_be_resolved_during_configuration() {
        val root = Files.createTempDirectory("kotlin-winrt-configuration-time-resolution-")
        write(root.resolve("settings.gradle"), """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
            rootProject.name = 'configuration-time-resolution'
        """)
        write(root.resolve("gradle.properties"), """
            org.gradle.daemon=false
            org.gradle.workers.max=1
            org.gradle.jvmargs=-Xmx384m -XX:CICompilerCount=1 -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8
        """)
        write(root.resolve("build.gradle"), """
            plugins {
                id 'org.jetbrains.kotlin.multiplatform'
                id 'io.github.compose-fluent.windows-toolkit'
            }
            repositories { mavenCentral() }
            kotlin { jvm('desktop') }
            def configurationTime = true
            configurations.configureEach { configuration ->
                configuration.incoming.beforeResolve {
                    if (configurationTime) {
                        throw new IllegalStateException(
                            "Configuration '" + configuration.name + "' was resolved during configuration time.")
                    }
                }
            }
            gradle.projectsEvaluated { configurationTime = false }
        """)
        write(root.resolve("src/commonMain/kotlin/sample/Model.kt"), """
            package sample

            class Model { var title: String = "" }
        """)

        val result = GradleRunner.create().withProjectDir(root.toFile()).withPluginClasspath()
            .withArguments("generateWinRTProjections", "--offline", "--stacktrace").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateWinRTProjections")?.outcome)
    }

    private fun write(path: Path, content: String) {
        Files.createDirectories(path.parent)
        Files.writeString(path, content.trimIndent() + System.lineSeparator())
    }
}
