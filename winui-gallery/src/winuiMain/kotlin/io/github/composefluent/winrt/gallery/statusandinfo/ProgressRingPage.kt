package io.github.composefluent.winrt.gallery.statusandinfo

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.PlacementMode
import windows.foundation.Rect
import windows.foundation.Uri

@GalleryPage(route = "ProgressRing", title = "ProgressRing", group = "StatusAndInfo", order = 3)
internal fun progressRingPage() = ExamplePage {
    fun background(ring: ProgressRing) = select("Background color", listOf("Transparent", "LightGray")) {
        ring.background = if (it == 0) microsoft.ui.xaml.media.SolidColorBrush(windows.ui.Color(0u, 0u, 0u, 0u)) else brush(0xD3D3D3u)
    }
    val indefinite = progressRingIndeterminateProgressRingSample()
    example("An indeterminate ProgressRing.", indefinite, stack {
        children.add(ToggleSwitch().apply {
            isOn = true; offContent = "Do work"; onContent = "Working"
            toggled.add { _, _ -> indefinite.isActive = isOn }
        })
        children.add(background(indefinite))
    })
    val definite = progressRingDeterminateSample()

    example("A determinate ProgressRing.", stack(60.0, true) {
        children.add(definite)
        children.add(NumberBox().apply {
            header = "Progress"; minimum = 0.0; maximum = 100.0; value = 0.0; minWidth = 120.0
            spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
            valueChanged.add { _, _ -> if (value.isFinite()) definite.value = value else value = 0.0 }
        })
    }, background(definite))

}

@GallerySample(route = "ProgressRing", title = "A determinate ProgressRing.")
internal fun progressRingDeterminateSample() = ProgressRing().apply { width = 60.0; height = 60.0; isIndeterminate = false }

@GallerySample(route = "ProgressRing", title = "An indeterminate ProgressRing.")
internal fun progressRingIndeterminateProgressRingSample() = ProgressRing().apply { width = 60.0; height = 60.0; isActive = true }
