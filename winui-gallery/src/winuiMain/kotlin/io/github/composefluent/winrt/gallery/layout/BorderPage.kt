package io.github.composefluent.winrt.gallery.layout

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.ToggleButton
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "Border", title = "Border", group = "Layout", order = 0)
internal fun borderPage() = ExamplePage {
    val border = borderBorderAroundATextBlockSample()
    example("A Border around a TextBlock.", border, stack {
        children.add(range("BorderThickness", 2.0, 0.0, 10.0) { border.borderThickness = inset(it) })
        children.add(stack(8.0, true) {
            val names = listOf("Green", "Yellow", "Blue", "White")
            children.add(choices("Background", names, 3) { border.background = brush(listOf(0x008000u, 0xFFFF00u, 0x0000FFu, 0xFFFFFFu)[it]) })
            children.add(choices("BorderBrush", names, 1) { border.borderBrush = brush(listOf(0x006400u, 0xFFD700u, 0x00008Bu, 0xFFFFFFu)[it]) })
        })
    })
}

@GallerySample(route = "Border", title = "A Border around a TextBlock.")
internal fun borderBorderAroundATextBlockSample() = Border().apply {
        borderThickness = inset(2.0); background = brush(0xFFFFFFu); borderBrush = brush(0xFFD700u)
        child = TextBlock().apply { this.text = "Text inside a border"; this.fontSize = 18.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(8.0, 5.0, 8.0, 5.0); foreground = brush(0u) }
    }
