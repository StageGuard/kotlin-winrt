package io.github.composefluent.winrt.gallery.collections

import io.github.composefluent.winrt.gallery.*
import kotlinx.coroutines.delay
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.imaging.BitmapImage
import windows.foundation.Uri

@GalleryPage(route = "PullToRefresh", title = "PullToRefresh", group = "Collections", order = 6)
internal fun pullToRefreshPage() = ExamplePage {
    val tasks = GalleryPageTasks(this)
    example("Basic pull to refresh.", pullToRefreshBasicSample(tasks))
    example("Pull to refresh with a custom icon.", pullToRefreshCustomIconSample(tasks))
}

@GallerySample(route = "PullToRefresh", title = "Basic pull to refresh.")
internal fun pullToRefreshBasicSample(tasks: GalleryPageTasks) = run {
    var count = 0
    val list = ListView().apply {
        height = 200.0; minWidth = 200.0; borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("TextControlBorderBrush")
        "AcrylicBrush ColorPicker NavigationView ParallaxView PersonPicture PullToRefreshPage RatingsControl RevealBrush TreeView".split(' ').forEach { items.add(it) }
    }
    RefreshContainer().apply {
        content = list
        refreshRequested.add { _, args ->
            val deferral = args.getDeferral()
            tasks.launch {
                try {
                    delay(500L)
                    list.items.add(0, "NewControl ${count++}")
                } finally {
                    deferral.complete(); deferral.close()
                }
            }
        }
    }
}

@GallerySample(route = "PullToRefresh", title = "Pull to refresh with a custom icon.")
internal fun pullToRefreshCustomIconSample(tasks: GalleryPageTasks) = run {
    var count = 0
    val list = ListView().apply {
        height = 200.0; minWidth = 200.0; borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("TextControlBorderBrush")
        "Mike Ben Barbra Claire Justin Shawn Drew Lili".split(' ').forEach { items.add(it) }
    }
    RefreshContainer().apply {
        content = list
        visualizer = RefreshVisualizer().apply {
            val sun = Image().apply { width = 35.0; height = 35.0 }
            fun updateSun() {
                sun.source = BitmapImage(Uri("ms-appx:///Assets/SampleMedia/Sun${if (sun.actualTheme == ElementTheme.Light) "Black" else "White"}.png"))
            }
            content = sun
            sun.loaded.add { _, _ -> updateSun() }; sun.actualThemeChanged.add { _, _ -> updateSun() }
            refreshStateChanged.add { _, _ -> microsoft.ui.xaml.hosting.ElementCompositionPreview.getElementVisual(sun).stopAnimation("RotationAngle") }
        }
        refreshRequested.add { _, args ->
            val deferral = args.getDeferral()
            tasks.launch {
                try {
                    delay(800L)
                    list.items.add(0, "New Friend ${count++}")
                } finally {
                    deferral.complete(); deferral.close()
                }
            }
        }
    }
}
