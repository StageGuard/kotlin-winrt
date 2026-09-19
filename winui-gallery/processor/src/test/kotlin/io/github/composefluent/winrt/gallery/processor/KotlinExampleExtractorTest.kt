package io.github.composefluent.winrt.gallery.processor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KotlinExampleExtractorTest {
    @Test fun separates_sample_calls_and_keeps_their_local_setup() {
        val source = """package samples
@GalleryPage(route = "First", title = "First", group = "Test", order = 0)
fun first() = stack {
    val before = label("first")
    children.add(example("First example", before))
    val after = label("second")
    children.add(example("Second example", stack {
        children.add(after)
    }))
}
@GalleryPage(route = "Other", title = "Other", group = "Test", order = 1)
fun other() = stack {
    children.add(example("Other example", label("other")))
}
"""
        val examples = KotlinExampleExtractor().extract(source, "first")
        assertEquals(2, examples.size)
        assertEquals(listOf("First example"), examples[0].titles)
        assertEquals(listOf("Second example"), examples[1].titles)
        assertTrue(examples[0].source.startsWith("val before"))
        assertTrue(examples[0].source.contains("example(\"First example\""))
        assertFalse(examples[0].source.contains("Second example"))
        assertTrue(examples[1].source.startsWith("val after"))
        assertFalse(examples[1].source.contains("Other example"))
    }

    @Test fun maps_all_literal_titles_from_a_repeated_example() {
        val source = """fun page() = stack {
    for (value in listOf(0, 1)) {
        children.add(example(if (value == 0) "First" else "Second", label("demo")))
    }
}"""
        val example = KotlinExampleExtractor().extract(source, "page").single()
        assertEquals(listOf("First", "Second"), example.titles)
        assertTrue(example.source.contains("for (value"))
        assertTrue(example.source.contains("example(if"))
    }
}
