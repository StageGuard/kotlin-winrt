package io.github.composefluent.winrt.gallery.fundamentals

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.*
import microsoft.ui.xaml.media.imaging.BitmapImage
import windows.foundation.Uri
import windows.ui.text.FontWeight

@GalleryPage(route = "XamlStyles", title = "Style", group = "FundamentalsItem", order = 1)
internal fun stylesPage() = ExamplePage {
    children.add(label("The definition of styles is similar to other resources: app-level, page-level, control-level.\n\n• Styles are reusable collections of property settings for a specific control type.\n• A keyed style is used for explicit application, while an implicit style is used for automatic application to all controls of a type.\n• Styles improve maintainability, consistency, and reduce repetition in UI code.").apply { margin = Thickness(0.0, 12.0, 0.0, 0.0) })
    example("Creating and applying a style.", xamlStylesCreatingAndApplyingAStyleSample())
    example("Style without a key (implicit style).", xamlStylesStyleWithoutAKeyImplicitStyleSample1())
}

@GallerySample(route = "XamlStyles", title = "Creating and applying a style.")
internal fun xamlStylesCreatingAndApplyingAStyleSample() = StackPanel().apply { this.spacing = 8.0; val custom = Style(Button::class).apply {
            basedOn = controlStyle("ButtonRevealStyle")
            setters.add(Setter(Control.backgroundProperty, GalleryTheme.brush("AccentAcrylicBackgroundFillColorDefaultBrush")))
            setters.add(Setter(FrameworkElement.minWidthProperty, 200.0))
        }
        resources["CustomButtonStyle"] = custom
        children.add(Button().apply { content = "Default button" })
        children.add(Button().apply { content = "Styled button"; style = custom })
        children.add(Button().apply { content = "Styled button (overridden)"; style = custom; background = GalleryTheme.brush("SystemFillColorCriticalBackgroundBrush") }) }

@GallerySample(route = "XamlStyles", title = "Style without a key (implicit style).")
internal fun xamlStylesStyleWithoutAKeyImplicitStyleSample1() = StackPanel().apply { this.spacing = 0.0; resources[TextBlock::class] = Style(TextBlock::class).apply {
            setters.add(Setter(TextBlock.fontSizeProperty, 16.0)); setters.add(Setter(TextBlock.fontFamilyProperty, FontFamily("Consolas")))
            setters.add(Setter(TextBlock.fontWeightProperty, FontWeight(700u)))
        }
        children.add(TextBlock().apply { text = "This style is applied automatically!" }); children.add(TextBlock().apply { text = "No need to set a key." }) }
