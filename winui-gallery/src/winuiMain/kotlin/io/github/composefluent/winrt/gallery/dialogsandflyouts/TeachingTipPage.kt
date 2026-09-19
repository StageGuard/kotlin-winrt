package io.github.composefluent.winrt.gallery.dialogsandflyouts

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "TeachingTip", title = "TeachingTip", group = "DialogsAndFlyouts", order = 3)
internal fun teachingTipPage() = ExamplePage {
    example("A targeted TeachingTip.", teachingTipTargetedSample())
    example("A non-targeted TeachingTip.", teachingTipNonTargetedSample())
    example("A TeachingTip with hero content.", teachingTipHeroContentSample())
}

@GallerySample(route = "TeachingTip", title = "A targeted TeachingTip.")
internal fun teachingTipTargetedSample() = run {
    val tip = TeachingTip().apply {
        title = "This is the title"; subtitle = "And this is the subtitle"
        iconSource = SymbolIconSource().apply { symbol = Symbol.Refresh }
    }
    val launch = Button().apply { this.content = "Show TeachingTip" }.also { galleryButton -> galleryButton.click.add { _, _ -> tip.isOpen = true } }
    tip.target = launch
    Grid().apply { children.add(launch); children.add(tip) }
}

@GallerySample(route = "TeachingTip", title = "A non-targeted TeachingTip.")
internal fun teachingTipNonTargetedSample() = run {
    val tip = TeachingTip().apply {
        title = "This is the title"; subtitle = "And this is the subtitle"
        actionButtonContent = "Action button"; closeButtonContent = "Close button"
        isLightDismissEnabled = true; placementMargin = inset(20.0)
    }
    val launch = Button().apply { this.content = "Show TeachingTip" }.also { galleryButton -> galleryButton.click.add { _, _ -> tip.isOpen = true } }
    Grid().apply { children.add(launch); children.add(tip) }
}

@GallerySample(route = "TeachingTip", title = "A TeachingTip with hero content.")
internal fun teachingTipHeroContentSample() = run {
    val tip = TeachingTip().apply {
        title = "This is the title"; subtitle = "And this is the subtitle"
        target = null; preferredPlacement = TeachingTipPlacementMode.Bottom
        heroContent = Image().apply { this.width = 320.0; this.height = 320.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/sunset.jpg") ) }
        content = TextBlock().apply { this.text = "Description can go here"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(0.0, 16.0, 0.0, 0.0) }
    }
    val launch = Button().apply { this.content = "Show TeachingTip" }.also { galleryButton -> galleryButton.click.add { _, _ -> tip.isOpen = true } }
    tip.target = launch
    Grid().apply { children.add(launch); children.add(tip) }
}
