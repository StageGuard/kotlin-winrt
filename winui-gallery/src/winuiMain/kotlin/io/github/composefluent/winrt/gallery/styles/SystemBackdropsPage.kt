package io.github.composefluent.winrt.gallery.styles

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.composition.ICompositionSupportsSystemBackdrop
import microsoft.ui.composition.systembackdrops.*
import microsoft.ui.system.ThemeSettings
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.*

@GalleryPage(route = "SystemBackdrops", title = "System Backdrops (Mica/Acrylic)", group = "Styles", order = 7)
internal fun systemBackdropsPage() = ExamplePage {
    example("System backdrop types.", systemBackdropTypesPreview(actualTheme))
    example("MicaController.", micaControllerPreview(actualTheme))
    example("DesktopAcrylicController.", desktopAcrylicControllerPreview(actualTheme))
}

@GallerySample(route = "SystemBackdrops", title = "System backdrop types.")
internal fun systemBackdropTypesSample(window: Window) = window.apply { systemBackdrop = MicaBackdrop() }

@GallerySample(route = "SystemBackdrops", title = "MicaController.")
internal fun micaControllerSample(window: Window, configuration: SystemBackdropConfiguration) = MicaController().apply {
    kind = MicaKind.Base
    addSystemBackdropTarget(window.asWinRT<ICompositionSupportsSystemBackdrop>())
    setSystemBackdropConfiguration(configuration)
}

@GallerySample(route = "SystemBackdrops", title = "DesktopAcrylicController.")
internal fun desktopAcrylicControllerSample(window: Window, configuration: SystemBackdropConfiguration) = DesktopAcrylicController().apply {
    kind = DesktopAcrylicKind.Base
    addSystemBackdropTarget(window.asWinRT<ICompositionSupportsSystemBackdrop>())
    setSystemBackdropConfiguration(configuration)
}

private fun systemBackdropTypesPreview(initialTheme: ElementTheme) = backdropPreview("SystemBackdrop sample window", initialTheme) { window, root, status, controls ->
    val types = listOf("Mica", "MicaAlt", "Acrylic", "None")
    var current = "None"
    fun updateTheme() {
        root.background = if (current == "None" && root.requestedTheme != ElementTheme.Default) {
            brush(if (root.actualTheme == ElementTheme.Light) 0xFFFFFFu else 0u)
        } else {
            SolidColorBrush(windows.ui.Color(0u, 0u, 0u, 0u))
        }
    }
    fun setBackdrop(type: String) {
        status.text = ""
        var availableType = type
        if (availableType.startsWith("Mica") && !MicaController.isSupported()) {
            status.text = "Mica isn't supported. Trying Acrylic."
            availableType = "Acrylic"
        }
        if (availableType.startsWith("Acrylic") && !DesktopAcrylicController.isSupported()) {
            status.text += " Acrylic isn't supported. Switching to default color."
            availableType = "None"
        }
        current = availableType
        if (availableType == "Mica") {
            systemBackdropTypesSample(window)
        } else {
            window.systemBackdrop = when (availableType) {
            "MicaAlt" -> MicaBackdrop().apply { kind = MicaKind.BaseAlt }
            "Acrylic" -> DesktopAcrylicBackdrop()
            else -> null
            }
        }
        updateTheme()
        announce(root, "Background changed to $current", "BackgroundChangedNotificationActivityId")
    }
    root.actualThemeChanged.add { _, _ -> updateTheme() }
    controls.children.add(
        ComboBox().apply {
            header = "Current backdrop"
            types.forEach { items.add(it) }
            selectedIndex = 0
            selectionChanged.add { _, _ -> if (selectedIndex >= 0) setBackdrop(types[selectedIndex]) }
        },
    )
    controls.children.add(themeSelector(root, ::updateTheme))
    window.closed.add { _, _ -> window.systemBackdrop = null }
    setBackdrop(types[0])
    window.activate()
}

private fun micaControllerPreview(initialTheme: ElementTheme) = backdropPreview("MicaController sample window", initialTheme) { window, root, status, controls ->
    val kinds = listOf("Mica", "MicaAlt")
    val themeSettings = ThemeSettings.createForWindowId(checkNotNull(window.appWindow).id)
    val configuration = backdropConfiguration(root, themeSettings)
    val controller = if (MicaController.isSupported()) micaControllerSample(window, configuration) else null
    if (controller == null) {
        status.text = if (DesktopAcrylicController.isSupported()) {
            "Mica isn't supported. Trying Acrylic."
        } else {
            "Mica isn't supported. Acrylic isn't supported either."
        }
        if (DesktopAcrylicController.isSupported()) window.systemBackdrop = DesktopAcrylicBackdrop()
    }
    fun updateTheme() {
        updateBackdropConfiguration(root, themeSettings, configuration)
        announce(root, "Mica theme changed", "BackgroundChangedNotificationActivityId")
    }
    root.actualThemeChanged.add { _, _ -> updateTheme() }
    val themeToken = themeSettings.changed.add { _, _ -> updateTheme() }
    controls.children.add(
        ComboBox().apply {
            header = "Mica kind"
            kinds.forEach { items.add(it) }
            selectedIndex = 0
            selectionChanged.add { _, _ ->
                if (selectedIndex >= 0) controller?.kind = if (selectedIndex == 1) MicaKind.BaseAlt else MicaKind.Base
            }
        },
    )
    controls.children.add(themeSelector(root, ::updateTheme))
    window.activated.add { _, args -> configuration.isInputActive = args.windowActivationState != WindowActivationState.Deactivated }
    window.closed.add { _, _ ->
        themeSettings.changed.remove(themeToken)
        controller?.close()
        window.systemBackdrop = null
    }
}

