package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.UIElement
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "Slider", title = "Slider", group = "BasicInput", order = 12)
internal fun sliderPage() = ExamplePage {
    addSliderExample("A simple Slider.", sliderSimpleSample())
    val ranged = sliderRangeSample()
    val options = stack(8.0) {
        fun field(title: String, initial: Double, changed: (Double) -> Unit) {
            children.add(NumberBox().apply { header = title; value = initial; spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Compact
                valueChanged.add { _, _ -> if (value.isFinite()) changed(value) }
            })
        }
        field("Minimum:", 500.0) { ranged.minimum = it }
        field("Maximum:", 1000.0) { ranged.maximum = it }
        field("StepFrequency:", 10.0) { if (it > 0) ranged.stepFrequency = it }
        field("SmallChange:", 10.0) { if (it > 0) ranged.smallChange = it }
    }
    addSliderExample("A Slider with range and step values.", ranged, options)
    addSliderExample("A Slider with tick marks.", sliderTicksSample())
    addSliderExample("A vertical Slider.", sliderVerticalSample())
}

private fun ExamplePage.addSliderExample(title: String, sample: Slider, options: UIElement? = null) {
    val output = label(sample.value.toInt().toString())
    sample.valueChanged.add { _, _ -> output.text = sample.value.toInt().toString() }
    example(title, sample, options, output)
}

@GallerySample(route = "Slider", title = "A simple Slider.")
internal fun sliderSimpleSample() = Slider().apply { width = 200.0 }

@GallerySample(route = "Slider", title = "A Slider with range and step values.")
internal fun sliderRangeSample() = Slider().apply {
    width = 200.0; header = "Control header"; maximum = 1000.0; minimum = 500.0; value = 800.0; stepFrequency = 10.0; smallChange = 10.0
}

@GallerySample(route = "Slider", title = "A Slider with tick marks.")
internal fun sliderTicksSample() = Slider().apply {
    width = 290.0; tickFrequency = 20.0; tickPlacement = microsoft.ui.xaml.controls.primitives.TickPlacement.Outside
}

@GallerySample(route = "Slider", title = "A vertical Slider.")
internal fun sliderVerticalSample() = Slider().apply {
    width = 100.0; height = 100.0; minimum = -50.0; maximum = 50.0; orientation = Orientation.Vertical
    tickFrequency = 10.0; tickPlacement = microsoft.ui.xaml.controls.primitives.TickPlacement.Outside
}
