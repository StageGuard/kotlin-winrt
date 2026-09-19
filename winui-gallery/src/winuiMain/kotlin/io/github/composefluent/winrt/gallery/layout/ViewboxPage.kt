package io.github.composefluent.winrt.gallery.layout

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.ToggleButton
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "Viewbox", title = "Viewbox", group = "Layout", order = 8)
internal fun viewboxPage() = ExamplePage {
    val viewbox = viewboxContentInsideAViewboxSample()
    example("Content inside a Viewbox.", viewbox, stack {
        children.add(range("Width/Height", 200.0, 20.0, 300.0) { viewbox.width = it; viewbox.height = it })
        children.add(choices("Stretch", listOf("None", "Fill", "Uniform", "UniformToFill"), 2) { viewbox.stretch = listOf(Stretch.None, Stretch.Fill, Stretch.Uniform, Stretch.UniformToFill)[it] })
        children.add(choices("StretchDirection", listOf("UpOnly", "DownOnly", "Both"), 2) { viewbox.stretchDirection = listOf(StretchDirection.UpOnly, StretchDirection.DownOnly, StretchDirection.Both)[it] })
    })
}

@GallerySample(route = "Viewbox", title = "Content inside a Viewbox.")
internal fun viewboxContentInsideAViewboxSample() = Viewbox().apply {
        width = 200.0; height = 200.0
        child = Border().apply {
            borderBrush = brush(0x808080u); borderThickness = inset(15.0)
            child = StackPanel().apply { this.spacing = 0.0; background = brush(0xA9A9A9u)
                children.add(StackPanel().apply { this.spacing = 0.0; this.orientation = Orientation.Horizontal; listOf(0x0000FFu, 0x008000u, 0xFF0000u, 0xFFFF00u).forEach { children.add(tile(it).apply { height = 10.0 }) } })
                children.add(Image().apply { this.width = 160.0; this.height = 160.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/Slices.png") ) })
                children.add(TextBlock().apply { this.text = "This is text."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { horizontalAlignment = HorizontalAlignment.Center }) }
        }
    }
