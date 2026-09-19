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

@GalleryPage(route = "SystemBackdropElement", title = "SystemBackdropElement", group = "Styles", order = 8)
internal fun systemBackdropElementPage() = ExamplePage {
    val sample = systemBackdropElementSample()
    val host = sample.children[0].asWinRT<SystemBackdropElement>()

    example("SystemBackdropElement sample.", sample, stack {
        children.add(select("Backdrop Type", listOf("Acrylic", "Mica", "Mica Alt")) {
            host.systemBackdrop = when (it) {
                0 -> DesktopAcrylicBackdrop()
                else -> MicaBackdrop().apply { kind = if (it == 1) MicaKind.Base else MicaKind.BaseAlt }
            }
        })
        children.add(range("Corner radius", 8.0, 0.0, 50.0) { host.cornerRadius = corners(it) }.apply { stepFrequency = 1.0 })
    })
    children.add(label("Theme updates for SystemBackdropElement are triggered by app or OS theme changes, not by setting theme directly on the element or a parent control.").apply { margin = Thickness(0.0, 12.0, 0.0, 0.0) })

}

@GallerySample(route = "SystemBackdropElement", title = "SystemBackdropElement sample.")
internal fun systemBackdropElementSample() = Grid().apply {
    width = 300.0; height = 200.0; horizontalAlignment = HorizontalAlignment.Center
    children.add(SystemBackdropElement().apply {
        cornerRadius = corners(8.0)
        systemBackdrop = DesktopAcrylicBackdrop()
    })
    children.add(Button().apply {
        content = "Click Me"
        horizontalAlignment = HorizontalAlignment.Center
        verticalAlignment = VerticalAlignment.Center
    })
}
