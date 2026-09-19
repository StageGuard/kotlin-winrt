package io.github.composefluent.winrt.gallery.motion

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.animation.*

@GalleryPage(route = "PageTransition", title = "Page Transitions", group = "Motion", order = 4)
internal fun pageTransitionPage() = ExamplePage {
    val sample = pageTransitionSample()
    example("Page transitions.", sample.first, sample.second)
}

@GallerySample(route = "PageTransition", title = "Page transitions.")
internal fun pageTransitionSample() = run {
    var transition: NavigationTransitionInfo? = null
    val frame = Frame().apply {
        minHeight = 600.0; horizontalAlignment = HorizontalAlignment.Stretch
        contentTransitions = TransitionCollection().apply { add(NavigationThemeTransition()) }
        // Native Page is activated by WinUI; its content is constructed in Kotlin.
        navigated.add { _, args -> checkNotNull(args.content).asWinRT<Page>().content = sampleContent(args.parameter.toString().toInt()) }
    }
    frame.navigate(Page::class, 1)
    val options = StackPanel().apply { this.spacing = 0.0; children.add(RadioButtons().apply { this.header = "Transition modes"; listOf("Default", "Entrance", "DrillIn", "Suppress", "Slide from Right", "Slide from Left", "Common", "Continuum").forEach { this.items.add(it) }; this.selectedIndex = 0 }.also { galleryRadioButtons -> galleryRadioButtons.selectionChanged.add { _, _ -> val gallerySelectedIndex = galleryRadioButtons.selectedIndex; if (gallerySelectedIndex in 0 until galleryRadioButtons.items.size) { val it = gallerySelectedIndex; transition = when (it) {
                0 -> null
                1 -> EntranceNavigationTransitionInfo()
                2 -> DrillInNavigationTransitionInfo()
                3 -> SuppressNavigationTransitionInfo()
                4 -> SlideNavigationTransitionInfo().apply { effect = SlideNavigationTransitionEffect.FromRight }
                5 -> SlideNavigationTransitionInfo().apply { effect = SlideNavigationTransitionEffect.FromLeft }
                6 -> CommonNavigationTransitionInfo()
                else -> ContinuumNavigationTransitionInfo()
            } } } })
        children.add(TextBlock().apply { this.text = "Navigate"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(0.0, 12.0, 0.0, 8.0) })
        children.add(Button().apply { this.content = "Navigate Forward" }.also { galleryButton -> galleryButton.click.add { _, _ -> val next = if (frame.backStackDepth % 2 == 1) 1 else 2
            val selected = transition
            if (selected == null) frame.navigate(Page::class, next) else frame.navigate(Page::class, next, selected) } }.apply { margin = Thickness(0.0, 0.0, 0.0, 4.0); horizontalAlignment = HorizontalAlignment.Stretch })
        children.add(Button().apply { this.content = "Navigate Backward" }.also { galleryButton -> galleryButton.click.add { _, _ -> if (frame.canGoBack) frame.goBack() } }.apply { horizontalAlignment = HorizontalAlignment.Stretch }) }
    frame to options
}
