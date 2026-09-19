package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import microsoft.ui.text.MarkerType
import windows.foundation.Uri

@GalleryPage(route = "RatingControl", title = "RatingControl", group = "BasicInput", order = 11)
internal fun ratingControlPage() = ExamplePage {
    val output = label("-1")

    val rating = ratingControlSimpleSample(output)
    example("A simple RatingControl.", rating, stack {
        children.add(option("IsClearEnabled") { rating.isClearEnabled = it })
        children.add(label("Swipe left or click again to clear your rating."))
        children.add(option("IsReadOnly") { rating.isReadOnly = it })
    }, output)
    val placeholder = ratingControlPlaceholderSample()
    val placeholderSlider = Slider().apply {
        width = 220.0
        header = "PlaceholderValue"; minimum = 0.0; maximum = 5.0
        stepFrequency = 0.5; smallChange = 0.5
        valueChanged.add { _, _ -> placeholder.placeholderValue = value }
    }
    example("A RatingControl with a placeholder value.", placeholder, placeholderSlider)

}

@GallerySample(route = "RatingControl", title = "A simple RatingControl.")
internal fun ratingControlSimpleSample(output: TextBlock) = RatingControl().apply {
    caption = "312 ratings"; horizontalAlignment = HorizontalAlignment.Left; isClearEnabled = false
    valueChanged.add { _, _ -> caption = "Your rating"; output.text = value.toString() }
    named(this, "Simple RatingControl")
}

@GallerySample(route = "RatingControl", title = "A RatingControl with a placeholder value.")
internal fun ratingControlPlaceholderSample() = RatingControl().apply { named(this, "RatingControl with placeholder") }
