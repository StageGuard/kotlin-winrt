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

@GalleryPage(route = "ThemeShadow", title = "ThemeShadow", group = "Styles", order = 9)
internal fun themeShadowPage() = ExamplePage {
    val sample = themeShadowBorderSample()
    val caster = sample.children[1].asWinRT<Border>()

    example("A ThemeShadow applied to a Border.", sample,
        range("Z-translation", 32.0, 0.0, 64.0) { caster.translation = Vector3(0f, 0f, it.toFloat()) }.apply { stepFrequency = 1.0; smallChange = 1.0; named(this, "shadow intensity") })

}

@GallerySample(route = "ThemeShadow", title = "A ThemeShadow applied to a Border.")
internal fun themeShadowBorderSample() = Grid().apply {
    padding = inset(36.0)
    val receiver = Grid()
    val shadow = ThemeShadow().apply { receivers.add(receiver) }
    val caster = Border().apply {
        width = 200.0; height = 200.0
        background = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush")
        cornerRadius = corners(8.0)
        this.shadow = shadow
        translation = Vector3(0f, 0f, 32f)
    }
    children.add(receiver)
    children.add(caster)
}
