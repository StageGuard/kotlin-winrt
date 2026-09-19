package io.github.composefluent.winrt.gallery.navigation

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.input.KeyboardAccelerator
import windows.system.VirtualKey
import windows.system.VirtualKeyModifiers

@GalleryPage(route = "TabView", title = "TabView", group = "Navigation", order = 4)
internal fun tabViewPage() = ExamplePage {
    example("A TabView with support for adding and closing tabs.", tabViewTabViewWithSupportForAddingAndClosingTabsSample())
    example("A TabView with TabViewItems defined in code.", tabViewTabViewWithTabViewItemsDefinedInCodeSample1())
    val bound = tabViewTabViewBoundToACollectionSample2()
    example("A TabView bound to a collection.", bound)
    example("A TabView with keyboard support.", stack {
        children.add(label("- Ctrl+T opens a new tab\n- Ctrl+W closes the selected tab\n- Ctrl+1 to Ctrl+8 selects that number tab\n- Ctrl+9 selects the last tab (regardless of the number of tabs)"))
        children.add(tabViewKeyboardSample())
    })
    example("A TabView with header and footer content.", tabViewTabViewWithHeaderAndFooterContentSample4())
    val width = tabViewTabWidthsCanBeSizedToContentEqualOrCompactSample5()
    listOf("Home", "Tab 2 Has Longer Text", "Third Tab").forEachIndexed { index, title -> checkNotNull(width.tabItems[index]).asWinRT<TabViewItem>().apply { header = title; isClosable = false } }
    example("Tab widths can be sized to content, equal, or compact.", width, select("TabWidthBehavior", listOf("SizeToContent", "Equal", "Compact")) {
        width.tabWidthMode = listOf(TabViewWidthMode.SizeToContent, TabViewWidthMode.Equal, TabViewWidthMode.Compact)[it]
    })
    val close = tabViewPersistentOrHoverOnlyCloseButtonSample6()
    example("A persistent or hover-only close button.", close, select("TabViewItem CloseButtonOverlayMode", listOf("Auto", "Always", "OnHover"), 1) {
        close.closeButtonOverlayMode = listOf(TabViewCloseButtonOverlayMode.Auto, TabViewCloseButtonOverlayMode.Always, TabViewCloseButtonOverlayMode.OnPointerOver)[it]
    })
    example("A TabView with color icons.", tabViewTabViewWithColorIconsSample7())
    example("An accent-colored tab strip.", tabViewAccentColoredTabStripSample8())
    example("A complete TabView windowing sample.", tabViewCompleteTabViewWindowingSampleSample9())

}

@GallerySample(route = "TabView", title = "A TabView with keyboard support.")
internal fun tabViewKeyboardSample() = TabView().apply {
    minHeight = 475.0
    margin = inset(-12.0)
    repeat(3) { index ->
        tabItems.add(TabViewItem().apply {
            header = "Document $index"
            iconSource = SymbolIconSource().apply { symbol = Symbol.Document }
            content = sampleContent(index % 3 + 1)
        })
    }
    selectedIndex = 0
    val tabs = this
    fun accelerator(key: VirtualKey, action: () -> Unit) {
        keyboardAccelerators.add(KeyboardAccelerator().apply {
            this.key = key; modifiers = VirtualKeyModifiers.Control
            invoked.add { _, args -> action(); args.handled = true }
        })
    }
    accelerator(VirtualKey.T) { tabs.tabItems.add(TabViewItem().apply { header = "Document ${tabs.tabItems.size}"; content = sampleContent(1) }) }
    accelerator(VirtualKey.W) { tabs.selectedItem?.asWinRT<TabViewItem>()?.takeIf { it.isClosable }?.let { tabs.tabItems.remove(it) } }
    listOf(VirtualKey.Number1, VirtualKey.Number2, VirtualKey.Number3, VirtualKey.Number4, VirtualKey.Number5,
        VirtualKey.Number6, VirtualKey.Number7, VirtualKey.Number8, VirtualKey.Number9).forEachIndexed { index, key ->
        accelerator(key) { val selected = if (index == 8) tabs.tabItems.size - 1 else index; if (selected in 0 until tabs.tabItems.size) tabs.selectedIndex = selected }
    }
    bringIntoViewRequested.add { _, args -> args.handled = true }
}

@GallerySample(route = "TabView", title = "A TabView with support for adding and closing tabs.")
internal fun tabViewTabViewWithSupportForAddingAndClosingTabsSample() = TabView().apply {
    minHeight = 475.0
    margin = inset(-12.0)
    repeat(3) { index ->
        tabItems.add(TabViewItem().apply {
            header = "Document $index"
            iconSource = SymbolIconSource().apply { symbol = Symbol.Document }
            content = sampleContent(index % 3 + 1)
        })
    }
    selectedIndex = 0
    addTabButtonClick.add { _, _ ->
        tabItems.add(TabViewItem().apply {
            header = "Document ${tabItems.size}"
            iconSource = SymbolIconSource().apply { symbol = Symbol.Document }
            content = sampleContent(tabItems.size % 3 + 1)
        })
    }
    tabCloseRequested.add { _, args -> tabItems.remove(args.tab) }
    bringIntoViewRequested.add { _, args -> args.handled = true }
}

