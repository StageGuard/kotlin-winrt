package io.github.composefluent.winrt.gallery.layout

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.ToggleButton
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "StackPanel", title = "StackPanel", group = "Layout", order = 6)
internal fun stackPanelPage() = ExamplePage {
    val panel = stackPanelStackPanelControlSample()
    example("A StackPanel control.", panel, stack {
        children.add(choices("Orientation", listOf("Horizontal", "Vertical"), 1) { panel.orientation = if (it == 0) Orientation.Horizontal else Orientation.Vertical })
        children.add(range("Spacing", 8.0, 0.0, 16.0) { panel.spacing = it })
    })
}

@GallerySample(route = "StackPanel", title = "A StackPanel control.")
internal fun stackPanelStackPanelControlSample() = StackPanel().apply {
    spacing = 8.0
    listOf(windows.ui.Color(255u, 255u, 0u, 0u), windows.ui.Color(255u, 0u, 0u, 255u), windows.ui.Color(255u, 0u, 128u, 0u), windows.ui.Color(255u, 255u, 255u, 0u)).forEach { color ->
        children.add(Rectangle().apply {
            width = 40.0
            height = 40.0
            fill = microsoft.ui.xaml.media.SolidColorBrush(color)
        })
    }
}
