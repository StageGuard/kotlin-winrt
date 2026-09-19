package io.github.composefluent.winrt.gallery.fundamentals

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import windows.ui.text.FontWeight

@GalleryPage(route = "CustomXamlConditionals", title = "XAML Conditions", group = "FundamentalsItem", order = 5)
internal fun conditionalPage() = ExamplePage {
    spacing = 12.0
    children.add(InfoBar().apply {
        title = "Construction-time evaluation"; margin = Thickness(0.0, 24.0, 0.0, 0.0); isOpen = true; isClosable = false
        message = "The samples below are constructed against the default flag values (NewExperience=true, LegacyMode=false). Change the feature flags before constructing the page to select the other variant. Kotlin conditions create the projected controls directly."
    })
    example("Conditional elements.", customXamlConditionalsConditionalElementsSample())
    example("Conditional attributes.", customXamlConditionalsConditionalAttributesSample1())
    example("Conditional Setters in a Style.", customXamlConditionalsConditionalSettersInAStyleSample2())
}

@GallerySample(route = "CustomXamlConditionals", title = "Conditional elements.")
internal fun customXamlConditionalsConditionalElementsSample() = StackPanel().apply { this.spacing = 8.0; if (GalleryFeatureFlags.newExperience) children.add(InfoBar().apply { title = "New experience"; isOpen = true; isClosable = false; severity = InfoBarSeverity.Success; message = "This InfoBar is included because the 'NewExperience' flag was true when the page was constructed." })
        if (GalleryFeatureFlags.legacyMode) children.add(InfoBar().apply { title = "Legacy mode"; isOpen = true; isClosable = false; severity = InfoBarSeverity.Warning; message = "This InfoBar is included because the 'LegacyMode' flag was true when the page was constructed." }) }

@GallerySample(route = "CustomXamlConditionals", title = "Conditional attributes.")
internal fun customXamlConditionalsConditionalAttributesSample1() = Button().apply {
        content = "Background depends on the active flag"; foreground = brush(0xFFFFFFu)
        if (GalleryFeatureFlags.newExperience) background = brush(0x107C10u)
        if (GalleryFeatureFlags.legacyMode) background = brush(0xC42B1Cu)
    }

@GallerySample(route = "CustomXamlConditionals", title = "Conditional Setters in a Style.")
internal fun customXamlConditionalsConditionalSettersInAStyleSample2() = TextBlock().apply {
        text = "Heading styled with conditional Setters"
        style = Style(TextBlock::class).apply {
            if (GalleryFeatureFlags.newExperience) { setters.add(Setter(TextBlock.fontWeightProperty, FontWeight(600u))); setters.add(Setter(TextBlock.fontSizeProperty, 28.0)) }
            if (GalleryFeatureFlags.legacyMode) { setters.add(Setter(TextBlock.fontWeightProperty, FontWeight(400u))); setters.add(Setter(TextBlock.fontSizeProperty, 18.0)) }
        }
    }

// Ported from WinUI Gallery CustomXamlConditionals (MIT), using Kotlin construction conditions.


internal object GalleryFeatureFlags {
    const val newExperience = true
    const val legacyMode = false
}