@GallerySample(route = "TabView", title = "A TabView with TabViewItems defined in code.")
internal fun tabViewTabViewWithTabViewItemsDefinedInCodeSample1() = TabView().apply {
    minHeight = 475.0
    margin = inset(-12.0)
    tabItems.add(TabViewItem().apply { header = "Home"; content = sampleContent(1) })
    tabItems.add(TabViewItem().apply { header = "Tab 2"; content = sampleContent(2) })
    tabItems.add(TabViewItem().apply { header = "Tab 3"; content = sampleContent(3) })
    selectedIndex = 0
    addTabButtonClick.add { _, _ -> tabItems.add(TabViewItem().apply { header = "Tab ${tabItems.size + 1}"; content = sampleContent(tabItems.size % 3 + 1) }) }
    tabCloseRequested.add { _, args -> tabItems.remove(args.tab) }
    bringIntoViewRequested.add { _, args -> args.handled = true }
}

@GallerySample(route = "TabView", title = "A TabView bound to a collection.")
internal fun tabViewTabViewBoundToACollectionSample2() = TabView().apply {
    minHeight = 475.0
    margin = inset(-12.0)
    val source = mutableListOf<TabViewItem>()
    repeat(3) { index ->
        source.add(TabViewItem().apply {
            header = "MyData Doc $index"
            iconSource = SymbolIconSource().apply { symbol = Symbol.Document }
            content = sampleContent(index % 3 + 1)
        })
    }
    tabItemsSource = source.toList()
    selectedIndex = 0
    addTabButtonClick.add { _, _ ->
        source.add(TabViewItem().apply {
            header = "MyData Doc ${source.size}"
            iconSource = SymbolIconSource().apply { symbol = Symbol.Document }
            content = sampleContent(source.size % 3 + 1)
        })
        tabItemsSource = source.toList()
    }
    tabCloseRequested.add { _, args ->
        args.tab?.let { item -> source.removeAll { it == item } }
        tabItemsSource = source.toList()
    }
    bringIntoViewRequested.add { _, args -> args.handled = true }
}

@GallerySample(route = "TabView", title = "A TabView with header and footer content.")
internal fun tabViewTabViewWithHeaderAndFooterContentSample4() = TabView().apply {
        minHeight = 475.0
        margin = inset(-12.0)
        repeat(3) { index -> tabItems.add(TabViewItem().apply { header = "Document $index"; iconSource = SymbolIconSource().apply { symbol = Symbol.Document }; content = sampleContent(index % 3 + 1) }) }
        selectedIndex = 0
        addTabButtonClick.add { _, _ -> tabItems.add(TabViewItem().apply { header = "Document ${tabItems.size}"; iconSource = SymbolIconSource().apply { symbol = Symbol.Document }; content = sampleContent(tabItems.size % 3 + 1) }) }
        tabCloseRequested.add { _, args -> tabItems.remove(args.tab) }
        bringIntoViewRequested.add { _, args -> args.handled = true }
        tabWidthMode = TabViewWidthMode.SizeToContent
        tabStripHeader = TextBlock().apply { this.text = "TabStripHeader Content"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(8.0) }
        tabStripFooter = TextBlock().apply { this.text = "TabStripFooter Content"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(6.0) }
    }

@GallerySample(route = "TabView", title = "A TabView with color icons.")
internal fun tabViewTabViewWithColorIconsSample7() = TabView().apply {
        isAddTabButtonVisible = false; tabWidthMode = TabViewWidthMode.SizeToContent
        listOf("CMD Prompt" to "cmd.png", "PowerShell" to "powershell.png", "Windows Subsystem for Linux" to "linux.png").forEach { (title, image) ->
            tabItems.add(TabViewItem().apply {
                header = title; isClosable = false
                iconSource = BitmapIconSource().apply { showAsMonochrome = false; uriSource = windows.foundation.Uri("ms-appx:///Assets/SampleMedia/$image") }
            })
        }
    }

@GallerySample(route = "TabView", title = "An accent-colored tab strip.")
internal fun tabViewAccentColoredTabStripSample8() = TabView().apply {
    minHeight = 475.0
    margin = inset(-12.0)
    repeat(3) { index -> tabItems.add(TabViewItem().apply { header = "Document $index"; iconSource = SymbolIconSource().apply { symbol = Symbol.Document }; content = sampleContent(index % 3 + 1) }) }
    selectedIndex = 0
    addTabButtonClick.add { _, _ -> tabItems.add(TabViewItem().apply { header = "Document ${tabItems.size}"; iconSource = SymbolIconSource().apply { symbol = Symbol.Document }; content = sampleContent(tabItems.size % 3 + 1) }) }
    tabCloseRequested.add { _, args -> tabItems.remove(args.tab) }
    bringIntoViewRequested.add { _, args -> args.handled = true }
    resources["TabViewBackground"] = GalleryTheme.brush("AccentFillColorDefaultBrush")
}

