package io.github.composefluent.winrt.gallery.media

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.documents.Hyperlink
import microsoft.ui.xaml.documents.Run
import windows.devices.geolocation.BasicGeoposition
import windows.devices.geolocation.Geopoint
import windows.foundation.Uri
import windows.system.VirtualKey

@GalleryPage(route = "MapControl", title = "MapControl", group = "Media", order = 3)
internal fun mapControlPage() = ExamplePage {
    children.add(TextBlock().apply {
        margin = Thickness(0.0, 0.0, 0.0, 12.0); textWrapping = TextWrapping.Wrap
        inlines.add(Run().apply { text = "Follow instructions " })
        inlines.add(Hyperlink().apply {
            navigateUri = Uri("https://learn.microsoft.com/azure/azure-maps/how-to-manage-account-keys")
            inlines.add(Run().apply { text = "here" })
        })
        inlines.add(Run().apply { text = " to obtain your MapServiceToken." })
    })
    children.add(picture("ms-appx:///Assets/SampleMedia/MapExample.png", Double.NaN).apply { height = 320.0; horizontalAlignment = HorizontalAlignment.Left })
    example("A MapControl showing a pin on a map.", mapControlSample())
}

@GallerySample(route = "MapControl", title = "A MapControl showing a pin on a map.")
internal fun mapControlSample() = stack {
    val map = MapControl().apply { height = 400.0; horizontalAlignment = HorizontalAlignment.Stretch; named(this, "Map") }
    val token = PasswordBox().apply { minWidth = 200.0; placeholderText = "Map service token"; named(this, "Map service token") }
    fun configureMap() {
        if (token.password.isBlank()) return
        map.center = Geopoint(BasicGeoposition(0.0, 0.0, 0.0))
        map.zoomLevel = 1.0
        if (map.layers.isEmpty()) map.layers.add(MapElementsLayer().apply {
            mapElements = mutableListOf(MapIcon().apply {
                location = Geopoint(BasicGeoposition(-30.034647, -51.217659, 0.0))
            })
        })
    }
    fun applyToken() {
        token.password.takeIf { it.isNotBlank() }?.let {
            map.mapServiceToken = it
            configureMap()
        }
    }
    token.keyDown.add { _, args -> if (args.key == VirtualKey.Enter) applyToken() }
    children.add(StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; children.add(token); children.add(Button().apply { this.content = "Set token" }.also { galleryButton -> galleryButton.click.add { _, _ -> applyToken() } }) })
    children.add(map)
}
