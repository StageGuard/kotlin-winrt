package io.github.composefluent.winrt.gallery.collections

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "FlipView", title = "FlipView", group = "Collections", order = 0)
internal fun flipViewPage() = ExamplePage {
    example("A simple FlipView with declared items.", flipViewDeclaredItemsSample())
    example("A FlipView showing bound data.", flipViewBoundDataSample())
    example("A vertical FlipView.", flipViewVerticalSample())
}

@GallerySample(route = "FlipView", title = "A simple FlipView with declared items.")
internal fun flipViewDeclaredItemsSample() = FlipView().apply {
    height = 270.0
    maxWidth = 400.0
    listOf("cliff", "grapes", "rainier", "sunset", "valley").forEach { name ->
        items.add(Image().apply { this.width = 400.0; this.height = 400.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/$name.jpg") ) }.apply {
            height = 270.0
            named(this, name)
        })
    }
}

@GallerySample(route = "FlipView", title = "A FlipView showing bound data.")
internal fun flipViewBoundDataSample() = FlipView().apply {
    height = 180.0
    maxWidth = 400.0
    borderThickness = inset(1.0)
    borderBrush = brush(0u)
    itemsSource = GalleryCatalog.groups.take(3).flatMap { it.pages }.map { page ->
        Grid().apply {
            rowDefinitions.add(starRow())
            rowDefinitions.add(autoRow())
            if (page.image.isNotBlank()) children.add(Image().apply { this.width = 36.0; this.height = 36.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(page.image) ) })
            children.add(Border().apply {
                Grid.setRow(this, 1)
                height = 60.0
                background = brush(0xFFFFFFu)
                opacity = 0.65
                child = TextBlock().apply { this.text = page.title; this.fontSize = 24.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply {
                    foreground = brush(0u)
                    horizontalAlignment = HorizontalAlignment.Center
                }
            })
        }
    }
}

@GallerySample(route = "FlipView", title = "A vertical FlipView.")
internal fun flipViewVerticalSample() = FlipView().apply {
    height = 270.0
    maxWidth = 400.0
    listOf("cliff", "grapes", "rainier", "sunset", "valley").forEach { name ->
        items.add(Image().apply { this.width = 400.0; this.height = 400.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/$name.jpg") ) }.apply {
            height = 270.0
            named(this, name)
        })
    }
    loaded.add { _, _ -> itemsPanelRoot?.asWinRT<VirtualizingStackPanel>()?.orientation = Orientation.Vertical }
}
