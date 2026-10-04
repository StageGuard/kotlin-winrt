@file:OptIn(org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi::class)

package io.github.composefluent.windows.toolkit.gradle

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NonJvmTargetCompileTasksTest {
    // The Android library target of the Android Gradle plugin is an external Kotlin target with
    // the jvm platform type whose main compilation is compiled by a KotlinJvmCompile task named
    // compileAndroidMain. The fixture declares a target of that shape without the Android plugin.
    @Test
    fun a_jvm_platform_target_that_is_not_a_jvm_target_gets_no_projection_compilation() {
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

        assertEquals(setOf("compileAndroidMain"), nonJvmTargetCompileTaskNames(project).filter { it.contains("Android") }.toSet())

        (project as ProjectInternal).evaluate()

        assertNotNull(project.tasks.findByName("compileKotlinWinRTProjectionDesktop"))
        assertNull(project.tasks.findByName("compileKotlinWinRTProjectioncompileAndroidMain"))
    }

    class AndroidLikeTarget(delegate: Delegate) : DecoratedExternalKotlinTarget(delegate)

    class AndroidLikeCompilation(delegate: Delegate) : DecoratedExternalKotlinCompilation(delegate)
}
