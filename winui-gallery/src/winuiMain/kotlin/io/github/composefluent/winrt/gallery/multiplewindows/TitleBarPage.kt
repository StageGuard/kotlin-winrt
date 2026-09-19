package io.github.composefluent.winrt.gallery.multiplewindows

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.windowing.TitleBarHeightOption
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.MicaBackdrop
import microsoft.ui.xaml.media.imaging.BitmapImage
import windows.foundation.Uri

@GalleryPage(route = "TitleBar", title = "TitleBar", group = "MultipleWindows", order = 3)
internal fun titleBarPage() = ExamplePage {
    margin = Thickness(0.0, 12.0, 0.0, 0.0)
    children.add(stack(4.0, true) {
        children.add(label("For full title bar customization without using the TitleBar control, see the"))
        children.add(HyperlinkButton().apply { content = "AppWindowTitleBar sample"; click.add { _, _ -> GalleryNavigationHost.navigate("AppWindowTitleBar") } })
    })
    val title = titleBarConfigurationSample()
    example("TitleBar configuration.", Border().apply {
        background = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush"); borderBrush = GalleryTheme.brush("SurfaceStrokeColorDefaultBrush"); borderThickness = inset(1.0); cornerRadius = corners(8.0); child = title
    }, stack {
        width = 240.0
        children.add(TextBox().apply { header = "Title"; text = title.title; textChanged.add { _, _ -> title.title = text } })
        children.add(TextBox().apply { header = "Subtitle"; text = title.subtitle; textChanged.add { _, _ -> title.subtitle = text } })
        children.add(ToggleSwitch().apply { header = "IsBackButtonVisible"; toggled.add { _, _ -> title.isBackButtonVisible = isOn } })
        children.add(ToggleSwitch().apply { header = "IsPaneToggleButtonVisible"; toggled.add { _, _ -> title.isPaneToggleButtonVisible = isOn } })
    })
    example("TitleBar drag regions.", titleBarTitleBarDragRegionsSample(actualTheme))
    example("End to end TitleBar sample.", titleBarEndToEndSample(actualTheme))
}

@GallerySample(route = "TitleBar", title = "TitleBar configuration.")
internal fun titleBarConfigurationSample() = TitleBar().apply {
    title = "Kotlin WinUI Gallery"; subtitle = "Preview"
    resources["TitleBarContentHorizontalAlignment"] = HorizontalAlignment.Stretch
    iconSource = ImageIconSource().apply { imageSource = BitmapImage(Uri("ms-appx:///Assets/AppList.png")) }
    rightHeader = PersonPicture().apply { width = 30.0; height = 30.0; initials = "JD" }
    content = AutoSuggestBox().apply {
        maxWidth = 580.0; horizontalAlignment = HorizontalAlignment.Stretch; verticalAlignment = VerticalAlignment.Center
        placeholderText = "Search..."; queryIcon = SymbolIcon(Symbol.Find)
    }
}

