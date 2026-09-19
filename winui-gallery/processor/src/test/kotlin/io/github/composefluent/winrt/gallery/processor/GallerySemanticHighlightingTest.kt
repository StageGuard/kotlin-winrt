@file:OptIn(org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi::class)

package io.github.composefluent.winrt.gallery.processor

import io.github.composefluent.winrt.gallery.code.*
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.common.ExitCode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Files
import kotlin.test.*

class GallerySemanticHighlightingTest {
    // Reference: IDEA 262 KotlinHighlightingColors + FunctionCall/VariableReferenceSemanticAnalyzer.
    // Compile against real symbols, including same-spelling shadowed names and extension calls.
    @Test fun resolved_fir_colors_survive_snippet_trimming_and_code_generation() {
        val source = """
            package fixture
            class Widget(var value: Int) { fun member() = value }
            fun Widget.extension() = value
            val packageValue = 1
            fun <T> sample(input: T) {
                val value = Widget(1)
                value.value = packageValue
                value.member()
                value.extension()
                value.apply { member(); println(this.value) }
                println(input)
                val callback = { 1 }
                callback()
                fun extension() = 2
                extension()
            }
        """.trimIndent()
        val start = source.indexOf("    val value")
        val end = source.lastIndexOf('}')
        val fragment = KotlinSourceFragment.from(source, start, end, source.substring(start, end).trimIndent())
        val document = compilePreview(source, fragment)
        fun kindAt(text: String, skip: Int = 0): KotlinCodeKind {
            val offset = document.source.indexOf(text) + skip
            assertTrue(offset >= 0, text)
            return document.spans.first { it.end > offset }.kind
        }
        assertEquals(fragment.source, document.source)
        assertEquals(KotlinCodeKind.Variable, kindAt("value = Widget"))
        assertEquals(KotlinCodeKind.Constructor, kindAt("Widget(1)"))
        assertEquals(KotlinCodeKind.Variable, kindAt("value.value"))
        assertEquals(KotlinCodeKind.Property, kindAt("value.value", "value.".length))
        assertEquals(KotlinCodeKind.StaticProperty, kindAt("packageValue"))
        assertEquals(KotlinCodeKind.FunctionCall, kindAt("member()"))
        assertEquals(KotlinCodeKind.StaticFunctionCall, kindAt("value.extension", "value.".length))
        assertEquals(KotlinCodeKind.StaticFunctionCall, kindAt("apply {"))
        assertEquals(KotlinCodeKind.Parameter, kindAt("input)"))
        assertEquals(KotlinCodeKind.Function, kindAt("extension() ="))
        assertEquals(KotlinCodeKind.FunctionCall, document.spans.first { it.end > document.source.lastIndexOf("extension()") }.kind)
        assertEquals(KotlinCodeKind.Variable, kindAt("callback()"))
    }

    @Test fun idea_2026_2_styles_distinguish_plain_calls_from_declarations_and_extensions() {
        for (palette in listOf(KotlinCodePalette.Light, KotlinCodePalette.Dark)) {
            for (kind in listOf(KotlinCodeKind.Variable, KotlinCodeKind.Parameter, KotlinCodeKind.FunctionCall, KotlinCodeKind.Constructor)) {
                assertEquals(palette.style(KotlinCodeKind.Plain), palette.style(kind), kind.name)
            }
            assertNotEquals(palette.style(KotlinCodeKind.Function), palette.style(KotlinCodeKind.FunctionCall))
            assertTrue(palette.style(KotlinCodeKind.StaticFunctionCall).italic)
            assertTrue(palette.style(KotlinCodeKind.StaticProperty).italic)
        }
        assertEquals(0x57AAF7u, KotlinCodePalette.Dark.style(KotlinCodeKind.StaticFunctionCall).foreground)
        assertEquals(0x56C1D6u, KotlinCodePalette.Dark.style(KotlinCodeKind.NamedArgument).foreground)
    }

