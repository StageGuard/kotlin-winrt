package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import microsoft.ui.text.MarkerType
import windows.foundation.Uri

@GalleryPage(route = "ToggleButton", title = "ToggleButton", group = "BasicInput", order = 4)
internal fun toggleButtonPage() = ExamplePage {
    val output = label("Off")
    val toggle = toggleButtonSimpleSample(output)
    example("A simple ToggleButton.", toggle, option("Disable ToggleButton") { toggle.isEnabled = !it }, output)
}

@GallerySample(route = "ToggleButton", title = "A simple ToggleButton.")
internal fun toggleButtonSimpleSample(output: TextBlock) = ToggleButton().apply {
    content = "ToggleButton"
    click.add { _, _ -> output.text = if (isChecked == true) "On" else "Off" }
}
