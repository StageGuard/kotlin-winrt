package io.github.composefluent.winrt.gallery.motion

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import windows.ui.Color

@GalleryPage(route = "ParallaxView", title = "ParallaxView", group = "Motion", order = 6)
internal fun parallaxViewPage() = ExamplePage {
    example("Parallax with a ListView.", parallaxListViewSample())
    example("Parallax with a ScrollView.", parallaxScrollViewSample())
}

@GallerySample(route = "ParallaxView", title = "Parallax with a ListView.")
internal fun parallaxListViewSample() = run {
    val list = ListView().apply {
        horizontalAlignment = HorizontalAlignment.Stretch; verticalAlignment = VerticalAlignment.Top; height = 750.0
        background = SolidColorBrush(Color(128u, 0u, 0u, 0u)); named(this, "all samples")
        GalleryCatalog.pages.sortedBy { it.title }.forEach { page -> items.add(TextBlock().apply { this.text = page.title; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { foreground = GalleryTheme.brush("SystemControlForegroundAltHighBrush") }) }
        header = TextBlock().apply { this.text = "Scroll the list to see parallaxing of image"; this.fontSize = 28.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply {
            maxWidth = 280.0; horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center; foreground = brush(0xFFFFFFu)
        }
    }
    Grid().apply {
        height = 750.0
        children.add(ParallaxView().apply {
            horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top
            source = list; verticalShift = 500.0; child = Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/cliff.jpg") ) }
        })
        children.add(list)
    }
}

@GallerySample(route = "ParallaxView", title = "Parallax with a ScrollView.")
internal fun parallaxScrollViewSample() = run {
    val view = ScrollView().apply {
        width = 150.0; height = 750.0; horizontalAlignment = HorizontalAlignment.Left
        content = StackPanel().apply { this.spacing = 0.0; listOf(0xF0F8FFu, 0xFAEBD7u, 0x00FFFFu, 0x7FFFD4u, 0xF0FFFFu, 0xF5F5DCu, 0xFFE4C4u, 0xFFEBCDu, 0x8A2BE2u, 0xA52A2Au, 0xDEB887u, 0x5F9EA0u, 0x7FFF00u, 0xD2691Eu, 0xFF7F50u, 0x6495EDu, 0xFFF8DCu, 0xDC143Cu, 0x00FFFFu).forEach { color ->
                children.add(Rectangle().apply { height = 150.0; fill = brush(color) })
            } }
    }
    Grid().apply {
        height = 750.0
        children.add(ParallaxView().apply {
            horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top
            source = view; verticalShift = 500.0; child = Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/cliff.jpg") ) }
        })
        children.add(TextBlock().apply { this.text = "Scroll the rectangles to see parallaxing of image"; this.fontSize = 28.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { maxWidth = 280.0; horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Top; foreground = brush(0xFFFFFFu) })
        children.add(view)
    }
}
