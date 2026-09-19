package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import microsoft.ui.text.MarkerType
import windows.foundation.Uri

@GalleryPage(route = "RadioButton", title = "RadioButton", group = "BasicInput", order = 10)
internal fun radioButtonPage() = ExamplePage {
    val output = label("Select an option.")
    example("A group of RadioButtons.", radioButtonGroupSample(output), output = output)
    example("RadioButtons with string items.", radioButtonStringItemsSample())
}

@GallerySample(route = "RadioButton", title = "A group of RadioButtons.")
internal fun radioButtonGroupSample(output: TextBlock) = RadioButtons().apply {
    header = "Options:"
    (1..3).forEach { number -> items.add(RadioButton().apply {
        content = "Option $number"; named(this, "Option${number}RadioButton")
        click.add { _, _ -> output.text = "You selected Option $number" }
    }) }
}

@GallerySample(route = "RadioButton", title = "RadioButtons with string items.")
internal fun radioButtonStringItemsSample() = StackPanel().apply { this.spacing = 8.0; val preview = Border().apply { height = 50.0; borderThickness = inset(10.0); background = brush(0x008000u); borderBrush = brush(0xFFD700u) }
        children.add(RadioButtons().apply { this.header = "Background"; listOf("Green", "Yellow", "White").forEach { this.items.add(it) }; this.selectedIndex = 0 }.also { galleryRadioButtons -> galleryRadioButtons.selectionChanged.add { _, _ -> val gallerySelectedIndex = galleryRadioButtons.selectedIndex; if (gallerySelectedIndex in 0 until galleryRadioButtons.items.size) { val it = gallerySelectedIndex; preview.background = brush(listOf(0x008000u, 0xFFFF00u, 0xFFFFFFu)[it]) } } }.apply { maxColumns = 3 })
        children.add(RadioButtons().apply { this.header = "Border"; listOf("Green", "Yellow", "White").forEach { this.items.add(it) }; this.selectedIndex = 1 }.also { galleryRadioButtons -> galleryRadioButtons.selectionChanged.add { _, _ -> val gallerySelectedIndex = galleryRadioButtons.selectedIndex; if (gallerySelectedIndex in 0 until galleryRadioButtons.items.size) { val it = gallerySelectedIndex; preview.borderBrush = brush(listOf(0x006400u, 0xFFD700u, 0xFFFFFFu)[it]) } } }.apply { maxColumns = 3 })
        children.add(preview) }
