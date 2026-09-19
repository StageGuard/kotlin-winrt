package io.github.composefluent.winrt.gallery.styles

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.composition.systembackdrops.MicaKind
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.documents.*
import microsoft.ui.xaml.media.*
import microsoft.ui.xaml.shapes.*
import windows.foundation.Uri
import windows.foundation.numerics.Vector3

@GalleryPage(route = "Acrylic", title = "AcrylicBrush", group = "Styles", order = 0)
internal fun acrylicPage() = ExamplePage {
    children.add(RichTextBlock().apply {
        blocks.add(Paragraph().apply {
            inlines.add(Run().apply { text = "Acrylic Brush might fall back to SolidColorbrush in certain scenarios. If you can't see the Acrylic effect, please refer to " })
            inlines.add(Hyperlink().apply {
                navigateUri = Uri("https://learn.microsoft.com/windows/apps/design/style/acrylic#usability-and-adaptability")
                inlines.add(Run().apply { text = "Acrylic brush adaptability documentation" })
            })
            inlines.add(Run().apply { text = ". Acrylic Brush uses in-app acrylic. See " })
            inlines.add(Hyperlink().apply { inlines.add(Run().apply { text = "SystemBackdrops (Mica/Acrylic)" }); click.add { _, _ -> GalleryNavigationHost.navigate("SystemBackdrops") } })
            inlines.add(Run().apply { text = " for background acrylic." })
        })
    })
    fun opacity(title: String, changed: (Double) -> Unit) = range(title, 0.8, 0.0, 1.0, changed).apply { width = 200.0; smallChange = 0.001; stepFrequency = 0.001; isFocusEngagementEnabled = false }
    example("Default in-app Acrylic brush.", acrylicDefaultInAppAcrylicBrushSample())
    val customSample = acrylicCustomInAppBrushSample()
    val custom = checkNotNull(customSample.children[3].asWinRT<Rectangle>().fill).asWinRT<AcrylicBrush>()

    example("Custom in-app Acrylic brush.", customSample, stack {
        children.add(opacity("Tint Opacity :") { custom.tintOpacity = it })
        children.add(select("Tint Color :", listOf("Black", "Red", "Blue")) { custom.tintColor = rgb(listOf(0u, 0xFF0000u, 0x0000FFu)[it]) })
        children.add(select("Fallback Color :", listOf("Green", "Yellow")) { custom.fallbackColor = rgb(listOf(0x008000u, 0xFFFF00u)[it]) })
    })
    val luminositySample = acrylicLuminosityInAppBrushSample()
    val luminosity = checkNotNull(luminositySample.children[3].asWinRT<Rectangle>().fill).asWinRT<AcrylicBrush>()

    example("Luminosity in-app Acrylic.", luminositySample, stack {
        children.add(opacity("Tint Opacity :") { luminosity.tintOpacity = it })
        children.add(opacity("Tint Luminosity Opacity :") { luminosity.tintLuminosityOpacity = it })
    })


}

@GallerySample(route = "Acrylic", title = "Default in-app Acrylic brush.")
internal fun acrylicDefaultInAppAcrylicBrushSample() = Grid().apply {
    minWidth = 320.0; height = 252.0; maxWidth = 400.0
    children.add(Rectangle().apply { width = 100.0; height = 200.0; horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top; fill = brush(0x00FFFFu) })
    children.add(Ellipse().apply { width = 152.0; height = 152.0; horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center; fill = brush(0xFF00FFu) })
    children.add(Rectangle().apply { width = 80.0; height = 100.0; horizontalAlignment = HorizontalAlignment.Right; verticalAlignment = VerticalAlignment.Bottom; fill = brush(0xFFFF00u) })
    children.add(Rectangle().apply { margin = inset(12.0); fill = GalleryTheme.brush("AcrylicInAppFillColorDefaultBrush") })
}

@GallerySample(route = "Acrylic", title = "Custom in-app Acrylic brush.")
internal fun acrylicCustomInAppBrushSample() = Grid().apply {
    val material = AcrylicBrush().apply { fallbackColor = rgb(0x008000u); tintColor = rgb(0u); tintOpacity = 0.8 }
    minWidth = 320.0; height = 252.0; maxWidth = 400.0
    children.add(Rectangle().apply { width = 100.0; height = 200.0; horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top; fill = brush(0x00FFFFu) })
    children.add(Ellipse().apply { width = 152.0; height = 152.0; horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center; fill = brush(0xFF00FFu) })
    children.add(Rectangle().apply { width = 80.0; height = 100.0; horizontalAlignment = HorizontalAlignment.Right; verticalAlignment = VerticalAlignment.Bottom; fill = brush(0xFFFF00u) })
    children.add(Rectangle().apply { margin = inset(12.0); fill = material })
}

@GallerySample(route = "Acrylic", title = "Luminosity in-app Acrylic.")
internal fun acrylicLuminosityInAppBrushSample() = Grid().apply {
    val material = AcrylicBrush().apply { fallbackColor = rgb(0x87CEEBu); tintColor = rgb(0x87CEEBu); tintOpacity = 0.8; tintLuminosityOpacity = 0.8 }
    minWidth = 320.0; height = 252.0; maxWidth = 400.0
    children.add(Rectangle().apply { width = 100.0; height = 200.0; horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top; fill = brush(0x00FFFFu) })
    children.add(Ellipse().apply { width = 152.0; height = 152.0; horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center; fill = brush(0xFF00FFu) })
    children.add(Rectangle().apply { width = 80.0; height = 100.0; horizontalAlignment = HorizontalAlignment.Right; verticalAlignment = VerticalAlignment.Bottom; fill = brush(0xFFFF00u) })
    children.add(Rectangle().apply { margin = inset(12.0); fill = material })
}
