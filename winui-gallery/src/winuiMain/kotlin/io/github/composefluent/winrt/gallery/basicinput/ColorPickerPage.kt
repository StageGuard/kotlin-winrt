package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import microsoft.ui.text.MarkerType
import windows.foundation.Uri

@GalleryPage(route = "ColorPicker", title = "ColorPicker", group = "BasicInput", order = 8)
internal fun colorPickerPage() = ExamplePage {
    val initialColor = rgb(0x0078D4u)
    val preview = Rectangle().apply { height = 100.0; fill = SolidColorBrush(initialColor) }

    val picker = colorPickerSample(preview)
    val alphaSlider = option("IsAlphaSliderVisible", true) { picker.isAlphaSliderVisible = it }.apply { isEnabled = false }
    val alphaText = option("IsAlphaTextInputVisible", true) { picker.isAlphaTextInputVisible = it }.apply { isEnabled = false }
    example("A ColorPicker with customizable properties.", picker, stack(4.0) {
        width = 250.0
        children.add(option("IsMoreButtonVisible") { picker.isMoreButtonVisible = it })
        children.add(option("IsColorSliderVisible", true) { picker.isColorSliderVisible = it })
        children.add(option("IsColorChannelTextInputVisible", true) { picker.isColorChannelTextInputVisible = it })
        children.add(option("IsHexInputVisible", true) { picker.isHexInputVisible = it })
        children.add(option("Alpha Enabled") { picker.isAlphaEnabled = it; alphaSlider.isEnabled = it; alphaText.isEnabled = it })
        children.add(alphaSlider); children.add(alphaText)
        children.add(choices("Colorspectrum shape", listOf("Box", "Ring")) { picker.colorSpectrumShape = if (it == 0) ColorSpectrumShape.Box else ColorSpectrumShape.Ring })
        children.add(label("ColorPicker applied on a Rectangle")); children.add(preview)
    })

}

@GallerySample(route = "ColorPicker", title = "A ColorPicker with customizable properties.")
internal fun colorPickerSample(preview: Rectangle) = ColorPicker().apply {
    isMoreButtonVisible = false
    isAlphaEnabled = false
    preview.fill = SolidColorBrush(color)
    colorChanged.add { _, _ -> preview.fill = SolidColorBrush(this.color) }
}
