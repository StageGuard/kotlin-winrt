package io.github.composefluent.winrt.gallery.layout

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.ToggleButton
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "Expander", title = "Expander", group = "Layout", order = 2)
internal fun expanderPage() = ExamplePage {
    val expander = expanderBasicSample()
    example("An Expander with text header and content.", expander, select("ExpandDirection", listOf("Down", "Up")) {
        expander.expandDirection = if (it == 0) ExpandDirection.Down else ExpandDirection.Up
        expander.verticalAlignment = if (it == 0) VerticalAlignment.Top else VerticalAlignment.Bottom
    })
    example("Modifying an Expander's content alignment.", expanderAlignmentSample())
}

@GallerySample(route = "Expander", title = "Modifying an Expander's content alignment.")
internal fun expanderAlignmentSample() = Expander().apply {
        width = 500.0; padding = inset(0.0); horizontalContentAlignment = HorizontalAlignment.Left
        header = TextBlock().apply { this.text = "This text is centered"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { horizontalAlignment = HorizontalAlignment.Center }
        content = TextBlock().apply { this.text = "And this text is left aligned"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(4.0) }
    }

@GallerySample(route = "Expander", title = "An Expander with text header and content.")
internal fun expanderBasicSample() = Expander().apply { header = "This text is in the header"; content = "This is in the content" }
