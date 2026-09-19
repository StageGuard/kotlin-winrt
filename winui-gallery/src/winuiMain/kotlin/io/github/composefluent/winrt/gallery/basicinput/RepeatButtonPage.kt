package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import microsoft.ui.text.MarkerType
import windows.foundation.Uri

@GalleryPage(route = "RepeatButton", title = "RepeatButton", group = "BasicInput", order = 3)
internal fun repeatButtonPage() = ExamplePage {
    val output = label("")
    val repeat = repeatButtonSample(output)
    example("A simple RepeatButton.", repeat, option("Disable RepeatButton") { repeat.isEnabled = !it }, output)
}

@GallerySample(route = "RepeatButton", title = "A simple RepeatButton.")
internal fun repeatButtonSample(output: TextBlock) = RepeatButton().apply {
    var clicks = 0
    content = "Click and hold"
    click.add { _, _ -> output.text = "Number of clicks: ${++clicks}" }
}
