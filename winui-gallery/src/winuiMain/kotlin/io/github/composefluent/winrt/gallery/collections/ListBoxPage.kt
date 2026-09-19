package io.github.composefluent.winrt.gallery.collections

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.Thickness
import microsoft.ui.xaml.controls.ListBox
import microsoft.ui.xaml.media.FontFamily
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "ListBox", title = "ListBox", group = "Collections", order = 4, glyph = "\uE8A9")
internal fun listBoxPage() = ExamplePage {
    val colorOutput = Rectangle().apply {
        width = 100.0
        height = 30.0
        margin = Thickness(0.0, 10.0, 0.0, 0.0)
    }
    example("A ListBox with items defined inline and its minimum width set.", stack(0.0) {
        children.add(listBoxInlineItemsSample { color -> colorOutput.fill = brush(color) })
        children.add(colorOutput)
    })

    val fontOutput = label("You can set the font used for this text.").apply {
        margin = Thickness(0.0, 10.0, 0.0, 0.0)
        fontFamily = FontFamily("Courier New")
    }
    example("A ListBox with its ItemsSource and Height set.", stack(0.0) {
        children.add(listBoxItemsSourceSample { font -> fontOutput.fontFamily = FontFamily(font) })
        children.add(fontOutput)
    })
}

@GallerySample(route = "ListBox", title = "A ListBox with items defined inline and its minimum width set.")
internal fun listBoxInlineItemsSample(onColorSelected: (UInt) -> Unit) = ListBox().apply {
    minWidth = 200.0
    listOf("Blue", "Green", "Red", "Yellow").forEach { items.add(it) }
    selectionChanged.add { _, _ ->
        val color = when (selectedItem?.toString()) {
            "Blue" -> 0x0000FFu
            "Green" -> 0x008000u
            "Red" -> 0xFF0000u
            "Yellow" -> 0xFFFF00u
            else -> null
        }
        color?.let(onColorSelected)
    }
}

@GallerySample(route = "ListBox", title = "A ListBox with its ItemsSource and Height set.")
internal fun listBoxItemsSourceSample(onFontSelected: (String) -> Unit) = ListBox().apply {
    height = 164.0
    itemsSource = listOf("Arial", "Comic Sans MS", "Courier New", "Segoe UI", "Times New Roman")
    selectionChanged.add { _, _ -> selectedItem?.toString()?.let(onFontSelected) }
    selectedIndex = 2
}