private fun desktopAcrylicControllerPreview(initialTheme: ElementTheme) = backdropPreview("DesktopAcrylicController sample window", initialTheme) { window, root, status, controls ->
    val kinds = listOf("Acrylic", "AcrylicThin")
    val themeSettings = ThemeSettings.createForWindowId(checkNotNull(window.appWindow).id)
    val configuration = backdropConfiguration(root, themeSettings)
    val controller = if (DesktopAcrylicController.isSupported()) desktopAcrylicControllerSample(window, configuration) else null
    if (controller == null) status.text = "Acrylic isn't supported. Switching to default color."
    fun updateTheme() {
        updateBackdropConfiguration(root, themeSettings, configuration)
        announce(root, "Acrylic theme changed", "BackgroundChangedNotificationActivityId")
    }
    root.actualThemeChanged.add { _, _ -> updateTheme() }
    val themeToken = themeSettings.changed.add { _, _ -> updateTheme() }
    controls.children.add(
        ComboBox().apply {
            header = "Acrylic kind"
            kinds.forEach { items.add(it) }
            selectedIndex = 0
            selectionChanged.add { _, _ ->
                if (selectedIndex >= 0) controller?.kind = if (selectedIndex == 1) DesktopAcrylicKind.Thin else DesktopAcrylicKind.Base
            }
        },
    )
    controls.children.add(themeSelector(root, ::updateTheme))
    window.activated.add { _, args -> configuration.isInputActive = args.windowActivationState != WindowActivationState.Deactivated }
    window.closed.add { _, _ ->
        themeSettings.changed.remove(themeToken)
        controller?.close()
        window.systemBackdrop = null
    }
}

private fun backdropPreview(
    title: String,
    initialTheme: ElementTheme,
    build: (Window, Grid, TextBlock, StackPanel) -> Unit,
): StackPanel = stack {
    children.add(TextBlock().apply {
        text = "Mica is an opaque material for main app windows. Desktop Acrylic is translucent and blurs the content behind the window. These system backdrops are applied to a separate AppWindow."
        fontSize = 14.0
        textWrapping = TextWrapping.Wrap
    })
    children.add(Button().apply {
        content = "Show window"
        click.add { _, _ ->
            val root = Grid().apply {
                requestedTheme = initialTheme
                rowDefinitions.add(RowDefinition().apply { height = GridLength(32.0, GridUnitType.Pixel) })
                rowDefinitions.add(starRow())
            }
            val window = Window().also { GalleryWindows.track(it) }.apply {
                this.title = title
                extendsContentIntoTitleBar = true
                content = root
            }
            checkNotNull(window.dispatcherQueue).ensureSystemDispatcherQueue()
            val titleBar = TextBlock().apply {
                text = title
                fontSize = 12.0
                textWrapping = TextWrapping.Wrap
                margin = Thickness(38.0, 0.0, 0.0, 0.0)
                verticalAlignment = VerticalAlignment.Center
            }
            root.children.add(titleBar)
            window.setTitleBar(titleBar)
            val status = TextBlock().apply {
                text = ""
                fontSize = 14.0
                textWrapping = TextWrapping.Wrap
                horizontalAlignment = HorizontalAlignment.Center
            }
            val controls = StackPanel().apply {
                spacing = 20.0
                Grid.setRow(this, 1)
                horizontalAlignment = HorizontalAlignment.Center
                verticalAlignment = VerticalAlignment.Center
                children.add(status)
            }
            root.children.add(controls)
            build(window, root, status, controls)
            window.activate()
        }
    })
}

private fun themeSelector(root: Grid, changed: () -> Unit) = ComboBox().apply {
    header = "Window theme"
    listOf("Use system setting", "Light", "Dark").forEach { items.add(it) }
    selectedIndex = 0
    selectionChanged.add { _, _ ->
        if (selectedIndex >= 0) {
            root.requestedTheme = listOf(ElementTheme.Default, ElementTheme.Light, ElementTheme.Dark)[selectedIndex]
            changed()
        }
    }
}

private fun backdropConfiguration(root: Grid, themeSettings: ThemeSettings) = SystemBackdropConfiguration().apply {
    isInputActive = true
    isHighContrast = themeSettings.highContrast
    theme = if (root.actualTheme == ElementTheme.Dark) SystemBackdropTheme.Dark else SystemBackdropTheme.Light
}

private fun updateBackdropConfiguration(
    root: Grid,
    themeSettings: ThemeSettings,
    configuration: SystemBackdropConfiguration,
) {
    configuration.isHighContrast = themeSettings.highContrast
    configuration.theme = if (root.actualTheme == ElementTheme.Dark) SystemBackdropTheme.Dark else SystemBackdropTheme.Light
}
