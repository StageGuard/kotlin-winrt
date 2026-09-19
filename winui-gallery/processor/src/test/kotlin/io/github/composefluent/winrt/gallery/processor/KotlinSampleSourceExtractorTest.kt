package io.github.composefluent.winrt.gallery.processor

import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinSampleSourceExtractorTest {
    @Test fun extracts_only_the_sample_expression() {
        val source = """package sample
@GallerySample(route = "SelectorBar", title = "A basic SelectorBar.")
fun basicSelectorBarSample() = SelectorBar().apply {
    listOf("Recent" to Symbol.Clock).forEach { (title, symbol) ->
        items.add(SelectorBarItem().apply { text = title; icon = SymbolIcon(symbol) })
    }
}
@GalleryPage(route = "SelectorBar", title = "SelectorBar", group = "Navigation", order = 3)
fun selectorBarPage() = stack {
    children.add(example("A basic SelectorBar.", basicSelectorBarSample()))
}
"""
        assertEquals(
            """SelectorBar().apply {
    listOf("Recent" to Symbol.Clock).forEach { (title, symbol) ->
        items.add(SelectorBarItem().apply { text = title; icon = SymbolIcon(symbol) })
    }
}""",
            KotlinSampleSourceExtractor().extract(source, "basicSelectorBarSample"),
        )
    }
}
