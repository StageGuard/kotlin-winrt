package io.github.composefluent.winrt.gallery.multiplewindows

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import kotlinx.coroutines.delay
import microsoft.ui.windowing.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.hosting.DesktopWindowXamlSource
import microsoft.ui.xaml.media.MicaBackdrop
import windows.graphics.*
import windows.system.VirtualKey
import windows.system.VirtualKeyModifiers

@GalleryPage(route = "AppWindow", title = "AppWindow", group = "MultipleWindows", order = 0)
internal fun appWindowPage() = ExamplePage {
    children.add(label("General usage of AppWindow", 20.0).apply { margin = Thickness(0.0, 16.0, 0.0, 0.0) })
    val creating = appWindowCreatingDemo(actualTheme)
    example("Creating and customizing an AppWindow window.", creating.first, creating.second)
    example("Centering an AppWindow in the screen's available area.", appWindowCenteringDemo())
    children.add(label("AppWindow Presenters", 20.0).apply { margin = Thickness(0.0, 24.0, 0.0, 0.0) })
    val overlapped = appWindowOverlappedPresenterDemo()
    example("AppWindow with OverlappedPresenter.", overlapped.first, overlapped.second)
    val constraints = appWindowConstraintsDemo()
    example("Setting minimum and maximum width and height.", constraints.first, constraints.second)
    example("Modal window with OverlappedPresenter and AppWindow.", appWindowModalDemo(this))
    example("AppWindow with FullScreenPresenter.", appWindowFullScreenDemo())
    val overlay = appWindowCompactOverlayDemo()
    example("AppWindow with CompactOverlayPresenter.", overlay.first, overlay.second)
}

private fun appWindowCreatingDemo(theme: ElementTheme) = run {
    fun number(name: String, initial: Double, min: Double, max: Double) = NumberBox().apply {
        header = name; value = initial; minimum = min; maximum = max; smallChange = 10.0; largeChange = 100.0
        spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
    }
    val title = TextBox().apply { header = "Window title"; placeholderText = "Enter window title"; text = "This is a title" }
    val width = number("Width", 800.0, 200.0, 1000.0); val height = number("Height", 500.0, 200.0, 700.0)
    val x = number("X", 50.0, 0.0, 800.0); val y = number("Y", 50.0, 0.0, 300.0)
    val openWindow = Button("Show window") {
        val content = stack(10.0) { horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center; requestedTheme = theme }
        val window = appWindowCreatingSample(content, title.text, RectInt32(x.value.toInt(), y.value.toInt(), width.value.toInt(), height.value.toInt()))
        GalleryWindows.track(window)
        val app = checkNotNull(window.appWindow)
        checkNotNull(app.titleBar).preferredTheme = TitleBarTheme.UseDefaultAppMode
        content.children.add(Button("Close") { window.close() })
        content.keyboardAccelerators.add(shortcut(VirtualKey.Escape, VirtualKeyModifiers.None).apply { invoked.add { _, args -> window.close(); args.handled = true } })
        window.activate()
    }
    val options = stack(8.0) {
        children.add(title); children.add(label("Window size")); children.add(stack(8.0, true) { children.add(width); children.add(height) })
        children.add(label("Window position")); children.add(stack(8.0, true) { children.add(x); children.add(y) })
    }
    openWindow to options
}

