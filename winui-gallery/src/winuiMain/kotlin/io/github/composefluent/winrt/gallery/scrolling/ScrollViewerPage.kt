package io.github.composefluent.winrt.gallery.scrolling

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.Stretch

@GalleryPage(route = "ScrollViewer", title = "ScrollViewer", group = "Scrolling", order = 3)
internal fun scrollViewerPage() = ExamplePage {
    val viewer = scrollViewerContentInsideAScrollViewerSample()
    val zoom = range("Zoom", 4.0, viewer.minZoomFactor.toDouble(), viewer.maxZoomFactor.toDouble()) {
        if (viewer.zoomFactor != it.toFloat()) viewer.changeView(null, null, it.toFloat())
    }
    viewer.viewChanged.add { _, args -> if (!args.isIntermediate) zoom.value = viewer.zoomFactor.toDouble() }
    val modes = listOf(ScrollMode.Disabled, ScrollMode.Enabled, ScrollMode.Auto)
    val bars = listOf(ScrollBarVisibility.Disabled, ScrollBarVisibility.Auto, ScrollBarVisibility.Hidden, ScrollBarVisibility.Visible)
    example("Content inside a ScrollViewer.", viewer, stack {
        children.add(select("ZoomMode", listOf("Disabled", "Enabled"), 1) {
            viewer.zoomMode = if (it == 0) ZoomMode.Disabled else ZoomMode.Enabled
            zoom.isEnabled = it == 1
            if (it == 0) viewer.changeView(null, null, 2.0f)
        })
        children.add(zoom)
        children.add(label("ScrollMode"))
        children.add(select("Horizontal", listOf("Disabled", "Enabled", "Auto"), 1) { viewer.horizontalScrollMode = modes[it] })
        children.add(select("Vertical", listOf("Disabled", "Enabled", "Auto"), 1) { viewer.verticalScrollMode = modes[it] })
        children.add(label("ScrollbarVisibility"))
        children.add(select("Horizontal", listOf("Disabled", "Auto", "Hidden", "Visible"), 1) { viewer.horizontalScrollBarVisibility = bars[it] })
        children.add(select("Vertical", listOf("Disabled", "Auto", "Hidden", "Visible"), 1) { viewer.verticalScrollBarVisibility = bars[it] })
    })
}

@GallerySample(route = "ScrollViewer", title = "Content inside a ScrollViewer.")
internal fun scrollViewerContentInsideAScrollViewerSample() = ScrollViewer().apply {
        width = 400.0; height = 266.0
        horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top
        isTabStop = true; isVerticalScrollChainingEnabled = true; zoomMode = ZoomMode.Enabled
        horizontalScrollMode = ScrollMode.Enabled; verticalScrollMode = ScrollMode.Enabled
        horizontalScrollBarVisibility = ScrollBarVisibility.Auto; verticalScrollBarVisibility = ScrollBarVisibility.Auto
        content = Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/cliff.jpg") ) }.apply {
            horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top; stretch = Stretch.None; named(this, "cliff")
        }
        loaded.add { _, _ -> changeView(null, null, 4.0f) }
    }
