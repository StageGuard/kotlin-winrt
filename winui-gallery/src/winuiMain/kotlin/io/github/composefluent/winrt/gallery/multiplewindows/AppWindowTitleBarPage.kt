package io.github.composefluent.winrt.gallery.multiplewindows

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.windowing.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.MicaBackdrop
import windows.graphics.SizeInt32
import windows.ui.Color

@GalleryPage(route = "AppWindowTitleBar", title = "AppWindowTitleBar", group = "MultipleWindows", order = 1)
internal fun appWindowTitleBarPage() = ExamplePage {
    children.add(stack(0.0, true) {
        margin = Thickness(0.0, 8.0, 0.0, 0.0)
        children.add(label("For the default title bar and basic scenarios, use the "))
        children.add(HyperlinkButton().apply {
            content = "TitleBar"
            click.add { _, _ -> GalleryNavigationHost.navigate("TitleBar") }
        })
        children.add(label(" control."))
    })

    val colors = appWindowTitleBarColorCustomizationDemo()
    example("AppWindowTitleBar color customization.", colors.first, colors.second)

    val extending = appWindowTitleBarExtendingDemo()
    example("Extending content into the AppWindowTitleBar area.", extending.first, extending.second)

    val theme = appWindowTitleBarPreferredThemeDemo()
    example("AppWindowTitleBar preferred theme.", theme.first, theme.second)
}

private fun appWindowTitleBarColorCustomizationDemo() = run {
    var colorWindow: Window? = null
    val names = listOf(
        "BackgroundColor", "ForegroundColor", "ButtonBackgroundColor", "ButtonForegroundColor",
        "ButtonHoverBackgroundColor", "ButtonHoverForegroundColor", "InactiveBackgroundColor", "InactiveForegroundColor",
        "ButtonInactiveBackgroundColor", "ButtonInactiveForegroundColor", "ButtonPressedBackgroundColor", "ButtonPressedForegroundColor",
    )
    val colors = listOf(0xF2F6FAu, 0x1E2933u, 0x3B82F6u, 0xFFFFFFu, 0x2563EBu, 0xFFFFFFu, 0xE5EAF0u, 0x6B7280u, 0xCBD5E1u, 0x475569u, 0x1D4ED8u, 0xFFFFFFu).map(::rgb).toMutableList()
    fun applyColors() {
        colorWindow?.appWindow?.titleBar?.let {
            appWindowTitleBarColorCustomizationSample(it, colors[0], colors[1], colors[2], colors[3], colors[4], colors[5], colors[6], colors[7], colors[8], colors[9], colors[10], colors[11])
        }
    }
    val showWindow = Button().apply { content = "Show window" }
    showWindow.click.add { _, _ ->
        showWindow.isEnabled = false
        colorWindow = GalleryWindows.create("AppWindowTitleBarWindow", Grid().apply {
            children.add(label("This is a sample window to demonstrate AppWindowTitleBar color customization.", 16.0).apply {
                horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center; margin = inset(20.0)
            })
        }).apply {
            systemBackdrop = MicaBackdrop()
            checkNotNull(appWindow).apply {
                setPresenter(OverlappedPresenter.create().apply { isAlwaysOnTop = true; isResizable = false })
                resize(SizeInt32(600, 400))
            }
            closed.add { _, _ -> colorWindow = null; showWindow.isEnabled = true }
        }
        applyColors(); colorWindow?.activate()
    }
    val options = stack(16.0, true) {
        repeat(2) { group ->
            if (group == 1) children.add(AppBarSeparator())
            children.add(stack(8.0) {
                repeat(6) { offset ->
                    val index = group * 6 + offset
                    children.add(label(names[index])); children.add(colorSelector(colors[index]) { colors[index] = it; applyColors() }.apply { named(this, names[index]) })
                }
            })
        }
    }
    showWindow to options
}

