package io.github.composefluent.windows.toolkit.gradle

import io.github.composefluent.winrt.metadata.WinRTXamlDeclarations
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.Base64

class CompileWinRTXamlTaskTest {
    @Test fun compiled_layout_uses_the_load_component_uri_without_build_or_source_prefixes() {
        val root = Files.createTempDirectory("xaml-pri-")
        val compiled = root.resolve("build/generated/xaml/final/compiled")
        val xbf = compiled.resolve("views/MainPage.xbf")
        Files.createDirectories(xbf.parent)
        Files.write(xbf, byteArrayOf(0x58, 0x42, 0x46))
        Files.writeString(compiled.resolve("views/MainPage.xaml"), "<Page/>")
        val original = root.resolve("src/main/kotlin/views/MainPage.xaml")
        Files.createDirectories(original.parent)
        Files.writeString(original, "<Page/>")
        val priRoot = root.resolve("pri")
        val items = ProjectPriInputStager(priRoot, "", root,
            mapOf(compiled.toString() to ""), setOf(original.toString())).stage(
            componentPriFiles = emptyList(), componentPriBaseRoot = root,
            appxResourceFiles = emptyList(), explicitResourceFiles = emptyList(),
            explicitLayoutFiles = listOf(compiled), explicitContentFiles = emptyList(),
            explicitEmbedFiles = emptyList(), defaultResourceFiles = emptyList(),
            defaultLayoutFiles = listOf(original), defaultContentFiles = emptyList(),
            includeDefaultProjectResources = true,
        )
        val embedded = items.single { it.kind == ApplicationPackageItemKind.Embed }
        assertEquals(priRoot.resolve("embed/views/MainPage.xbf"), embedded.target)
        assertArrayEquals(Files.readAllBytes(xbf), Files.readAllBytes(embedded.target))
        assertTrue(items.none { it.kind == ApplicationPackageItemKind.Layout || it.source == original })
    }

    @Test fun analyzes_nested_resources_and_removes_deleted_page_artifacts() {
        val compiler = System.getenv("WINRT_TEST_XAMLC")
        val genXbf = System.getenv("WINRT_TEST_GENXBF")
        val refs = System.getenv("WINRT_TEST_XAMLC_REFERENCES")
        assumeTrue(compiler != null && genXbf != null && refs != null)
        val root = Files.createTempDirectory("xaml-gradle-").toFile()
        val project = ProjectBuilder.builder().withProjectDir(root).build()
        val sources = File(root, "src").apply { mkdirs() }
        val page = File(sources, "pages/MainPage.xaml").apply {
            parentFile.mkdirs()
            writeText("""<Page xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation" xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml" x:Class="sample.MainPage"><Button x:Name="button"/></Page>""")
        }
        File(page.parentFile, "MainPage.kt").writeText("package sample; class MainPage")
        fun encoded(value: String) = Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray())
        val manifest = File(root, "resolved.tsv").apply {
            writeText("kotlin-winrt-prepared-metadata-v1\n" + refs!!.split(File.pathSeparator).flatMap {
                File(it).listFiles()!!.filter { file -> file.extension == "winmd" }
            }.joinToString("\n") { "file\tWindowsSdk\t${encoded("fixture")}\t${encoded(it.absolutePath)}" })
        }
        val task = project.tasks.create("analyze", CompileWinRTXamlTask::class.java)
        task.sourceRoots.from(sources)
        task.preparedMetadataManifest.set(manifest)
        task.compilerDirectory.set(File(compiler!!).parentFile)
        task.genXbfDirectory.set(File(genXbf!!))
        task.projectName.set("fixture")
        task.outputDirectory.set(File(root, "output"))
        task.compile()
        val index = WinRTXamlDeclarations.parse(task.declarationsFile.get().asFile.readText())
        assertEquals("pages/MainPage.xaml", index.pages.single().resourcePath)
        val stale = File(root, "output/compiled/pages/obsolete.xbf").apply { parentFile.mkdirs(); writeText("stale") }
        page.delete()
        task.compile()
        assertTrue(WinRTXamlDeclarations.parse(task.declarationsFile.get().asFile.readText()).pages.isEmpty())
        assertFalse(stale.exists())
        page.writeText("<Page invalid")
        assertTrue(runCatching { task.compile() }.isFailure)
        assertFalse(task.declarationsFile.get().asFile.exists())
    }
}
