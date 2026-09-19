package io.github.composefluent.winrt.gallery.multiplewindows

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.Page
import microsoft.ui.xaml.controls.TextBlock
import microsoft.ui.xaml.media.MicaBackdrop
import windows.graphics.SizeInt32

@GalleryPage(route = "CreateMultipleWindows", title = "Multiple windows", group = "MultipleWindows", order = 2, glyph = "\uE8A7")
internal fun createMultipleWindowsPage() = ExamplePage {
    val parentTheme = actualTheme
    example("Create single threaded Multiple Top level Windows(MTW).", Button("Create new Window") {
        val childWindow = createMultipleWindowsSample()
        checkNotNull(childWindow.content).asWinRT<Page>().requestedTheme = parentTheme
        GalleryWindows.track(childWindow)
        childWindow.activate()
    })
}

@GallerySample(route = "CreateMultipleWindows", title = "Create single threaded Multiple Top level Windows(MTW).")
internal fun createMultipleWindowsSample() = Window().apply {
    content = Page().apply {
        content = TextBlock().apply {
            text = "New child window!"
            horizontalAlignment = HorizontalAlignment.Center
            verticalAlignment = VerticalAlignment.Center
        }
    }
    extendsContentIntoTitleBar = true
    systemBackdrop = MicaBackdrop()
    checkNotNull(appWindow).resizeClient(SizeInt32(500, 500))
}
