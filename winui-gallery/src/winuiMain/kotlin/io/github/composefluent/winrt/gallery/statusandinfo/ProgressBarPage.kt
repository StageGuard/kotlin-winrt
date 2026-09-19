package io.github.composefluent.winrt.gallery.statusandinfo

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.PlacementMode
import windows.foundation.Rect
import windows.foundation.Uri

@GalleryPage(route = "ProgressBar", title = "ProgressBar", group = "StatusAndInfo", order = 2)
internal fun progressBarPage() = ExamplePage {
    val indefinite = progressBarIndeterminateProgressBarSample()
    example("An indeterminate ProgressBar.", indefinite, choices("Progress state", listOf("Running", "Paused", "Error")) {
        indefinite.showPaused = it == 1; indefinite.showError = it == 2
    })
    val progress = progressBarDeterminateSample()

    example("A determinate ProgressBar.", stack(16.0, true) {
        children.add(progress); children.add(label("Progress"))
        children.add(NumberBox().apply {
            value = 0.0; minimum = 0.0; maximum = 100.0; spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
            named(this, "NumberBox controlling ProgressBar2 value")
            valueChanged.add { _, _ -> if (value.isFinite()) progress.value = value else value = 0.0 }
        })
    })

}

@GallerySample(route = "ProgressBar", title = "A determinate ProgressBar.")
internal fun progressBarDeterminateSample() = ProgressBar().apply { width = 130.0 }

@GallerySample(route = "ProgressBar", title = "An indeterminate ProgressBar.")
internal fun progressBarIndeterminateProgressBarSample() = ProgressBar().apply { width = 130.0; margin = Thickness(10.0, 10.0, 0.0, 0.0); isIndeterminate = true }
