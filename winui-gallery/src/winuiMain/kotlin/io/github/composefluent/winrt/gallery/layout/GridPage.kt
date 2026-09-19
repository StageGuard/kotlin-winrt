package io.github.composefluent.winrt.gallery.layout

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "Grid", title = "Grid", group = "Layout", order = 3)
internal fun gridPage() = ExamplePage {
    val red = tile(layoutColors[0], 50.0)

    val grid = gridSample(red)
    example("A 3x3 Grid control.", grid, Grid().apply {
        columnSpacing = 12.0; rowSpacing = 12.0
        repeat(2) { columnDefinitions.add(column(1.0, GridUnitType.Auto)) }
        repeat(4) { rowDefinitions.add(autoRow()) }
        fun slider(title: String, initial: Double, maximum: Double, row: Int, vertical: Boolean, changed: (Double) -> Unit) {
            children.add(range(title, initial, 0.0, maximum, changed).apply {
                Grid.setRow(this, row); Grid.setColumn(this, if (vertical) 1 else 0)
                stepFrequency = 1.0; tickFrequency = 1.0; snapsTo = microsoft.ui.xaml.controls.primitives.SliderSnapsTo.Ticks
                if (vertical) {
                    orientation = Orientation.Vertical; height = 100.0; width = Double.NaN
                    verticalAlignment = VerticalAlignment.Top; isDirectionReversed = true
                } else { width = 140.0; margin = Thickness(16.0, 0.0, 0.0, 0.0) }
            })
        }
        children.add(label("Grid"))
        slider("ColumnSpacing", 8.0, 16.0, 1, false) { grid.columnSpacing = it }
        slider("RowSpacing", 8.0, 16.0, 1, true) { grid.rowSpacing = it }
        children.add(label("Red block").apply { Grid.setRow(this, 2) })
        slider("Grid.Column", 0.0, 2.0, 3, false) { Grid.setColumn(red, it.toInt()) }
        slider("Grid.Row", 0.0, 2.0, 3, true) { Grid.setRow(red, it.toInt()) }
    })
}

@GallerySample(route = "Grid", title = "A 3x3 Grid control.")
internal fun gridSample(red: Rectangle) = Grid().apply {
    width = 240.0; height = 160.0; background = brush(0x808080u)
    rowSpacing = 8.0; columnSpacing = 8.0
    repeat(3) {
        columnDefinitions.add(column(50.0, GridUnitType.Pixel))
        rowDefinitions.add(RowDefinition().apply { height = GridLength(50.0, GridUnitType.Pixel) })
    }
    children.add(red)
    children.add(tile(layoutColors[1], 50.0).apply { Grid.setRow(this, 1) })
    children.add(tile(layoutColors[2], 50.0).apply { Grid.setColumn(this, 1) })
    children.add(tile(layoutColors[3], 50.0).apply { Grid.setColumn(this, 1); Grid.setRow(this, 1) })
}
