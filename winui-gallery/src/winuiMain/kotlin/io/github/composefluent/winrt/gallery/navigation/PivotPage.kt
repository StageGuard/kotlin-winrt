package io.github.composefluent.winrt.gallery.navigation

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.animation.SlideNavigationTransitionInfo
import microsoft.ui.xaml.media.animation.SlideNavigationTransitionEffect

@GalleryPage(route = "Pivot", title = "Pivot", group = "Navigation", order = 2)
internal fun pivotPage() = ExamplePage {
    example("A basic Pivot.", pivotBasicPivotSample())
}

@GallerySample(route = "Pivot", title = "A basic Pivot.")
internal fun pivotBasicPivotSample() = Pivot().apply {
        title = "EMAIL"; minHeight = 400.0
        listOf("All", "Unread", "Flagged", "Urgent").forEach { name -> items.add(PivotItem().apply {
            header = name; content = TextBlock().apply { this.text = "${name.lowercase()} emails go here."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
        }) }
    }
