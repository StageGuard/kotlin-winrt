package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.FontFamily
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import windows.ui.Color

@GalleryPage(route = "ComboBox", title = "ComboBox", group = "BasicInput", order = 9)
internal fun comboBoxPage(): UIElement = ExamplePage {
    val colorOutput = Rectangle().apply {
        width = 100.0
        height = 30.0
        margin = Thickness(0.0, 8.0, 0.0, 0.0)
    }
    example("A ComboBox with items defined inline and its width set.", comboBoxColorsSample(colorOutput), output = colorOutput)

    val fontOutput = label("You can set the font used for this text.").apply {
        margin = Thickness(8.0, 8.0, 0.0, 0.0)
    }
    example("A ComboBox with its ItemsSource set.", comboBoxFontsSample(fontOutput), output = fontOutput)

    val sizeOutput = label("You can set the font size used for this text.").apply {
        margin = Thickness(8.0, 8.0, 0.0, 0.0)
        fontFamily = FontFamily("Segoe UI")
    }
    example("An editable ComboBox.", comboBoxFontSizeSample(sizeOutput), output = sizeOutput)
}

@GallerySample(route = "ComboBox", title = "A ComboBox with items defined inline and its width set.")
internal fun comboBoxColorsSample(colorOutput: Rectangle) = ComboBox().apply {
    width = 200.0
    header = "Colors"
    placeholderText = "Pick a color"
    listOf("Blue", "Green", "Red", "Yellow").forEach { items.add(it) }
    selectionChanged.add { _, _ ->
        val color = when (selectedIndex) {
            0 -> Color(255u, 0u, 0u, 255u)
            1 -> Color(255u, 0u, 128u, 0u)
            2 -> Color(255u, 255u, 0u, 0u)
            3 -> Color(255u, 255u, 255u, 0u)
            else -> null
        }
        color?.let { colorOutput.fill = SolidColorBrush(it) }
    }
}

@GallerySample(route = "ComboBox", title = "A ComboBox with its ItemsSource set.")
internal fun comboBoxFontsSample(fontOutput: TextBlock) = ComboBox().apply {
    minWidth = 200.0
    header = "Font"
    itemsSource = listOf("Arial", "Comic Sans MS", "Courier New", "Segoe UI", "Times New Roman")
    selectionChanged.add { _, _ ->
        selectedItem?.toString()?.let { fontOutput.fontFamily = FontFamily(it) }
    }
    selectedIndex = 2
}

@GallerySample(route = "ComboBox", title = "An editable ComboBox.")
internal fun comboBoxFontSizeSample(sizeOutput: TextBlock) = ComboBox().apply {
    val sizes = listOf(8.0, 9.0, 10.0, 11.0, 12.0, 14.0, 16.0, 18.0, 20.0, 24.0, 28.0, 36.0, 48.0, 72.0)
    width = 200.0
    header = "Font Size"
    isEditable = true
    itemsSource = sizes
    selectionChanged.add { _, _ ->
        (selectedItem as? Double)?.let { sizeOutput.fontSize = it }
    }
    selectedIndex = 2
    textSubmitted.add { _, args ->
        val submitted = text.toDoubleOrNull()
        if (submitted != null && (submitted in sizes || submitted > 8.0 && submitted < 100.0)) {
            selectedItem = submitted
        } else {
            text = selectedValue.toString()
            val ownerRoot = xamlRoot
            ContentDialog().apply {
                content = "The font size must be a number between 8 and 100."
                closeButtonText = "Close"
                defaultButton = ContentDialogButton.Close
                xamlRoot = ownerRoot
            }.showAsync()
        }
        args.handled = true
    }
}
