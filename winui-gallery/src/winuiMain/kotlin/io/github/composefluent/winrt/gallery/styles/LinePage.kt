package io.github.composefluent.winrt.gallery.styles

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.*
import microsoft.ui.xaml.shapes.*
import windows.foundation.Point
import windows.foundation.Rect

@GalleryPage(route = "Line", title = "Line", group = "Styles", order = 4)
internal fun linePage() = ExamplePage {
    val line = lineShapeSample()

    example("Line", Canvas().apply { width = 100.0; height = 200.0; children.add(line) }, stack {
        children.add(geometryRange("Start point X", 0.0, 0.0, 100.0) { line.x1 = it })
        children.add(geometryRange("Start point Y", 0.0, 0.0, 100.0) { line.y1 = it })
        children.add(geometryRange("End point X", 200.0, 200.0, 300.0) { line.x2 = it })
        children.add(geometryRange("End point Y", 0.0, 0.0, 100.0) { line.y2 = it })
        children.add(geometryRange("Stroke Thickness", 5.0, 5.0, 10.0) { line.strokeThickness = it })
    })
    val polyline = polylineShapeSample()

    val polyCanvas = Canvas().apply {
        width = 320.0; height = 170.0
        children.add(stack(0.0) { children.add(label("Draws a series of connected straight lines.").apply { margin = Thickness(0.0, 0.0, 0.0, 10.0) }); children.add(polyline) })
    }
    val polyPoints = pointLabels(polyCanvas, listOf(Triple("Point #1: (10,100)", 0.0, 140.0), Triple("Point #2: (60,40)", 50.0, 40.0), Triple("Point #3: (200,40)", 200.0, 40.0), Triple("Point #4: (250,100)", 240.0, 140.0)))
    example("Polyline", polyCanvas, stack { children.add(polyPoints); children.add(geometryRange("Stroke Thickness", 2.0, 2.0, 10.0) { polyline.strokeThickness = it }) })

    val path = pathShapeSample()

    val pathCanvas = Canvas().apply {
        width = 320.0; height = 200.0
        children.add(stack(0.0) { children.add(label("Draws a series of connected lines and curves.")); children.add(path) })
    }
    val pathPoints = pointLabels(pathCanvas, listOf(Triple("Point #1: (10,100)", 0.0, 130.0), Triple("Point #2: (100,25)", 40.0, 75.0), Triple("Point #3: (300,250)", 280.0, 175.0), Triple("Point #4: (400,75)", 360.0, 60.0), Triple("Point #5: (200,75)", 170.0, 60.0)))
    example("Path", pathCanvas, stack { children.add(pathPoints); children.add(geometryRange("Stroke Thickness", 2.0, 2.0, 10.0) { path.strokeThickness = it }) })

    val geometryGroupPath = geometryGroupShapeSample()
    val ellipse = checkNotNull(geometryGroupPath.data).asWinRT<GeometryGroup>().children[1].asWinRT<EllipseGeometry>()

    example("GeometryGroup", Canvas().apply {
        width = 100.0; height = 170.0
        children.add(stack(0.0) {
            children.add(label("Composite geometry objects can be created using a GeometryGroup.").apply { margin = Thickness(0.0, 0.0, 0.0, 15.0) })
            children.add(geometryGroupPath)
        })
    }, stack {
        children.add(geometryRange("RadiusX", 30.0, 30.0, 40.0) { ellipse.radiusX = it })
        children.add(geometryRange("RadiusY", 30.0, 30.0, 50.0) { ellipse.radiusY = it })
    })




}

@GallerySample(route = "Line", title = "Line")
internal fun lineShapeSample() = Line().apply {
    x2 = 200.0
    stroke = brush(0x4682B4u)
    strokeThickness = 5.0
    Canvas.setTop(this, 50.0)
}

@GallerySample(route = "Line", title = "Polyline")
internal fun polylineShapeSample() = Polyline().apply {
    stroke = brush(0u)
    strokeThickness = 2.0
    points = PointCollection().apply {
        listOf(Point(10f, 100f), Point(60f, 40f), Point(200f, 40f), Point(250f, 100f)).forEach(::add)
    }
}

@GallerySample(route = "Line", title = "Path")
internal fun pathShapeSample() = Path().apply {
    stroke = brush(0xB8860Bu)
    strokeThickness = 2.0
    data = PathGeometry().apply {
        figures.add(PathFigure().apply {
            startPoint = Point(10f, 100f)
            isClosed = false
            segments.add(BezierSegment().apply {
                point1 = Point(100f, 25f); point2 = Point(300f, 250f); point3 = Point(400f, 75f)
            })
            segments.add(LineSegment().apply { point = Point(200f, 75f) })
        })
    }
}

@GallerySample(route = "Line", title = "GeometryGroup")
internal fun geometryGroupShapeSample() = Path().apply {
    fill = brush(0xCCCCFFu)
    stroke = brush(0u)
    strokeThickness = 4.0
    data = GeometryGroup().apply {
        fillRule = FillRule.EvenOdd
        children.add(LineGeometry().apply { startPoint = Point(10f, 10f); endPoint = Point(50f, 30f) })
        children.add(EllipseGeometry().apply { center = Point(40f, 70f); radiusX = 30.0; radiusY = 30.0 })
        children.add(RectangleGeometry().apply { rect = Rect(30f, 55f, 100f, 30f) })
    }
}
