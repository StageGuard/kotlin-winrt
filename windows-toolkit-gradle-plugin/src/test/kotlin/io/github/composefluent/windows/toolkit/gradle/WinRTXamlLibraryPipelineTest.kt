package io.github.composefluent.windows.toolkit.gradle

import java.nio.file.Files
import java.nio.file.Path
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WinRTXamlLibraryPipelineTest {
    // Only markup selects the declarations that the application header describes. A library
    // without XAML takes the header's references and dependency schemas, never its own sources,
    // so a source root that another task generates is not an undeclared input of the header.
    @Test
    fun library_header_without_xaml_does_not_read_generated_source_roots() {
        val root = writeLibrary("kotlin-winrt-xaml-library-header-")
        // The generated root has to exist before the header resolves its source roots.
        run(root, "generateVersion")

        val result = run(root, "generateVersion", "generateWinRTXamlApplicationHeader")

        assertEquals(TaskOutcome.UP_TO_DATE, result.task(":generateVersion")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateWinRTXamlApplicationHeader")?.outcome)
        assertTrue(Files.isRegularFile(root.resolve("build/generated/kotlin-winrt/xaml/application/KotlinXaml.winmd")))
    }

    private fun run(root: Path, vararg arguments: String) =
        GradleRunner.create().withProjectDir(root.toFile()).withPluginClasspath()
            .withArguments(*arguments, "--offline", "--stacktrace").build()

    private fun writeLibrary(prefix: String): Path {
        val root = Files.createTempDirectory(prefix)
        write(root.resolve("settings.gradle"), """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
            rootProject.name = 'xaml-library'
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
            def generateVersion = tasks.register('generateVersion') {
                def output = layout.buildDirectory.dir('generated/version')
                outputs.dir(output)
                doLast {
                    def source = output.get().file('sample/Version.kt').asFile
                    source.parentFile.mkdirs()
                    source.text = 'package sample\nobject Version { const val NAME = "1.0" }\n'
                }
            }
            kotlin {
                jvm('libraryDesktop')
                sourceSets.commonMain.kotlin.srcDir(generateVersion)
            }
        """)
        write(root.resolve("src/commonMain/kotlin/sample/Model.kt"), """
            package sample

            class Model { var title: String = "" }
        """)
        return root
    }

    private fun write(path: Path, content: String) {
        Files.createDirectories(path.parent)
        Files.writeString(path, content.trimIndent() + System.lineSeparator())
    }
}
