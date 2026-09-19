package io.github.composefluent.winrt.gallery.scrolling

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.Stretch

@GalleryPage(route = "PipsPager", title = "PipsPager", group = "Scrolling", order = 1)
internal fun pipsPagerPage() = ExamplePage {
    example("A PipsPager integrated with a FlipView.", pipsPagerFlipViewSample())
    val configurable = pipsPagerPipsPagerWithOptionsToChangeItsOrientationSample1()
    val visibilities = listOf(PipsPagerButtonVisibility.Visible, PipsPagerButtonVisibility.VisibleOnPointerOver, PipsPagerButtonVisibility.Collapsed)
    example("A PipsPager with options to change its orientation.", configurable, stack {
        children.add(select("Orientation", listOf("Horizontal", "Vertical")) { configurable.orientation = if (it == 0) Orientation.Horizontal else Orientation.Vertical })
        children.add(select("Previous Button Visibility", listOf("Visible", "VisibleOnPointerOver", "Collapsed")) { configurable.previousButtonVisibility = visibilities[it] })
        children.add(select("Next Button Visibility", listOf("Visible", "VisibleOnPointerOver", "Collapsed")) { configurable.nextButtonVisibility = visibilities[it] })
        children.add(select("Wrap Mode", listOf("None", "Wrap")) { configurable.wrapMode = if (it == 0) PipsPagerWrapMode.None else PipsPagerWrapMode.Wrap })
    })

}

@GallerySample(route = "PipsPager", title = "A PipsPager integrated with a FlipView.")
internal fun pipsPagerFlipViewSample() = stack {
    val gallery = FlipView().apply {
        height = 270.0; maxWidth = 400.0
        repeat(8) { items.add(Image().apply { this.width = 400.0; this.height = 400.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(it)) ) }.apply { height = 270.0; stretch = Stretch.Uniform }) }
    }
    val pager = PipsPager().apply { numberOfPages = 8; horizontalAlignment = HorizontalAlignment.Center }
    gallery.selectionChanged.add { _, _ ->
        if (pager.selectedPageIndex != gallery.selectedIndex) pager.selectedPageIndex = gallery.selectedIndex
    }
    pager.selectedIndexChanged.add { _, _ ->
        if (gallery.selectedIndex != pager.selectedPageIndex) gallery.selectedIndex = pager.selectedPageIndex
    }
    children.add(gallery)
    children.add(pager)
}

@GallerySample(route = "PipsPager", title = "A PipsPager with options to change its orientation.")
internal fun pipsPagerPipsPagerWithOptionsToChangeItsOrientationSample1() = PipsPager().apply {
        numberOfPages = 10
        previousButtonVisibility = PipsPagerButtonVisibility.Visible
        nextButtonVisibility = PipsPagerButtonVisibility.Visible
    }