@GallerySample(route = "TabView", title = "A complete TabView windowing sample.")
internal fun tabViewCompleteTabViewWindowingSampleSample9() = Button().apply { this.content = "Click here to launch the sample" }.also { galleryButton -> galleryButton.click.add { _, _ -> GalleryTabWindows.open() } }

@GallerySample(route = "TabView", title = "Tab widths can be sized to content, equal, or compact.")
internal fun tabViewTabWidthsCanBeSizedToContentEqualOrCompactSample5() = TabView().apply {
    minHeight = 475.0
    margin = inset(-12.0)
    repeat(3) { index -> tabItems.add(TabViewItem().apply { header = "Document $index"; iconSource = SymbolIconSource().apply { symbol = Symbol.Document }; content = sampleContent(index % 3 + 1) }) }
    selectedIndex = 0
    isAddTabButtonVisible = false
    tabCloseRequested.add { _, args -> tabItems.remove(args.tab) }
    bringIntoViewRequested.add { _, args -> args.handled = true }
    tabWidthMode = TabViewWidthMode.SizeToContent
}

@GallerySample(route = "TabView", title = "A persistent or hover-only close button.")
internal fun tabViewPersistentOrHoverOnlyCloseButtonSample6() = TabView().apply {
    minHeight = 475.0
    margin = inset(-12.0)
    repeat(3) { index -> tabItems.add(TabViewItem().apply { header = "Document $index"; iconSource = SymbolIconSource().apply { symbol = Symbol.Document }; content = sampleContent(index % 3 + 1) }) }
    selectedIndex = 0
    isAddTabButtonVisible = false
    closeButtonOverlayMode = TabViewCloseButtonOverlayMode.Always
    tabCloseRequested.add { _, args -> tabItems.remove(args.tab) }
    bringIntoViewRequested.add { _, args -> args.handled = true }
}

/** Native TabView tear-out protocol; tab instances move between windows without recreating content. */
private object GalleryTabWindows {
    private val hosts = mutableMapOf<TabView, Window>()
    private fun tab(title: String) = TabViewItem().apply {
        header = title
        iconSource = SymbolIconSource().apply { symbol = Symbol.Placeholder }
        content = stack {
            padding = inset(24.0)
            children.add(label(title, 28.0))
            children.add(TextBox().apply { header = "Tab content"; placeholderText = "Text stays with this tab when you move it" })
        }
    }

    fun open() { create(true).second.activate() }

    private fun create(demo: Boolean): Pair<TabView, Window> {
        val drag = Grid().apply { minWidth = 188.0; minHeight = 48.0; background = brush(0x000000u).apply { opacity = 0.0 } }
        val tabs = TabView().apply { canTearOutTabs = true; tabStripFooter = drag }
        val window = GalleryWindows.create("TabView", tabs)
        hosts[tabs] = window
        window.systemBackdrop = microsoft.ui.xaml.media.MicaBackdrop()
        window.extendsContentIntoTitleBar = true
        window.setTitleBar(drag)
        window.appWindow?.resize(windows.graphics.SizeInt32(800, 600))
        tabs.loaded.add { _, _ ->
            window.appWindow?.presenter?.asWinRT<microsoft.ui.windowing.OverlappedPresenter>()?.apply {
                preferredMinimumWidth = 500
                preferredMinimumHeight = 300
            }
        }
        window.closed.add { _, _ -> hosts.remove(tabs) }
        var pending: Pair<TabView, Window>? = null
        if (demo) repeat(3) { tabs.tabItems.add(tab("Item $it")) }
        tabs.selectedIndex = 0
        tabs.addTabButtonClick.add { _, _ -> tabs.tabItems.add(tab("New Item")) }
        tabs.tabCloseRequested.add { _, args -> tabs.tabItems.remove(args.tab); closeIfEmpty(tabs) }
        tabs.tabTearOutWindowRequested.add { _, args ->
            val target = create(false)
            pending = target
            args.newWindowId = checkNotNull(target.second.appWindow).id
        }
        tabs.tabTearOutRequested.add { _, args ->
            val destination = pending?.first ?: return@add
            args.tabs.forEach { value ->
                val item = value.asWinRT<TabViewItem>()
                tabs.tabItems.remove(item)
                destination.tabItems.add(item)
            }
            destination.selectedIndex = 0
            pending = null
            closeIfEmpty(tabs)
        }
        tabs.externalTornOutTabsDropping.add { _, args -> args.allowDrop = true }
        tabs.externalTornOutTabsDropped.add { _, args ->
            args.tabs.forEachIndexed { offset, value ->
                val item = value.asWinRT<TabViewItem>()
                val source = hosts.keys.firstOrNull { it.tabItems.contains(item) }
                source?.tabItems?.remove(item)
                tabs.tabItems.add((args.dropIndex + offset).coerceIn(0, tabs.tabItems.size), item)
                if (source != null && source != tabs) closeIfEmpty(source)
            }
        }
        return tabs to window
    }

    private fun closeIfEmpty(tabs: TabView) { if (tabs.tabItems.isEmpty()) hosts[tabs]?.close() }
}