@GallerySample(route = "TitleBar", title = "TitleBar drag regions.")
internal fun titleBarTitleBarDragRegionsSample(theme: ElementTheme) = stack {
    maxWidth = 560.0
    children.add(TextBlock().apply { this.text = "Drag regions can only be observed on a real window. Click the button below to open a sample window where you can toggle TitleBar.IsDragRegion on a status badge and call RecomputeDragRegions() after dynamic content changes."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { textAlignment = TextAlignment.Center })
    children.add(Button().apply { this.content = "Show window" }.also { galleryButton -> galleryButton.click.add { _, _ -> val root = Grid().apply { requestedTheme = theme; rowDefinitions.add(autoRow()); rowDefinitions.add(starRow()) }
        val title = TitleBar().apply {
            this.title = "Drag regions"; subtitle = "Try dragging the window"; rightHeader = null
            resources["TitleBarContentHorizontalAlignment"] = HorizontalAlignment.Stretch
            iconSource = ImageIconSource().apply { imageSource = BitmapImage(Uri("ms-appx:///Assets/AppList.png")) }
        }
        val status = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") }
        val badge = Button().apply { this.content = "Status" }.also { galleryButton -> galleryButton.click.add { _, _ -> status.text = "Status badge clicked" } }.apply { style = controlStyle("AccentButtonStyle"); verticalAlignment = VerticalAlignment.Center }
        val right = StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; verticalAlignment = VerticalAlignment.Center; children.add(badge) }
        title.content = Grid().apply {
            columnSpacing = 8.0; columnDefinitions.add(column(1.0, GridUnitType.Star)); columnDefinitions.add(column(1.0, GridUnitType.Auto))
            children.add(AutoSuggestBox().apply { maxWidth = 580.0; horizontalAlignment = HorizontalAlignment.Stretch; verticalAlignment = VerticalAlignment.Center; placeholderText = "Search..."; queryIcon = SymbolIcon(Symbol.Find) })
            Grid.setColumn(right, 1); children.add(right)
        }
        var extra: Button? = null
        root.children.add(title)
        root.children.add(ScrollViewer().apply { this.content = StackPanel().apply { this.spacing = 16.0; maxWidth = 640.0; padding = Thickness(32.0, 24.0, 32.0, 24.0)
            children.add(TextBlock().apply { this.text = "Custom drag regions"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(TextBlock().apply { this.text = "Try dragging the window from different parts of the title bar. Interactive controls (like the search box) are automatically excluded from the drag region by the new default behavior in Windows App SDK 2.1."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(TextBlock().apply { this.text = "Status badge: TitleBar.IsDragRegion"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "Pick a value for the badge in the title bar."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(RadioButtons().apply { this.header = ""; listOf("Unset (framework decides — clickable, since Button is interactive)", "True (always draggable — overrides the framework default)", "False (always clickable)").forEach { this.items.add(it) }; this.selectedIndex = 0 }.also { galleryRadioButtons -> galleryRadioButtons.selectionChanged.add { _, _ -> val gallerySelectedIndex = galleryRadioButtons.selectedIndex; if (gallerySelectedIndex in 0 until galleryRadioButtons.items.size) { val it = gallerySelectedIndex; if (it == 0) badge.clearValue(TitleBar.isDragRegionProperty) else TitleBar.setIsDragRegion(badge, it == 1) } } })
            children.add(TextBlock().apply { this.text = "Dynamic content"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "When you add or remove elements in TitleBar.Content at runtime, call RecomputeDragRegions() to refresh."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; children.add(Button().apply { this.content = "Toggle extra title bar button" }.also { galleryButton -> galleryButton.click.add { _, _ -> val existing = extra
                    if (existing == null) {
                        extra = Button().apply { content = "Extra"; verticalAlignment = VerticalAlignment.Center }.also { right.children.add(0, it) }
                        status.text = "Added a Button to TitleBar.Content. Call RecomputeDragRegions() to refresh drag regions."
                    } else { right.children.remove(existing); extra = null; status.text = "Removed the Button. Call RecomputeDragRegions() to refresh drag regions." } } })
                children.add(Button().apply { this.content = "RecomputeDragRegions()" }.also { galleryButton -> galleryButton.click.add { _, _ -> title.recomputeDragRegions(); status.text = "RecomputeDragRegions() called." } }) })
            children.add(status) }; this.verticalScrollBarVisibility = ScrollBarVisibility.Auto; this.horizontalScrollBarVisibility = ScrollBarVisibility.Disabled; this.horizontalContentAlignment = HorizontalAlignment.Stretch; this.verticalContentAlignment = VerticalAlignment.Top }.apply { Grid.setRow(this, 1) })
        GalleryWindows.create("TitleBar drag regions sample", root).apply {
            systemBackdrop = MicaBackdrop(); extendsContentIntoTitleBar = true
            checkNotNull(checkNotNull(appWindow).titleBar).preferredHeightOption = TitleBarHeightOption.Tall
            setTitleBar(title); activate()
        } } }.apply { horizontalAlignment = HorizontalAlignment.Center; style = controlStyle("AccentButtonStyle") })
}

@GallerySample(route = "TitleBar", title = "End to end TitleBar sample.")
internal fun titleBarEndToEndSample(theme: ElementTheme) = stack {
    maxWidth = 560.0
    children.add(TextBlock().apply { this.text = "Click the button below to see an end to end sample of a TitleBar in a new window, binding some of its properties to the NavigationView and navigation frame."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { textAlignment = TextAlignment.Center })
    children.add(Button().apply { this.content = "Show window" }.also { galleryButton -> galleryButton.click.add { _, _ -> val title = TitleBar().apply {
            this.title = "Kotlin WinUI Gallery"; subtitle = "TitleBar sample"; isPaneToggleButtonVisible = true
            resources["TitleBarContentHorizontalAlignment"] = HorizontalAlignment.Stretch
            iconSource = ImageIconSource().apply { imageSource = BitmapImage(Uri("ms-appx:///Assets/AppList.png")) }
            rightHeader = PersonPicture().apply { width = 30.0; height = 30.0; initials = "JD" }
            content = AutoSuggestBox().apply { maxWidth = 580.0; horizontalAlignment = HorizontalAlignment.Stretch; verticalAlignment = VerticalAlignment.Center; placeholderText = "Search..."; queryIcon = SymbolIcon(Symbol.Find) }
        }
        val frame = Frame()
        val navigation = NavigationView().apply { isBackButtonVisible = NavigationViewBackButtonVisible.Collapsed; isPaneToggleButtonVisible = false; isSettingsVisible = false; content = frame }
        val icons = listOf(Symbol.Play, Symbol.Save, Symbol.Refresh, Symbol.Download)
        repeat(4) { index -> navigation.menuItems.add(NavigationViewItem().apply { content = "Menu Item${index + 1}"; icon = SymbolIcon(icons[index]); tag = index + 1 }) }
        title.paneToggleRequested.add { _, _ -> navigation.isPaneOpen = !navigation.isPaneOpen }
        title.backRequested.add { _, _ -> if (frame.canGoBack) frame.goBack() }
        frame.navigated.add { _, args ->
            val index = args.parameter.toString().toInt(); checkNotNull(args.content).asWinRT<Page>().content = sampleContent(index)
            title.isBackButtonVisible = frame.canGoBack; navigation.header = "Sample Page $index"
        }
        navigation.selectionChanged.add { _, args -> args.selectedItem?.let { frame.navigate(Page::class, it.asWinRT<NavigationViewItem>().tag) } }
        navigation.selectedItem = navigation.menuItems[0]
        val root = Grid().apply {
            requestedTheme = theme; rowDefinitions.add(autoRow()); rowDefinitions.add(starRow()); children.add(title); Grid.setRow(navigation, 1); children.add(navigation)
        }
        GalleryWindows.create("TitleBarWindow", root).apply {
            systemBackdrop = MicaBackdrop(); extendsContentIntoTitleBar = true
            checkNotNull(checkNotNull(appWindow).titleBar).preferredHeightOption = TitleBarHeightOption.Tall
            setTitleBar(title); activate()
        } } }.apply { horizontalAlignment = HorizontalAlignment.Center; style = controlStyle("AccentButtonStyle") })
}
