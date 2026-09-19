package io.github.composefluent.winrt.gallery.styles

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.*
import microsoft.ui.xaml.shapes.*
import windows.foundation.Point
import windows.foundation.Rect

@GalleryPage(route = "Shape", title = "Shape", group = "Styles", order = 5)
internal fun shapePage() = ExamplePage {
    fun dimensions(shape: Shape) = stack {
        children.add(geometryRange("Height", 100.0, 100.0, 150.0) { shape.height = it })
        children.add(geometryRange("Width", 100.0, 100.0, 150.0) { shape.width = it })
        children.add(geometryRange("Stroke Thickness", 2.0, 2.0, 10.0) { shape.strokeThickness = it })
    }
    val ellipse = shapeEllipseSample()
    example("Ellipse", ellipse, dimensions(ellipse))
    val rectangle = shapeRectangleSample1()
    example("Rectangle", rectangle, dimensions(rectangle).apply {
        children.add(geometryRange("Radius Y", 0.0, 0.0, 100.0) { rectangle.radiusY = it })
        children.add(geometryRange("Radius X", 0.0, 0.0, 100.0) { rectangle.radiusX = it })
    })
    val polygon = shapePolygonSample()

    val canvas = Canvas().apply {
        width = 320.0; height = 200.0
        children.add(stack(0.0) {
            children.add(label("A polygon is a connected series of lines that form a closed shape.").apply { margin = Thickness(0.0, 0.0, 0.0, 15.0) })
            children.add(polygon)
        })
    }
    val points = pointLabels(canvas, listOf(Triple("Point #1: (10,100)", 0.0, 150.0), Triple("Point #2: (60,40)", 50.0, 40.0), Triple("Point #3: (200,40)", 200.0, 40.0), Triple("Point #4: (250,100)", 240.0, 150.0)))
    example("Polygon", canvas, stack {
        children.add(points); children.add(geometryRange("Stroke Thickness", 2.0, 2.0, 10.0) { polygon.strokeThickness = it })
    })

}

@GallerySample(route = "Shape", title = "Ellipse")
internal fun shapeEllipseSample() = Ellipse().apply { width = 100.0; height = 100.0; fill = brush(0x4682B4u); stroke = brush(0u); strokeThickness = 2.0 }

@GallerySample(route = "Shape", title = "Rectangle")
internal fun shapeRectangleSample1() = Rectangle().apply { width = 100.0; height = 100.0; fill = brush(0x4682B4u); stroke = brush(0u); strokeThickness = 2.0 }

@GallerySample(route = "Shape", title = "Polygon")
internal fun shapePolygonSample() = Polygon().apply {
    fill = brush(0x4682B4u)
    stroke = brush(0u)
    strokeThickness = 2.0
    points = PointCollection().apply {
        listOf(Point(10f, 100f), Point(60f, 40f), Point(200f, 40f), Point(250f, 100f)).forEach(::add)
    }
}
