package io.github.composefluent.winrt.gallery.text

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.documents.*
import microsoft.ui.xaml.media.FontFamily
import windows.foundation.Uri
import windows.ui.text.FontStyle
import windows.ui.text.FontWeight

@GalleryPage(route = "TextBox", title = "TextBox", group = "Text", order = 6)
internal fun textBoxPage() = ExamplePage {
    example("A simple TextBox.", textBoxSimpleTextBoxSample())
    example("A TextBox with a header and placeholder text.", textBoxTextBoxWithAHeaderAndPlaceholderTextSample1())
    example("A read-only TextBox with various properties.", textBoxReadOnlyTextBoxWithVariousPropertiesSample2())
    example("A multiline TextBox with spell checking.", textBoxMultilineTextBoxWithSpellCheckingSample3())
}

@GallerySample(route = "TextBox", title = "A simple TextBox.")
internal fun textBoxSimpleTextBoxSample() = TextBox().apply { named(this, "simple TextBox") }

@GallerySample(route = "TextBox", title = "A TextBox with a header and placeholder text.")
internal fun textBoxTextBoxWithAHeaderAndPlaceholderTextSample1() = TextBox().apply { header = "Enter your name:"; placeholderText = "Name" }

@GallerySample(route = "TextBox", title = "A read-only TextBox with various properties.")
internal fun textBoxReadOnlyTextBoxWithVariousPropertiesSample2() = TextBox().apply {
        characterSpacing = 200; fontFamily = FontFamily("Arial"); fontSize = 24.0; fontStyle = FontStyle.Italic
        foreground = brush(0x5178BEu); isReadOnly = true; text = "I am super excited to be here!"
    }

@GallerySample(route = "TextBox", title = "A multiline TextBox with spell checking.")
internal fun textBoxMultilineTextBoxWithSpellCheckingSample3() = TextBox().apply {
        minWidth = 400.0; acceptsReturn = true; isSpellCheckEnabled = true; selectionHighlightColor = brush(0x008000u); textWrapping = TextWrapping.Wrap
    }
