package io.github.composefluent.winrt.gallery.layout

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.ToggleButton
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "RelativePanel", title = "RelativePanel", group = "Layout", order = 4)
internal fun relativePanelPage() = ExamplePage {
    example("A RelativePanel control.", relativePanelSample())
}

@GallerySample(route = "RelativePanel", title = "A RelativePanel control.")
internal fun relativePanelSample() = RelativePanel().apply {
        width = 300.0
        val red = tile(layoutColors[0], 50.0)
        val blue = tile(layoutColors[1], 50.0).apply { margin = Thickness(8.0, 0.0, 0.0, 0.0) }
        val green = tile(layoutColors[2], 50.0)
        val yellow = tile(layoutColors[3], 50.0).apply { margin = Thickness(0.0, 8.0, 0.0, 0.0) }
        RelativePanel.setRightOf(blue, red); RelativePanel.setAlignRightWithPanel(green, true)
        RelativePanel.setBelow(yellow, green); RelativePanel.setAlignHorizontalCenterWith(yellow, green)
        listOf(red, blue, green, yellow).forEach { children.add(it) }
    }
