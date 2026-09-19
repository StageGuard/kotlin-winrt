package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.UIElement
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "ToggleSwitch", title = "ToggleSwitch", group = "BasicInput", order = 13)
internal fun toggleSwitchPage() = ExamplePage {
    example("A simple ToggleSwitch.", toggleSwitchSimpleSample())
    example("A ToggleSwitch with custom content.", toggleSwitchCustomContentSample())
}

@GallerySample(route = "ToggleSwitch", title = "A simple ToggleSwitch.")
internal fun toggleSwitchSimpleSample() = ToggleSwitch()

@GallerySample(route = "ToggleSwitch", title = "A ToggleSwitch with custom content.")
internal fun toggleSwitchCustomContentSample() = StackPanel().apply { this.spacing = 12.0; this.orientation = Orientation.Horizontal; val progress = ProgressRing().apply { width = 32.0; isActive = true }
        val toggle = ToggleSwitch().apply {
            header = "Toggle work"; isOn = true; offContent = "Do work"; onContent = "Working"
            toggled.add { _, _ -> progress.isActive = isOn }
        }
        children.add(toggle); children.add(progress) }
