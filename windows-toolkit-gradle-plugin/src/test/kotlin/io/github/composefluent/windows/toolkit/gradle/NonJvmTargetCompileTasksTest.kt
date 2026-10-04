@file:OptIn(org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi::class)

package io.github.composefluent.windows.toolkit.gradle

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.external.DecoratedExternalKotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.external.DecoratedExternalKotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.external.ExternalKotlinCompilationDescriptor
import org.jetbrains.kotlin.gradle.plugin.mpp.external.ExternalKotlinTargetDescriptor
import org.jetbrains.kotlin.gradle.plugin.mpp.external.createCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.external.createExternalKotlinTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// The Android library target of the Android Gradle plugin is an external Kotlin target with the
// jvm platform type whose main compilation is compiled by a KotlinJvmCompile task named
// compileAndroidMain. The fixture declares a target of that shape without the Android plugin.
class NonJvmTargetCompileTasksTest {
    @Test
    fun a_jvm_platform_target_that_is_not_a_jvm_target_gets_no_projection_compilation() {
        val project = androidLikeProject()

        assertEquals(setOf("compileAndroidMain"), nonJvmTargetCompileTaskNames(project).filter { it.contains("Android") }.toSet())

        (project as ProjectInternal).evaluate()

        assertNotNull(project.tasks.findByName("compileKotlinWinRTProjectionDesktop"))
        assertNull(project.tasks.findByName("compileKotlinWinRTProjectioncompileAndroidMain"))
    }

    // With the options, the compiler plugin looks for the projection registrar of the module,
    // which a compilation without the projection on its class path cannot resolve.
    @Test
    fun a_jvm_platform_target_that_is_not_a_jvm_target_gets_no_compiler_plugin_options() {
        val project = androidLikeProject()
        (project as ProjectInternal).evaluate()

        val android = project.tasks.getByName("compileAndroidMain") as KotlinJvmCompile

        assertFalse(android.compilerOptions.freeCompilerArgs.get().any { it.contains("io.github.composefluent.winrt.compiler") })
        assertFalse(android.outputs.files.files.any { it.name == "type-index.tsv" })
    }

    private fun androidLikeProject(): Project {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply(KotlinWindowsToolkitPlugin::class.java)
        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.jvm("desktop")
        val android = kotlin.createExternalKotlinTarget<AndroidLikeTarget> {
            targetName = "android"
            platformType = KotlinPlatformType.jvm
            targetFactory = ExternalKotlinTargetDescriptor.TargetFactory(::AndroidLikeTarget)
        }
        android.createCompilation<AndroidLikeCompilation> {
            compilationName = "main"
            compileTaskName = "compileAndroidMain"
            defaultSourceSet = kotlin.sourceSets.create("androidMain")
            compilationFactory = ExternalKotlinCompilationDescriptor.CompilationFactory(::AndroidLikeCompilation)
        }
        assertTrue(project.tasks.getByName("compileAndroidMain") is KotlinJvmCompile)
        return project
    }

    class AndroidLikeTarget(delegate: Delegate) : DecoratedExternalKotlinTarget(delegate)

    class AndroidLikeCompilation(delegate: Delegate) : DecoratedExternalKotlinCompilation(delegate)
}
