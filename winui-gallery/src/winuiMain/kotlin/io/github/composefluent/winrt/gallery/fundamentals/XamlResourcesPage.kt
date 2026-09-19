package io.github.composefluent.winrt.gallery.fundamentals

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.*
import microsoft.ui.xaml.media.imaging.BitmapImage
import windows.foundation.Uri
import windows.ui.text.FontWeight

@GalleryPage(route = "XamlResources", title = "Resources", group = "FundamentalsItem", order = 0)
internal fun resourcesPage() = ExamplePage {
    children.add(label("Creating and using resources", 20.0).apply { margin = Thickness(0.0, 12.0, 0.0, 4.0) })
    children.add(label("Resources are defined using ResourceDictionary. The important parts are the resource's key (a unique identifier) and the value (like a color or brush).\n\n• App-level: Resources are defined globally, accessible throughout the application.\n• Page-level: Resources are defined specific to a particular page.\n• Control-level: Resources are defined local to a specific control, such as a Button or Grid.\n\nTips\n• Naming: descriptive keys should always be used for resources to make them easier to identify.\n• Scope: Resources should be defined at the narrowest scope possible to improve maintainability.\n• Access: Resources[" + '"' + "Key" + '"' + "] is used for runtime access."))

    example("Resources.", resourceDictionaryScopeSample())
    children.add(label("Theme resources", 20.0).apply { margin = Thickness(0.0, 24.0, 0.0, 4.0); fontWeight = FontWeight(600u) })
    children.add(HyperlinkButton().apply { content = "WinUI 3 includes built-in theme resources for commonly used colors. See all brushes on the Color page."; click.add { _, _ -> GalleryNavigationHost.navigate("Color") } })
    children.add(label("• ThemeResource is used for dynamic theme-based updates.\n• ThemeDictionaries are defined to provide different values for light and dark themes.\n• A fallback value should always be provided to ensure compatibility with undefined themes."))
    example("StaticResource versus ThemeResource", staticVersusThemeResourceSample())
    example("Define a new theme resource.", customThemeResourceSample())



}

@GallerySample(route = "XamlResources", title = "Resources.")
internal fun resourceDictionaryScopeSample() = StackPanel().apply { this.spacing = 0.0; resources["PrimaryColor"] = rgb(0x0078D4u)
    resources["HighlightBrush"] = brush(0xA94DC1u)
    resources["FontColor"] = brush(0xFFFFFFu)
    val pageResources = resources
    padding = inset(8.0); background = SolidColorBrush(pageResources["PrimaryColor"] as windows.ui.Color); cornerRadius = corners(4.0)
    children.add(TextBlock().apply { this.text = "Using application-level resources"; this.fontSize = 24.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { foreground = brush(0xFFFFFFu) })
    children.add(StackPanel().apply { this.spacing = 0.0; margin = inset(8.0); padding = inset(8.0); background = checkNotNull(pageResources["HighlightBrush"]).asWinRT(); cornerRadius = corners(4.0)
        children.add(TextBlock().apply { this.text = "Using page-level resources"; this.fontSize = 18.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { foreground = checkNotNull(pageResources["FontColor"]).asWinRT() })
        children.add(StackPanel().apply { this.spacing = 0.0; margin = inset(8.0); padding = inset(8.0); cornerRadius = corners(4.0)
            resources["BackgroundColor"] = rgb(0xE2241Au)
            resources["Description"] = "Using control-level resources"
            val local = resources
            children.add(Grid().apply {
                padding = inset(8.0); cornerRadius = corners(4.0)
                background = SolidColorBrush(local["BackgroundColor"] as windows.ui.Color)
                children.add(TextBlock().apply { this.text = local["Description"].toString(); this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { foreground = brush(0xFFFFFFu) })
            }) }) }) }

@GallerySample(route = "XamlResources", title = "StaticResource versus ThemeResource")
internal fun staticVersusThemeResourceSample() = StackPanel().apply { this.spacing = 0.0; val staticText = TextBlock().apply { this.text = "StaticResource uses the value defined when the app starts and does not update when the theme changes."; this.fontSize = 16.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val dynamicText = TextBlock().apply { this.text = "ThemeResource adapts automatically to the current theme. If the app switches from light to dark, the color defined by ThemeResource changes."; this.fontSize = 16.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val staticPanel = Grid().apply { children.add(staticText) }
    val dynamicPanel = Grid().apply { children.add(dynamicText) }
    var initialized = false
    fun refreshTheme() {
        val backgroundColor = GalleryTheme.resource("SolidBackgroundFillColorBase", actualTheme) as windows.ui.Color
        val foregroundValue = GalleryTheme.resource("TextFillColorPrimaryBrush", actualTheme).asWinRT<SolidColorBrush>()
        dynamicPanel.background = SolidColorBrush(backgroundColor); dynamicText.foreground = foregroundValue
        if (!initialized) {
            staticPanel.background = SolidColorBrush(backgroundColor)
            staticText.foreground = SolidColorBrush(foregroundValue.color)
            initialized = true
        }
    }
    children.add(TextBlock().apply { this.text = "Toggle the theme using the theme switch button in the top right corner."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(0.0, 0.0, 0.0, 16.0) })
    children.add(staticPanel); children.add(dynamicPanel)
    loaded.add { _, _ -> refreshTheme() }
    actualThemeChanged.add { _, _ -> refreshTheme() } }

@GallerySample(route = "XamlResources", title = "Define a new theme resource.")
internal fun customThemeResourceSample() = stack {
    val image = Image()
    val themeName = TextBlock().apply { this.text = ""; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val themePanel = StackPanel().apply { this.spacing = 4.0; maxWidth = 700.0; padding = inset(8.0); horizontalAlignment = HorizontalAlignment.Center; cornerRadius = corners(4.0)
        children.add(themeName); children.add(image) }
    listOf("Default", "Dark").forEach { name ->
        themePanel.resources.themeDictionaries[name] = ResourceDictionary().apply {
            val dark = name == "Dark"
            this["BackgroundBrush"] = brush(if (dark) 0x333333u else 0xEEEEEEu)
            this["TextBrush"] = brush(if (dark) 0xEEEEEEu else 0x333333u)
            this["ThemeString"] = if (dark) "Dark theme" else "Light theme"
            this["ImageSource"] = BitmapImage(Uri("ms-appx:///Assets/SampleMedia/${if (dark) "Dark" else "Light"}_Image.png"))
        }
    }
    fun refreshCustomTheme() {
        val dictionary = checkNotNull(themePanel.resources.themeDictionaries[if (actualTheme == ElementTheme.Dark) "Dark" else "Default"]).asWinRT<ResourceDictionary>()
        themePanel.background = checkNotNull(dictionary["BackgroundBrush"]).asWinRT()
        themeName.foreground = checkNotNull(dictionary["TextBrush"]).asWinRT()
        themeName.text = dictionary["ThemeString"].toString()
        image.source = checkNotNull(dictionary["ImageSource"]).asWinRT()
    }
    children.add(TextBlock().apply { this.text = "Toggle the theme using the theme switch button in the top right corner."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    children.add(themePanel)
    loaded.add { _, _ -> refreshCustomTheme() }
    actualThemeChanged.add { _, _ -> refreshCustomTheme() }
}
