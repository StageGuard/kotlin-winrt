package io.github.composefluent.winrt.gallery.layout

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.ToggleButton
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "VariableSizedWrapGrid", title = "VariableSizedWrapGrid", group = "Layout", order = 7)
internal fun variableSizedWrapGridPage() = ExamplePage {
    val panel = variableSizedWrapGridSample()
    example("A VariableSizedWrapGrid control.", panel, choices("Orientation", listOf("Horizontal", "Vertical"), 1) {
        panel.orientation = if (it == 0) Orientation.Horizontal else Orientation.Vertical
    })
}

@GallerySample(route = "VariableSizedWrapGrid", title = "A VariableSizedWrapGrid control.")
internal fun variableSizedWrapGridSample() = VariableSizedWrapGrid().apply {
        width = 400.0; itemWidth = 44.0; itemHeight = 44.0; maximumRowsOrColumns = 3
        children.add(tile(layoutColors[0]))
        children.add(tile(layoutColors[1]).apply { height = 80.0; VariableSizedWrapGrid.setRowSpan(this, 2) })
        children.add(tile(layoutColors[2]).apply { width = 80.0; VariableSizedWrapGrid.setColumnSpan(this, 2) })
        children.add(tile(layoutColors[3], 80.0).apply { VariableSizedWrapGrid.setRowSpan(this, 2); VariableSizedWrapGrid.setColumnSpan(this, 2) })
    }
