package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import microsoft.ui.text.MarkerType
import windows.foundation.Uri

@GalleryPage(route = "HyperlinkButton", title = "HyperlinkButton", group = "BasicInput", order = 2)
internal fun hyperlinkButtonPage() = ExamplePage {
    val link = hyperlinkButtonUriSample()
    example("A HyperlinkButton that navigates to a URI.", link,
        option("Disable hyperlink button") { link.isEnabled = !it })
    example("A HyperlinkButton that handles a Click event.", hyperlinkButtonClickSample())
}

@GallerySample(route = "HyperlinkButton", title = "A HyperlinkButton that handles a Click event.")
internal fun hyperlinkButtonClickSample() = HyperlinkButton().apply {
        content = "Go to ToggleButton"
        click.add { _, _ -> GalleryNavigationHost.navigate("ToggleButton") }
    }

@GallerySample(route = "HyperlinkButton", title = "A HyperlinkButton that navigates to a URI.")
internal fun hyperlinkButtonUriSample() = HyperlinkButton().apply {
        content = "Microsoft home page"
        navigateUri = Uri("https://www.microsoft.com")
    }
