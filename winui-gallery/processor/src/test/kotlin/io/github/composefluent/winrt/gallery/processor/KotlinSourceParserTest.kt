package io.github.composefluent.winrt.gallery.processor

import io.github.composefluent.winrt.gallery.code.*
import kotlin.test.*

class KotlinSourceParserTest {
    @Test fun distinguishes_function_calls_from_bindings_and_type_names() {
        val source = """package sample
import sample.Widget
fun render(input: Widget) {
    val control = Widget()
    control.content = input
    control.click.add { _, _ -> show(control) }
}
fun show(value: Widget) {}
"""
        val document = KotlinSourceParser().parse("Sample.kt", source)
        fun kindAt(offset: Int): KotlinCodeKind = document.spans.first { it.end > offset }.kind

        assertEquals(KotlinCodeKind.Function, kindAt(source.indexOf("render(input")))
        assertEquals(KotlinCodeKind.FunctionCall, kindAt(source.indexOf("show(control")))
        assertEquals(KotlinCodeKind.FunctionCall, kindAt(source.indexOf("add {")))
        assertEquals(KotlinCodeKind.Variable, kindAt(source.indexOf("control =")))
        assertEquals(KotlinCodeKind.Variable, kindAt(source.indexOf("control.content")))
        assertEquals(KotlinCodeKind.Parameter, kindAt(source.indexOf("input: Widget")))
        assertEquals(KotlinCodeKind.Plain, kindAt(source.indexOf("Widget", source.indexOf("input:"))))
        assertEquals(KotlinCodeKind.Plain, kindAt(source.indexOf("Widget", source.indexOf("import"))))
    }

    @Test fun preserves_source_and_parses_nested_comments_templates_and_declarations() {
        val source = "/** Documentation */\n@Sample(\"fun\")\ninternal fun `show code`(): String {\n" +
            "    /* outer /* nested */ end */\n    val count = 0x2A\n" +
            "    return \"你好 😀 \\n \${count + 1}\"\n}\n"
        KotlinSourceParser().let { parser ->
            val document = parser.parse("Sample.kt", source)
            var start = 0
            val pieces = document.spans.map { span ->
                (source.substring(start, span.end) to span.kind).also { start = span.end }
            }
            assertEquals(source, pieces.joinToString("") { it.first })
            assertTrue(pieces.any { it.first == "`show code`" && it.second == KotlinCodeKind.Function })
            assertTrue(pieces.any { it.first == "0x2A" && it.second == KotlinCodeKind.Number })
            assertTrue(pieces.any { it.first.contains("outer /* nested */ end") && it.second == KotlinCodeKind.Comment })
            assertTrue(pieces.any { it.first == "fun" && it.second == KotlinCodeKind.Keyword })
            assertTrue(pieces.any { it.first == "\\n" && it.second == KotlinCodeKind.Escape })
            assertTrue(pieces.any { it.second == KotlinCodeKind.Documentation })
            assertEquals(document, parser.parse("Sample.kt", source))
        }
    }

    @Test fun highlights_infix_calls_as_functions() {
        val source = """val pairs = listOf("Recent" to Symbol.Clock, "Shared" `to` Symbol.Share)
val contains = 1 in listOf(1)
val cast = value as String
"""
        val document = KotlinSourceParser().parse("Sample.kts", source)
        fun kindAt(offset: Int): KotlinCodeKind = document.spans.first { it.end > offset }.kind

        assertEquals(KotlinCodeKind.FunctionCall, kindAt(source.indexOf("to Symbol.Clock")))
        assertEquals(KotlinCodeKind.FunctionCall, kindAt(source.indexOf("`to` Symbol.Share")))
        assertEquals(KotlinCodeKind.FunctionCall, kindAt(source.indexOf("listOf(")))
        assertEquals(KotlinCodeKind.Keyword, kindAt(source.indexOf("in listOf")))
        assertEquals(KotlinCodeKind.Keyword, kindAt(source.indexOf("as String")))
    }

    @Test fun handles_raw_strings_empty_and_incomplete_source_without_dropping_text() {
        KotlinSourceParser().let { parser ->
            for (source in listOf("", "val text = \"\"\"if (true) // literal\n\${1 + 2}\"\"\"", "fun unfinished(\n")) {
                val document = parser.parse("Preview.kt", source)
                assertEquals(source.length, document.spans.lastOrNull()?.end ?: 0)
                assertEquals(source, document.source)
            }
        }
    }

    @Test fun generated_source_escapes_kotlin_templates_and_chunks_large_documents() {
        val source = "\"\"\"\n\$name\n".repeat(1000)
        val document = KotlinCodeDocument("Large.kt", source, listOf(KotlinCodeSpan(source.length, KotlinCodeKind.String)))
        val generated = generateCodeDocument("GalleryCode0", document)
        assertTrue(generated.contains("\\\$name"))
        assertTrue(generated.contains("joinToString"))
        assertEquals(generated, generateCodeDocument("GalleryCode0", document))
        assertFailsWith<IllegalArgumentException> { KotlinCodeDocument("Bad.kt", "text", listOf(KotlinCodeSpan(3, KotlinCodeKind.Plain))) }
    }
}