    @Test fun preserves_windows_offsets_type_parameters_named_arguments_and_aliases() {
        val source = """
            package fixture
            import kotlin.collections.listOf as items
            annotation class Marker
            @Marker class Box<T>(val item: T)
            enum class State { Ready }
            fun <T> identity(value: T?): T? = value
            fun sample(input: String) {
                // 中文 😀 must not shift UTF-16 offsets.
                val box = Box(item = input)
                items(box.item, State.Ready)
                identity(input)
                loop@ for (value in items(1)) { if (value == 1) break@loop }
            }
        """.trimIndent()
        val fragment = KotlinSourceFragment.from(source, 0, source.length, source)
        val document = compilePreview(source, fragment, windowsLineEndings = true, isScript = false)
        fun kindAt(text: String, skip: Int = 0) = document.spans.first { it.end > source.indexOf(text) + skip }.kind
        assertEquals(KotlinCodeKind.Annotation, kindAt("@Marker class", 1))
        assertEquals(KotlinCodeKind.Annotation, kindAt("class Marker", 6))
        assertEquals(KotlinCodeKind.Property, kindAt("val item", 4))
        assertEquals(KotlinCodeKind.TypeParameter, kindAt("identity(value: T?", "identity(value: ".length))
        assertEquals(KotlinCodeKind.TypeParameter, kindAt("<T> identity", 1))
        assertEquals(KotlinCodeKind.NamedArgument, kindAt("item = input"))
        assertEquals(KotlinCodeKind.Constructor, kindAt("Box(item"))
        assertEquals(KotlinCodeKind.Property, kindAt("box.item", 4))
        assertEquals(KotlinCodeKind.StaticProperty, kindAt("State.Ready", 6))
        assertEquals(KotlinCodeKind.StaticFunctionCall, kindAt("items(box"))
        assertEquals(KotlinCodeKind.StaticFunctionCall, kindAt("identity(input)"))
        assertEquals(KotlinCodeKind.Label, kindAt("loop@"))
        assertEquals(KotlinCodeKind.Label, kindAt("break@loop", 6))
    }

    private fun compilePreview(source: String, fragment: KotlinSourceFragment, windowsLineEndings: Boolean = false, isScript: Boolean = true): KotlinCodeDocument {
        val directory = Files.createTempDirectory("gallery-semantic-test").toFile()
        try {
            val input = File(directory, "Sample.kt").apply { writeText(if (windowsLineEndings) source.replace("\n", "\r\n") else source) }
            val document = KotlinSourceParser().parse(input.name, fragment.source, isScript = isScript)
            val generated = File(directory, "Preview.kt").apply {
                writeText(generateCodeDocuments(listOf("SemanticFixture" to document),
                    mapOf("SemanticFixture" to KotlinCodeOriginData(input.name, fragment))))
            }
            val classes = File(directory, "classes")
            val output = ByteArrayOutputStream()
            val classpath = listOf(KotlinCodeDocument::class.java, Unit::class.java)
                .map { File(it.protectionDomain.codeSource.location.toURI()).absolutePath }.distinct().joinToString(File.pathSeparator)
            val result = K2JVMCompiler().exec(PrintStream(output),
                "-no-stdlib", "-no-reflect", "-jvm-target", "25", "-classpath", classpath,
                "-Xplugin=${System.getProperty("gallery.highlighting.plugin")}",
                "-d", classes.absolutePath, input.absolutePath, generated.absolutePath)
            assertEquals(ExitCode.OK, result, output.toString())
            URLClassLoader(arrayOf(classes.toURI().toURL()), javaClass.classLoader).use { loader ->
                val type = loader.loadClass("io.github.composefluent.winrt.gallery.SemanticFixture")
                return type.getMethod("create").invoke(type.getField("INSTANCE").get(null)) as KotlinCodeDocument
            }
        } finally { directory.deleteRecursively() }
    }
}
