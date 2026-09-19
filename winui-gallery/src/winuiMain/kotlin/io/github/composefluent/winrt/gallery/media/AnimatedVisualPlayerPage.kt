package io.github.composefluent.winrt.gallery.media

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.await
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.ToggleButton

@GalleryPage(route = "AnimatedVisualPlayer", title = "AnimatedVisualPlayer", group = "Media", order = 0)
internal fun animatedVisualPlayerPage() = ExamplePage {
    val tasks = GalleryPageTasks(this)
    var player: AnimatedVisualPlayer? = null
    example("AnimatedVisualPlayer playback of a Lottie animation.", animatedVisualPlayerSample(tasks) { player = it })
    unloaded.add { _, _ -> player?.let { it.stop(); it.source = null } }
}

@GallerySample(route = "AnimatedVisualPlayer", title = "AnimatedVisualPlayer playback of a Lottie animation.")
internal fun animatedVisualPlayerSample(
    tasks: GalleryPageTasks,
    onPlayerCreated: (AnimatedVisualPlayer) -> Unit,
) = StackPanel().apply { this.spacing = 0.0; val player = AnimatedVisualPlayer().apply { autoPlay = false; source = GalleryLottieSource() }
    onPlayerCreated(player)
    val pause = ToggleButton().apply {
        content = SymbolIcon(Symbol.Pause); isThreeState = false; named(this, "Pause")
        ToolTipService.setToolTip(this, "Pause")
    }
    pause.checked.add { _, _ -> player.pause() }
    pause.unchecked.add { _, _ -> player.resume() }
    fun play(rate: Double) {
        player.playbackRate = rate
        if (pause.isChecked == true) pause.isChecked = false
        else if (!player.isPlaying) tasks.launch { player.playAsync(0.0, 1.0, false).await() }
    }
    horizontalAlignment = HorizontalAlignment.Center
    children.add(TextBlock().apply { this.text = "This AnimatedVisualPlayer consumes an animation created using Adobe AfterEffects and translated into Microsoft.UI.Composition objects using Lottie-Windows. The composition graph and vector paths are constructed with Kotlin projection classes."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    children.add(referenceLink("Lottie-Windows", "https://aka.ms/lottie"))
    children.add(Border().apply {
        width = 400.0; height = 400.0; margin = Thickness(0.0, 20.0, 0.0, 20.0)
        background = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush")
        borderBrush = GalleryTheme.brush("CardStrokeColorDefaultBrush"); borderThickness = inset(1.0); child = player
    })
    children.add(Grid().apply {
        width = 400.0; margin = inset(12.0); columnSpacing = 8.0
        repeat(4) { columnDefinitions.add(column(1.0, GridUnitType.Star)) }
        val controls = listOf(
            Button().apply { this.content = "Play" }.also { galleryButton -> galleryButton.click.add { _, _ -> play(1.0) } }.apply { content = SymbolIcon(Symbol.Play) },
            pause,
            Button().apply { this.content = "Stop" }.also { galleryButton -> galleryButton.click.add { _, _ -> player.stop(); pause.isChecked = false } }.apply { content = SymbolIcon(Symbol.Stop) },
            Button().apply { this.content = "Reverse" }.also { galleryButton -> galleryButton.click.add { _, _ -> play(-1.0) } }.apply { content = SymbolIcon(Symbol.Previous) })
        controls.forEachIndexed { index, control ->
            Grid.setColumn(control, index); control.horizontalAlignment = HorizontalAlignment.Stretch
            named(control, listOf("Play", "Pause", "Stop", "Reverse")[index])
            ToolTipService.setToolTip(control, listOf("Play", "Pause", "Stop", "Reverse")[index])
            children.add(control)
        }
    }) }
