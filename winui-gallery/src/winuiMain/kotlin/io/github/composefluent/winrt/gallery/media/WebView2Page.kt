package io.github.composefluent.winrt.gallery.media

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.await
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.windows.storage.pickers.FileOpenPicker
import windows.foundation.Uri
import windows.media.core.MediaSource
import windows.storage.StorageFile

@GalleryPage(route = "WebView2", title = "WebView2", group = "Media", order = 7)
internal fun webView2Page() = ExamplePage {
    val web = webView2BasicSample()

    example("A simple WebView2.", Grid().apply {
        rowDefinitions.add(autoRow()); rowDefinitions.add(starRow())
        children.add(label("WebView2 is powered by the Chromium engine.").apply { margin = Thickness(0.0, 0.0, 0.0, 12.0) })
        Grid.setRow(web, 1); children.add(web)
    })
    unloaded.add { _, _ -> web.close() }

}

@GallerySample(route = "WebView2", title = "A simple WebView2.")
internal fun webView2BasicSample() = WebView2().apply {
    minWidth = 200.0; minHeight = 200.0
    horizontalAlignment = HorizontalAlignment.Stretch; verticalAlignment = VerticalAlignment.Stretch
    source = Uri("https://learn.microsoft.com/windows/apps/winui/winui3/")
}
