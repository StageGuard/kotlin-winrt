package io.github.composefluent.winrt.gallery.layout

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "Canvas", title = "Canvas", group = "Layout", order = 1)
internal fun canvasPage() = ExamplePage {
    val red = tile(layoutColors[0])

    val canvas = canvasSample(red)
    example("A Canvas control.", canvas, stack(16.0, true) {
        children.add(range("Canvas.Top", 0.0, 0.0, 100.0) { Canvas.setTop(red, it) }.apply {
            orientation = Orientation.Vertical; height = 110.0; width = Double.NaN; isDirectionReversed = true
        })
        children.add(stack {
            children.add(range("Canvas.Left", 0.0, 0.0, 100.0) { Canvas.setLeft(red, it) })
            children.add(range("Canvas.ZIndex", 0.0, 0.0, 4.0) { Canvas.setZIndex(red, it.toInt()) })
        })
    })
}

@GallerySample(route = "Canvas", title = "A Canvas control.")
internal fun canvasSample(red: Rectangle) = Canvas().apply {
    width = 140.0; height = 140.0; background = brush(0x808080u)
    children.add(red)
    layoutColors.drop(1).forEachIndexed { index, color -> children.add(tile(color).apply {
        Canvas.setLeft(this, (index + 1) * 20.0); Canvas.setTop(this, (index + 1) * 20.0); Canvas.setZIndex(this, index + 1)
    }) }
}
