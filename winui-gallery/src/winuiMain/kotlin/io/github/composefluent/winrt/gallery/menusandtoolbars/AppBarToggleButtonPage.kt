package io.github.composefluent.winrt.gallery.menusandtoolbars

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.input.KeyboardAccelerator
import microsoft.ui.xaml.media.*
import windows.foundation.Point
import windows.foundation.Uri
import windows.system.VirtualKey
import windows.system.VirtualKeyModifiers

@GalleryPage(route = "AppBarToggleButton", title = "AppBarToggleButton", group = "MenusAndToolbars", order = 2)
internal fun appBarToggleButtonPage() = ExamplePage {
    val samples: List<Pair<String, (TextBlock) -> AppBarToggleButton>> = listOf(
        "An AppBarToggleButton with SymbolIcon." to ::appBarToggleButtonSymbolIconSample,
        "An AppBarToggleButton with BitmapIcon." to ::appBarToggleButtonBitmapIconSample,
        "An AppBarToggleButton with FontIcon." to ::appBarToggleButtonFontIconSample,
        "An AppBarToggleButton with PathIcon." to ::appBarToggleButtonPathIconSample,
    )
    samples.forEach { (title, create) ->
        val output = label("")
        example(title, create(output), output = output)
    }
}

@GallerySample(route = "AppBarToggleButton", title = "An AppBarToggleButton with SymbolIcon.")
internal fun appBarToggleButtonSymbolIconSample(output: TextBlock) = AppBarToggleButton().apply {
    label = "SymbolIcon"
    icon = SymbolIcon(Symbol.Shuffle)
    click.add { _, _ -> output.text = "IsChecked = ${isChecked?.toString().orEmpty()}" }
}

@GallerySample(route = "AppBarToggleButton", title = "An AppBarToggleButton with BitmapIcon.")
internal fun appBarToggleButtonBitmapIconSample(output: TextBlock) = AppBarToggleButton().apply {
    label = "BitmapIcon"
    icon = BitmapIcon().apply { uriSource = Uri("ms-appx:///Assets/SampleMedia/Slices2.png") }
    click.add { _, _ -> output.text = "IsChecked = ${isChecked?.toString().orEmpty()}" }
}

@GallerySample(route = "AppBarToggleButton", title = "An AppBarToggleButton with FontIcon.")
internal fun appBarToggleButtonFontIconSample(output: TextBlock) = AppBarToggleButton().apply {
    label = "FontIcon"
    icon = FontIcon().apply { fontFamily = FontFamily("Candara"); glyph = "\u03A3" }
    click.add { _, _ -> output.text = "IsChecked = ${isChecked?.toString().orEmpty()}" }
}

@GallerySample(route = "AppBarToggleButton", title = "An AppBarToggleButton with PathIcon.")
internal fun appBarToggleButtonPathIconSample(output: TextBlock) = AppBarToggleButton().apply {
    label = "PathIcon"
    icon = samplePath()
    isThreeState = true
    click.add { _, _ -> output.text = "IsChecked = ${isChecked?.toString().orEmpty()}" }
}
