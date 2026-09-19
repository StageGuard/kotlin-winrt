package io.github.composefluent.winrt.gallery.media

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import windows.ui.text.FontStyle

@GalleryPage(route = "Sound", title = "Sound", group = "Media", order = 6)
internal fun soundPage() = ExamplePage {
    val sound = ToggleSwitch().apply {
        offContent = "Sound Off"; onContent = "Sound On"
        isOn = ElementSoundPlayer.state == ElementSoundPlayerState.On
    }
    val spatial = option("Enable Spatial Audio", sound.isOn && ElementSoundPlayer.spatialAudioMode == ElementSpatialAudioMode.On) {
        if (sound.isOn) soundSpatialAudioSample(it)
    }.apply { isEnabled = sound.isOn }
    sound.toggled.add { _, _ ->
        spatial.isEnabled = sound.isOn
        soundTogglingSoundSample(sound.isOn)
        if (!sound.isOn) { spatial.isChecked = false; soundSpatialAudioSample(false) }
    }
    example("Toggling sound.", sound)
    example("Toggling spatial audio.", stack(0.0) {
        children.add(spatial)
        children.add(label("Can only enable spatial audio when sound is on!", 12.0).apply {
            margin = Thickness(0.0, 5.0, 0.0, 0.0)
            fontStyle = FontStyle.Italic
            foreground = GalleryTheme.brush("TextFillColorSecondaryBrush")
        })
    })
    example("Play a specific system sound.", stack(5.0) {
        listOf(
            "Focus" to ElementSoundKind.Focus,
            "Invoke" to ElementSoundKind.Invoke,
            "Show" to ElementSoundKind.Show,
            "Hide" to ElementSoundKind.Hide,
            "MovePrevious" to ElementSoundKind.MovePrevious,
            "MoveNext" to ElementSoundKind.MoveNext,
            "GoBack" to ElementSoundKind.GoBack,
        ).forEach { (title, kind) ->
            children.add(Button("▶ $title") { soundSpecificSystemSoundsSample(kind) }.apply {
                elementSoundMode = ElementSoundMode.Off
                named(this, title)
            })
        }
    })
}

@GallerySample(route = "Sound", title = "Toggling sound.")
internal fun soundTogglingSoundSample(enabled: Boolean) = run {
    ElementSoundPlayer.state = if (enabled) ElementSoundPlayerState.On else ElementSoundPlayerState.Off
}

@GallerySample(route = "Sound", title = "Toggling spatial audio.")
internal fun soundSpatialAudioSample(enabled: Boolean) = run {
    ElementSoundPlayer.spatialAudioMode = if (enabled) ElementSpatialAudioMode.On else ElementSpatialAudioMode.Off
}

@GallerySample(route = "Sound", title = "Play a specific system sound.")
internal fun soundSpecificSystemSoundsSample(kind: ElementSoundKind) = run {
    ElementSoundPlayer.play(kind)
}
