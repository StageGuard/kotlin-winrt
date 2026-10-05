package io.github.composefluent.windows.toolkit.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectionSourceOwnershipTest {
    @Test
    fun inactive_projection_outputs_do_not_enter_classpaths_or_archives() {
        // CsWinRT targets include only the active projection's generated files.
        val project = ProjectBuilder.builder().build()
        val directory = project.layout.buildDirectory.dir("classes/projection")
        val staleClass = directory.get().file("OldSdkType.class").asFile
        staleClass.parentFile.mkdirs()
        staleClass.writeText("old projection output")
        val required = project.objects.property(Boolean::class.java).convention(true)
        val outputs = project.files(winRTJvmProjectionOutputFiles(directory, required)).asFileTree

        assertEquals(setOf(staleClass), outputs.files)
        required.set(false)
        assertEquals(emptySet<java.io.File>(), outputs.files)
    }

    @Test
    fun compilation_sources_follow_regenerated_shards_and_ownership() {
        // Gradle owns compilation input discovery (.cswinrt/build responsibility).
        // A new SDK may split a namespace into different generated shard files.
        val project = ProjectBuilder.builder().build()
        val directory = project.layout.buildDirectory.dir("generated/projection").get()
        val oldShard = directory.file("namespace_0.kt").asFile
        oldShard.parentFile.mkdirs()
        oldShard.writeText("@file:Suppress(\"KOTLIN_WINRT_GENERATED\")\nclass Before")
        val sources = project.files(generatedWinRTProjectionSourceFiles(project, directory))
        assertEquals(setOf(oldShard), sources.files)

        val newShard = directory.file("namespace_0_1.kt").asFile
        newShard.writeText("@file:Suppress(\"KOTLIN_WINRT_GENERATED\")\nclass After")
        oldShard.writeText("@file:Suppress(\"KOTLIN_WINRT_GENERATED\", \"KOTLIN_WINRT_BUSINESS_OVERLAY\")\nclass Overlay")
        directory.file("metadata.tsv").asFile.writeText("not Kotlin source")
        assertEquals(setOf(newShard), sources.files)
    }
}
