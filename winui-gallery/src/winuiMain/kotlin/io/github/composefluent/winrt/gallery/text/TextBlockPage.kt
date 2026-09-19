package io.github.composefluent.winrt.gallery.text

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.documents.*
import microsoft.ui.xaml.media.FontFamily
import windows.foundation.Uri
import windows.ui.text.FontStyle
import windows.ui.text.FontWeight

@GalleryPage(route = "TextBlock", title = "TextBlock", group = "Text", order = 5)
internal fun textBlockPage() = ExamplePage {
    example("A simple TextBlock.", textBlockSimpleTextBlockSample())
    example("A TextBlock with a style applied.", textBlockTextBlockWithAStyleAppliedSample1())
    example("A TextBlock with various properties.", textBlockTextBlockWithVariousPropertiesSample2())
    example("A TextBlock with inline text elements.", textBlockTextBlockWithInlineTextElementsSample3())
    val selectable = textBlockSelectableTextBlockSample4()
    example("A selectable TextBlock.", selectable, ToggleSwitch().apply { header = "IsTextSelectionEnabled"; toggled.add { _, _ -> selectable.isTextSelectionEnabled = isOn } })
}

@GallerySample(route = "TextBlock", title = "A simple TextBlock.")
internal fun textBlockSimpleTextBlockSample() = TextBlock().apply { text = "I am a TextBlock."; textWrapping = TextWrapping.Wrap }

@GallerySample(route = "TextBlock", title = "A TextBlock with a style applied.")
internal fun textBlockTextBlockWithAStyleAppliedSample1() = TextBlock().apply { text = "I am a styled TextBlock."; textWrapping = TextWrapping.Wrap; fontFamily = FontFamily("Comic Sans MS"); fontStyle = FontStyle.Italic }

@GallerySample(route = "TextBlock", title = "A TextBlock with various properties.")
internal fun textBlockTextBlockWithVariousPropertiesSample2() = TextBlock().apply {
        text = "I am super excited to be here!"; fontSize = 24.0; textWrapping = TextWrapping.Wrap
        characterSpacing = 200; fontFamily = FontFamily("Arial"); fontStyle = FontStyle.Italic; foreground = microsoft.ui.xaml.media.SolidColorBrush(windows.ui.Color(255u, 100u, 149u, 237u))
    }

@GallerySample(route = "TextBlock", title = "A TextBlock with inline text elements.")
internal fun textBlockTextBlockWithInlineTextElementsSample3() = TextBlock().apply {
        inlines.add(Run().apply { text = "Text in a TextBlock doesn't have to be a simple string." }.apply { fontFamily = FontFamily("Times New Roman"); foreground = microsoft.ui.xaml.media.SolidColorBrush(windows.ui.Color(255u, 169u, 169u, 169u)) })
        inlines.add(LineBreak()); inlines.add(Run().apply { text = "Text can be " })
        inlines.add(Bold().apply { inlines.add(Run().apply { text = "bold" }) }); inlines.add(Run().apply { text = ", " })
        inlines.add(Italic().apply { inlines.add(Run().apply { text = "italic" }) }); inlines.add(Run().apply { text = ", or " })
        inlines.add(Underline().apply { inlines.add(Run().apply { text = "underlined" }) }); inlines.add(Run().apply { text = "." })
    }

@GallerySample(route = "TextBlock", title = "A selectable TextBlock.")
internal fun textBlockSelectableTextBlockSample4() = TextBlock().apply { text = "I am a selectable TextBlock with custom SelectionHighlightColor."; textWrapping = TextWrapping.Wrap; selectionHighlightColor = microsoft.ui.xaml.media.SolidColorBrush(windows.ui.Color(255u, 255u, 140u, 0u)) }