private fun appWindowTitleBarExtendingDemo() = run {
    var extendedWindow: Window? = null
    var extend = true
    var heightIndex = 0
    val heights = listOf(TitleBarHeightOption.Standard, TitleBarHeightOption.Tall, TitleBarHeightOption.Collapsed)
    fun applyExtension() {
        extendedWindow?.appWindow?.titleBar?.let { appWindowTitleBarExtendingSample(it, extend, heights[heightIndex]) }
    }
    val showWindow = Button().apply { content = "Show window" }
    showWindow.click.add { _, _ ->
        showWindow.isEnabled = false
        extendedWindow = GalleryWindows.create("AppWindowTitleBarExtendWindow", Grid().apply {
            children.add(label("This is a sample window to demonstrate content extending into the title bar area and title bar height options.", 16.0).apply {
                horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center; margin = inset(20.0)
            })
        }).apply {
            systemBackdrop = MicaBackdrop()
            checkNotNull(appWindow).apply {
                setPresenter(OverlappedPresenter.create().apply { isAlwaysOnTop = true; isResizable = false })
                resize(SizeInt32(600, 400))
                checkNotNull(titleBar).buttonBackgroundColor = Color(0u, 0u, 0u, 0u)
            }
            closed.add { _, _ -> extendedWindow = null; showWindow.isEnabled = true }
        }
        applyExtension(); extendedWindow?.activate()
    }
    val options = stack(8.0) {
        children.add(option("Extend content into title bar", true) { extend = it; applyExtension() })
        children.add(select("TitleBarHeightOption", listOf("Standard", "Tall", "Collapsed")) { heightIndex = it; applyExtension() }.apply { width = 200.0 })
    }
    showWindow to options
}

private fun appWindowTitleBarPreferredThemeDemo() = run {
    var themeWindow: Window? = null
    var themeIndex = 1
    val themes = listOf(TitleBarTheme.UseDefaultAppMode, TitleBarTheme.Light, TitleBarTheme.Dark)
    val showWindow = Button().apply { content = "Show window" }
    showWindow.click.add { _, _ ->
        showWindow.isEnabled = false
        themeWindow = GalleryWindows.create("AppWindowTitleBarThemeHeightWindow", Grid().apply {
            children.add(label("This is a sample window to demonstrate AppWindowTitleBar theme customization.", 16.0).apply {
                horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center; margin = inset(20.0)
            })
        }).apply {
            systemBackdrop = MicaBackdrop()
            checkNotNull(appWindow).apply {
                setPresenter(OverlappedPresenter.create().apply { isAlwaysOnTop = true; isResizable = false })
                resize(SizeInt32(600, 400))
                appWindowTitleBarPreferredThemeSample(checkNotNull(titleBar), themes[themeIndex])
            }
            closed.add { _, _ -> themeWindow = null; showWindow.isEnabled = true }
            activate()
        }
    }
    val options = select("TitleBarTheme", listOf("UseDefaultAppMode", "Light", "Dark"), 1) {
        themeIndex = it; themeWindow?.appWindow?.titleBar?.let { bar -> appWindowTitleBarPreferredThemeSample(bar, themes[it]) }
    }.apply { width = 200.0 }
    showWindow to options
}

@GallerySample(route = "AppWindowTitleBar", title = "AppWindowTitleBar color customization.")
internal fun appWindowTitleBarColorCustomizationSample(
    titleBar: AppWindowTitleBar,
    backgroundColor: Color,
    foregroundColor: Color,
    buttonBackgroundColor: Color,
    buttonForegroundColor: Color,
    buttonHoverBackgroundColor: Color,
    buttonHoverForegroundColor: Color,
    inactiveBackgroundColor: Color,
    inactiveForegroundColor: Color,
    buttonInactiveBackgroundColor: Color,
    buttonInactiveForegroundColor: Color,
    buttonPressedBackgroundColor: Color,
    buttonPressedForegroundColor: Color,
) = run {
    titleBar.backgroundColor = backgroundColor
    titleBar.foregroundColor = foregroundColor
    titleBar.buttonBackgroundColor = buttonBackgroundColor
    titleBar.buttonForegroundColor = buttonForegroundColor
    titleBar.buttonHoverBackgroundColor = buttonHoverBackgroundColor
    titleBar.buttonHoverForegroundColor = buttonHoverForegroundColor
    titleBar.inactiveBackgroundColor = inactiveBackgroundColor
    titleBar.inactiveForegroundColor = inactiveForegroundColor
    titleBar.buttonInactiveBackgroundColor = buttonInactiveBackgroundColor
    titleBar.buttonInactiveForegroundColor = buttonInactiveForegroundColor
    titleBar.buttonPressedBackgroundColor = buttonPressedBackgroundColor
    titleBar.buttonPressedForegroundColor = buttonPressedForegroundColor
}

@GallerySample(route = "AppWindowTitleBar", title = "Extending content into the AppWindowTitleBar area.")
internal fun appWindowTitleBarExtendingSample(titleBar: AppWindowTitleBar, extend: Boolean, height: TitleBarHeightOption) = run {
    titleBar.extendsContentIntoTitleBar = extend
    if (extend) titleBar.preferredHeightOption = height
}

@GallerySample(route = "AppWindowTitleBar", title = "AppWindowTitleBar preferred theme.")
internal fun appWindowTitleBarPreferredThemeSample(titleBar: AppWindowTitleBar, theme: TitleBarTheme) = run {
    titleBar.preferredTheme = theme
}
