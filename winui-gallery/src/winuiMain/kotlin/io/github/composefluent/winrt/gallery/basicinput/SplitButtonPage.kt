package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import microsoft.ui.text.MarkerType
import windows.foundation.Uri

@GalleryPage(route = "SplitButton", title = "SplitButton", group = "BasicInput", order = 5)
internal fun splitButtonPage() = ExamplePage {
    val editor = RichEditBox().apply { width = 240.0; minHeight = 96.0; placeholderText = "Type something here" }
    example("A SplitButton with a color picker.", splitButtonColorSample(editor), editor)
    example("A SplitButton with text.", splitButtonTextSample(editor))
}

@GallerySample(route = "SplitButton", title = "A SplitButton with a color picker.")
internal fun splitButtonColorSample(editor: RichEditBox) = SplitButton().apply {
    var color = rgb(0x008000u)
    val preview = Border().apply { width = 32.0; height = 32.0; background = SolidColorBrush(color); cornerRadius = corners(4.0) }
    val picker = Flyout()
    picker.content = VariableSizedWrapGrid().apply {
        maximumRowsOrColumns = 3; orientation = Orientation.Horizontal
        swatches.forEach { (name, value) -> children.add(Button().apply {
            padding = inset(0.0); minWidth = 0.0; minHeight = 0.0; margin = inset(6.0)
            content = Rectangle().apply { width = 32.0; height = 32.0; radiusX = 4.0; radiusY = 4.0; fill = brush(value) }
            named(this, name)
            click.add { _, _ ->
                color = rgb(value); preview.background = SolidColorBrush(color)
                editor.document!!.selection!!.characterFormat!!.foregroundColor = color
                picker.hide(); editor.focus(FocusState.Keyboard)
            }
        }) }
    }
    content = preview; padding = inset(0.0); minWidth = 0.0; minHeight = 0.0; flyout = picker
    named(this, "Font color")
    click.add { _, _ -> editor.document!!.selection!!.characterFormat!!.foregroundColor = color; editor.focus(FocusState.Keyboard) }
}

@GallerySample(route = "SplitButton", title = "A SplitButton with text.")
internal fun splitButtonTextSample(editor: RichEditBox) = SplitButton().apply {
    content = "Choose color"
    flyout = MenuFlyout().apply { swatches.forEach { (name, value) -> items.add(MenuFlyoutItem().apply {
            text = name; click.add { _, _ -> editor.document!!.selection!!.characterFormat!!.foregroundColor = rgb(value) }
    }) } }
}

// Ported from WinUI Gallery Samples/{DropDownButton,HyperlinkButton,RepeatButton,
// ToggleButton,SplitButton,ToggleSplitButton,RadioButton,RatingControl,ColorPicker} (MIT).


internal val swatches = listOf("Red" to 0xFF0000u, "Orange" to 0xFFA500u, "Yellow" to 0xFFFF00u,
    "Green" to 0x008000u, "Blue" to 0x0000FFu, "Indigo" to 0x4B0082u, "Violet" to 0xEE82EEu, "Gray" to 0x808080u)