private fun appWindowCenteringDemo() = Button("Show centered sample window") {
    val content = stack(10.0) { horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
    val window = GalleryWindows.create("AppWindow sample", content).apply { systemBackdrop = MicaBackdrop() }
    val app = checkNotNull(window.appWindow)
    app.resize(SizeInt32(800, 500)); checkNotNull(app.titleBar).preferredTheme = TitleBarTheme.UseDefaultAppMode
    content.children.add(Button("Close") { window.close() })
    content.keyboardAccelerators.add(shortcut(VirtualKey.Escape, VirtualKeyModifiers.None).apply { invoked.add { _, args -> window.close(); args.handled = true } })
    appWindowCenteringSample(app)
    window.activate()
}

private fun appWindowOverlappedPresenterDemo() = run {
    val alwaysOnTop = ToggleSwitch().apply { header = "IsAlwaysOnTop"; isOn = false; onContent = "true"; offContent = "false" }
    val maximizable = ToggleSwitch().apply { header = "IsMaximizable"; isOn = true; onContent = "true"; offContent = "false" }
    val minimizable = ToggleSwitch().apply { header = "IsMinimizable"; isOn = true; onContent = "true"; offContent = "false" }
    val resizable = ToggleSwitch().apply { header = "IsResizable"; isOn = true; onContent = "true"; offContent = "false" }
    val hasBorder = ToggleSwitch().apply { header = "HasBorder"; isOn = true; onContent = "true"; offContent = "false" }
    val hasTitleBar = ToggleSwitch().apply { header = "HasTitleBar"; isOn = true; onContent = "true"; offContent = "false" }
    hasBorder.toggled.add { _, _ -> if (!hasBorder.isOn) hasTitleBar.isOn = false }
    hasTitleBar.toggled.add { _, _ -> if (hasTitleBar.isOn) hasBorder.isOn = true }
    val sample = stack(8.0) {
        children.add(label("OverlappedPresenter is the default presenter for AppWindow, providing a standard resizable window with system buttons. It is used for typical app windows and can be customized to control resizing and button visibility."))
        children.add(InfoBar().apply {
            title = "Warning"; severity = InfoBarSeverity.Warning; isOpen = true; isClosable = false
            message = "For an AppWindow with OverlappedPresenter, if the title bar (HasTitleBar = true) is enabled, the window must have a border (HasBorder = true). Setting HasBorder to false while HasTitleBar is true will result in a fatal error."
        })
        children.add(Button("Show window") {
            val content = stack(10.0) { horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
            val window = GalleryWindows.create("AppWindow sample", content).apply { systemBackdrop = MicaBackdrop() }
            val app = checkNotNull(window.appWindow)
            app.resize(SizeInt32(800, 500)); checkNotNull(app.titleBar).preferredTheme = TitleBarTheme.UseDefaultAppMode
            content.children.add(Button("Close") { window.close() })
            content.keyboardAccelerators.add(shortcut(VirtualKey.Escape, VirtualKeyModifiers.None).apply { invoked.add { _, args -> window.close(); args.handled = true } })
            val presenter = appWindowOverlappedPresenterSample(app, alwaysOnTop.isOn, maximizable.isOn, minimizable.isOn, resizable.isOn, hasBorder.isOn, hasTitleBar.isOn)
            val tasks = GalleryPageTasks(content)
            val maximize = Button().apply { this.content = "Maximize"; width = 200.0 }
            maximize.click.add { _, _ ->
                if (presenter.state == OverlappedPresenterState.Maximized) presenter.restore() else presenter.maximize()
                maximize.content = if (presenter.state == OverlappedPresenterState.Maximized) "Restore" else "Maximize"
            }
            content.children.add(maximize)
            content.children.add(Button("Minimize") { presenter.minimize() }.apply { width = 200.0 })
            content.children.add(Button("Minimize and restore the window after 3 seconds") {
                presenter.minimize(); tasks.launch { delay(3000); presenter.restore() }
            }.apply { width = 200.0 })
            window.activate()
        })
    }
    val options = stack(8.0) { listOf(alwaysOnTop, maximizable, minimizable, resizable, hasBorder, hasTitleBar).forEach { children.add(it) } }
    sample to options
}

private fun appWindowConstraintsDemo() = run {
    fun number(name: String, initial: Double) = NumberBox().apply {
        header = name; value = initial; minimum = 1.0; maximum = 10000.0; smallChange = 10.0; largeChange = 100.0
        spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
    }
    val values = listOf("PreferredMinimumWidth" to 400.0, "PreferredMinimumHeight" to 400.0, "PreferredMaximumWidth" to 1000.0, "PreferredMaximumHeight" to 1000.0).map { number(it.first, it.second) }
    val error = InfoBar().apply { severity = InfoBarSeverity.Error; title = "Invalid window constraints"; isOpen = false }
    val sample = stack(8.0) {
        children.add(label("The minimum and maximum width and height can be set on an AppWindow. When setting the maximum width or height, it's recommended to disable the window maximization."))
        children.add(error)
        children.add(Button("Show window") {
            val dimensions = values.map { it.value }
            error.isOpen = dimensions.any { !it.isFinite() } || dimensions[0] > dimensions[2] || dimensions[1] > dimensions[3]
            if (error.isOpen) error.message = "Minimum dimensions must not exceed maximum dimensions."
            else {
                val content = stack(10.0) { horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
                val window = GalleryWindows.create("AppWindow sample", content).apply { systemBackdrop = MicaBackdrop() }
                val app = checkNotNull(window.appWindow)
                app.resize(SizeInt32(800, 500)); checkNotNull(app.titleBar).preferredTheme = TitleBarTheme.UseDefaultAppMode
                content.children.add(Button("Close") { window.close() })
                content.keyboardAccelerators.add(shortcut(VirtualKey.Escape, VirtualKeyModifiers.None).apply { invoked.add { _, args -> window.close(); args.handled = true } })
                appWindowConstraintsSample(app, dimensions[0].toInt(), dimensions[1].toInt(), dimensions[2].toInt(), dimensions[3].toInt())
                val presenter = checkNotNull(app.presenter).asWinRT<OverlappedPresenter>()
                content.children.add(Button("Maximize") { if (presenter.state == OverlappedPresenterState.Maximized) presenter.restore() else presenter.maximize() }.apply { width = 200.0 })
                content.children.add(Button("Minimize") { presenter.minimize() }.apply { width = 200.0 })
                window.activate()
            }
        })
    }
    val options = stack(8.0) { values.forEach { children.add(it) } }
    sample to options
}

private fun appWindowModalDemo(owner: FrameworkElement) = stack {
    children.add(label("A modal window is a separate window that blocks interaction with its owner window until it is closed, often used for critical actions like confirmations, authentication, or settings. Unlike a ContentDialog, which is a lightweight pop-up within the same window, a modal window is a fully independent window, making it suitable for multi-window applications or scenarios requiring more flexibility in layout and behavior."))
    children.add(Button("Show modal window") {
        val ownerId = checkNotNull(checkNotNull(owner.xamlRoot).contentIslandEnvironment).appWindowId
        val window = appWindowModalSample(ownerId)
        GalleryWindows.track(window)
        val source = DesktopWindowXamlSource()
        source.initialize(window.id)
        source.content = stack {
            requestedTheme = owner.actualTheme; horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center
            children.add(label("Modal window", 20.0)); children.add(label("Close this window to return to the Gallery."))
            children.add(stack(8.0, true) { children.add(Button("OK") { window.destroy() }); children.add(Button("Cancel") { window.destroy() }) })
            keyboardAccelerators.add(shortcut(VirtualKey.Escape, VirtualKeyModifiers.None).apply { invoked.add { _, args -> window.destroy(); args.handled = true } })
        }
        val scale = checkNotNull(owner.xamlRoot).rasterizationScale
        window.title = "Modal window"; window.resizeClient(SizeInt32((400 * scale).toInt(), (300 * scale).toInt()))
        fun resize() { val size = window.clientSize; checkNotNull(source.siteBridge).moveAndResize(RectInt32(0, 0, size.width, size.height)) }
        window.changed.add { _, args -> if (args.didSizeChange) resize() }
        window.destroying.add { _, _ -> source.close() }
        resize(); checkNotNull(source.siteBridge).show(); window.show()
    })
}

private fun appWindowFullScreenDemo() = stack(8.0) {
    children.add(label("The FullScreenPresenter makes an AppWindow cover the entire screen, removing the title bar and system UI to create an immersive experience. To ensure usability, an exit mechanism, such as handling the Escape key or close button, should be included, and fullscreen mode should be used in scenarios like media playback or focused tasks."))
    children.add(Button("Show window (Fullscreen mode)") {
        val content = stack(10.0) { horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
        val window = GalleryWindows.create("AppWindow sample", content).apply { systemBackdrop = MicaBackdrop() }
        val app = checkNotNull(window.appWindow)
        app.resize(SizeInt32(800, 500)); checkNotNull(app.titleBar).preferredTheme = TitleBarTheme.UseDefaultAppMode
        content.children.add(Button("Close") { window.close() })
        content.keyboardAccelerators.add(shortcut(VirtualKey.Escape, VirtualKeyModifiers.None).apply { invoked.add { _, args -> window.close(); args.handled = true } })
        appWindowFullScreenSample(app)
        window.activate()
    })
}

private fun appWindowCompactOverlayDemo() = run {
    var sizeIndex = 0
    val sizes = listOf(CompactOverlaySize.Small, CompactOverlaySize.Medium, CompactOverlaySize.Large)
    val descriptions = listOf(5, 15, 25)
    val description = label("Small: Window size is approximately 5% of the display's work area.").apply { width = 250.0 }
    val sample = stack(8.0) {
        children.add(label("CompactOverlayPresenter (Picture-in-Picture mode) keeps an AppWindow always on top while using minimal screen space. To ensure a good user experience, the window should have a small yet functional size (e.g., for media players or floating tools)."))
        children.add(Button("Show window (Picture-in-Picture mode)") {
            val content = stack(10.0) { horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
            val window = GalleryWindows.create("AppWindow sample", content).apply { systemBackdrop = MicaBackdrop() }
            val app = checkNotNull(window.appWindow)
            app.resize(SizeInt32(800, 500)); checkNotNull(app.titleBar).preferredTheme = TitleBarTheme.UseDefaultAppMode
            content.children.add(Button("Close") { window.close() })
            content.keyboardAccelerators.add(shortcut(VirtualKey.Escape, VirtualKeyModifiers.None).apply { invoked.add { _, args -> window.close(); args.handled = true } })
            appWindowCompactOverlaySample(app, sizes[sizeIndex])
            window.activate()
        })
    }
    val options = stack(8.0) {
        children.add(select("InitialSize", listOf("Small", "Medium", "Large")) {
            sizeIndex = it; description.text = "${listOf("Small", "Medium", "Large")[it]}: Window size is approximately ${descriptions[it]}% of the display's work area."
        })
        children.add(description)
    }
    sample to options
}

@GallerySample(route = "AppWindow", title = "Creating and customizing an AppWindow window.")
internal fun appWindowCreatingSample(content: UIElement, title: String, bounds: RectInt32) = Window().apply {
    this.content = content
    systemBackdrop = MicaBackdrop()
    checkNotNull(appWindow).apply {
        this.title = title
        moveAndResize(bounds)
    }
}

@GallerySample(route = "AppWindow", title = "Centering an AppWindow in the screen's available area.")
internal fun appWindowCenteringSample(app: AppWindow) = run {
    val area = DisplayArea.getFromWindowId(app.id, DisplayAreaFallback.Nearest).workArea
    app.move(PointInt32(area.x + (area.width - app.size.width) / 2, area.y + (area.height - app.size.height) / 2))
}

@GallerySample(route = "AppWindow", title = "AppWindow with OverlappedPresenter.")
internal fun appWindowOverlappedPresenterSample(
    app: AppWindow, alwaysOnTop: Boolean, maximizable: Boolean, minimizable: Boolean,
    resizable: Boolean, hasBorder: Boolean, hasTitleBar: Boolean,
): OverlappedPresenter = run {
    val presenter = OverlappedPresenter.create().apply {
        isAlwaysOnTop = alwaysOnTop
        isMaximizable = maximizable
        isMinimizable = minimizable
        isResizable = resizable
        setBorderAndTitleBar(hasBorder, hasTitleBar)
    }
    app.setPresenter(presenter)
    return@run presenter
}

@GallerySample(route = "AppWindow", title = "Setting minimum and maximum width and height.")
internal fun appWindowConstraintsSample(app: AppWindow, minWidth: Int, minHeight: Int, maxWidth: Int, maxHeight: Int) = run {
    app.setPresenter(OverlappedPresenter.create().apply {
        preferredMinimumWidth = minWidth
        preferredMinimumHeight = minHeight
        preferredMaximumWidth = maxWidth
        preferredMaximumHeight = maxHeight
        isMaximizable = false
    })
}

@GallerySample(route = "AppWindow", title = "Modal window with OverlappedPresenter and AppWindow.")
internal fun appWindowModalSample(ownerId: microsoft.ui.WindowId) =
    AppWindow.create(OverlappedPresenter.createForDialog().apply { isModal = true }, ownerId)

@GallerySample(route = "AppWindow", title = "AppWindow with FullScreenPresenter.")
internal fun appWindowFullScreenSample(app: AppWindow) = run {
    app.setPresenter(AppWindowPresenterKind.FullScreen)
}

@GallerySample(route = "AppWindow", title = "AppWindow with CompactOverlayPresenter.")
internal fun appWindowCompactOverlaySample(app: AppWindow, size: CompactOverlaySize) = run {
    app.setPresenter(CompactOverlayPresenter.create().apply { initialSize = size })
}
