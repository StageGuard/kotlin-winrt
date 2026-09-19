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

@GalleryPage(route = "AppBarButton", title = "AppBarButton", group = "MenusAndToolbars", order = 0)
internal fun appBarButtonPage() = ExamplePage {
    val samples: List<Pair<String, (TextBlock) -> AppBarButton>> = listOf(
        "An AppBarButton with SymbolIcon." to ::appBarButtonSymbolIconSample,
        "An AppBarButton with BitmapIcon." to ::appBarButtonBitmapIconSample,
        "An AppBarButton with FontIcon." to ::appBarButtonFontIconSample,
        "An AppBarButton with PathIcon." to ::appBarButtonPathIconSample,
        "An AppBarButton with Save." to ::appBarButtonSaveSample,
    )
    samples.forEach { (title, create) ->
        val output = label("")
        example(title, create(output), output = output)
    }
    example("An AppBarButton that opens a Flyout containing a TextBox.", appBarButtonAppBarButtonThatOpensAFlyoutContainingATextBoxSample1())
}

@GallerySample(route = "AppBarButton", title = "An AppBarButton that opens a Flyout containing a TextBox.")
internal fun appBarButtonAppBarButtonThatOpensAFlyoutContainingATextBoxSample1() = AppBarButton().apply {
    label = "Edit"
    icon = SymbolIcon(Symbol.Edit)
    allowFocusOnInteraction = true
    flyout = Flyout().apply { content = TextBox().apply { minWidth = 240.0; placeholderText = "Input text here" } }
}

@GallerySample(route = "AppBarButton", title = "An AppBarButton with SymbolIcon.")
internal fun appBarButtonSymbolIconSample(output: TextBlock) = AppBarButton().apply {
    label = "SymbolIcon"
    icon = SymbolIcon(Symbol.Like)
    click.add { _, _ -> output.text = "You clicked: Button1" }
}

@GallerySample(route = "AppBarButton", title = "An AppBarButton with BitmapIcon.")
internal fun appBarButtonBitmapIconSample(output: TextBlock) = AppBarButton().apply {
    label = "BitmapIcon"
    icon = BitmapIcon().apply { uriSource = Uri("ms-appx:///Assets/SampleMedia/Slices2.png") }
    click.add { _, _ -> output.text = "You clicked: Button2" }
}

@GallerySample(route = "AppBarButton", title = "An AppBarButton with FontIcon.")
internal fun appBarButtonFontIconSample(output: TextBlock) = AppBarButton().apply {
    label = "FontIcon"
    icon = FontIcon().apply { fontFamily = FontFamily("Candara"); glyph = "\u03A3" }
    click.add { _, _ -> output.text = "You clicked: Button3" }
}

@GallerySample(route = "AppBarButton", title = "An AppBarButton with PathIcon.")
internal fun appBarButtonPathIconSample(output: TextBlock) = AppBarButton().apply {
    label = "PathIcon"
    icon = samplePath()
    click.add { _, _ -> output.text = "You clicked: Button4" }
}

@GallerySample(route = "AppBarButton", title = "An AppBarButton with Save.")
internal fun appBarButtonSaveSample(output: TextBlock) = AppBarButton().apply {
    label = "Save"
    icon = SymbolIcon(Symbol.Save)
    keyboardAccelerators.add(shortcut(VirtualKey.S))
    click.add { _, _ -> output.text = "You clicked: Button5" }
}
