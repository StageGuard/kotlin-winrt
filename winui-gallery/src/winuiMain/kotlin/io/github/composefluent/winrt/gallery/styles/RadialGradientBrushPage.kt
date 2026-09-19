package io.github.composefluent.winrt.gallery.styles

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.*
import microsoft.ui.xaml.shapes.*
import windows.foundation.Point
import windows.foundation.Rect

@GalleryPage(route = "RadialGradientBrush", title = "RadialGradientBrush", group = "Styles", order = 6)
internal fun radialGradientPage() = ExamplePage {
    val sample = radialGradientBrushSample()
    val gradient = checkNotNull(sample.fill).asWinRT<RadialGradientBrush>()
    val sliders = listOf("Center.X", "Center.Y", "RadiusX", "RadiusY", "GradientOrigin.X", "GradientOrigin.Y").map { title ->
        Slider().apply { header = title; minimum = 0.0; maximum = 1.0; value = 0.5; stepFrequency = 0.02; smallChange = 0.05; width = 140.0 }
    }
    fun update() {
        gradient.center = Point(sliders[0].value.toFloat(), sliders[1].value.toFloat())
        gradient.radiusX = sliders[2].value; gradient.radiusY = sliders[3].value
        gradient.gradientOrigin = Point(sliders[4].value.toFloat(), sliders[5].value.toFloat())
    }
    sliders.forEach { slider -> slider.valueChanged.add { _, _ -> update() } }

    example("RadialGradientBrush sample.", sample, stack {
        children.add(select("MappingMode", listOf("RelativeToBoundingBox", "Absolute")) { mode ->
            gradient.mappingMode = if (mode == 0) BrushMappingMode.RelativeToBoundingBox else BrushMappingMode.Absolute
            sliders.forEach { slider ->
                slider.maximum = if (mode == 0) 1.0 else 200.0
                slider.value = slider.maximum / 2.0
                slider.stepFrequency = if (mode == 0) 0.02 else 4.0
                slider.smallChange = if (mode == 0) 0.05 else 10.0
            }
        })
        sliders.chunked(2).forEach { pair -> children.add(stack(8.0, true) { pair.forEach { children.add(it) } }) }
        children.add(select("SpreadMethod", listOf("Pad", "Reflect", "Repeat")) { gradient.spreadMethod = listOf(GradientSpreadMethod.Pad, GradientSpreadMethod.Reflect, GradientSpreadMethod.Repeat)[it] })
    })

}

@GallerySample(route = "RadialGradientBrush", title = "RadialGradientBrush sample.")
internal fun radialGradientBrushSample() = Rectangle().apply {
    width = 200.0; height = 200.0
    fill = RadialGradientBrush().apply {
        center = Point(0.5f, 0.5f); gradientOrigin = Point(0.5f, 0.5f); radiusX = 0.5; radiusY = 0.5
        mappingMode = BrushMappingMode.RelativeToBoundingBox; spreadMethod = GradientSpreadMethod.Pad
        gradientStops.add(GradientStop().apply { offset = 0.0; color = rgb(0xFFFF00u) })
        gradientStops.add(GradientStop().apply { offset = 1.0; color = rgb(0x0000FFu) })
    }
}
