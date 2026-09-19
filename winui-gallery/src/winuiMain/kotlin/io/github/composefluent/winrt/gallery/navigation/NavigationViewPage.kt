package io.github.composefluent.winrt.gallery.navigation

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.animation.SlideNavigationTransitionInfo
import microsoft.ui.xaml.media.animation.SlideNavigationTransitionEffect

@GalleryPage(route = "NavigationView", title = "NavigationView", group = "Navigation", order = 1)
internal fun navigationViewPage() = ExamplePage {
    example("A NavigationView with the default PaneDisplayMode.", navigationViewNavigationViewWithTheDefaultPaneDisplayModeSample())
    example("A NavigationView with PaneDisplayMode Top.", navigationViewNavigationViewWithPaneDisplayModeTopSample1())
    val adaptive = navigationViewNavigationViewThatSwitchesPaneOrientationSample2()
    example("A NavigationView that switches pane orientation.", adaptive)
    example("Tying selection and focus together for tabs.", navigationViewTyingSelectionAndFocusTogetherForTabsSample3())
    val bound = navigationViewNavigationViewWithABoundCollectionSample4()
    example("A NavigationView with a bound collection.", bound)
    val footer = navigationViewNavigationViewWithFooterMenuItemsSample5()
    example("A NavigationView with footer menu items.", footer, choices("Pane position:", listOf("Left mode", "Top mode")) {
        footer.paneDisplayMode = if (it == 0) NavigationViewPaneDisplayMode.Left else NavigationViewPaneDisplayMode.Top
    })
    val hierarchy = navigationViewHierarchicalNavigationViewSample6()
    example("A hierarchical NavigationView.", hierarchy, choices("Pane position:", listOf("Left", "LeftCompact", "Top")) {
        hierarchy.paneDisplayMode = listOf(NavigationViewPaneDisplayMode.Left, NavigationViewPaneDisplayMode.LeftCompact, NavigationViewPaneDisplayMode.Top)[it]
    })
    val paneLink = HyperlinkButton().apply {
        content = "Pane custom content"
        navigateUri = windows.foundation.Uri("https://learn.microsoft.com/windows/apps/design/controls/navigationview")
    }
    val paneFooterContent = stack(4.0) { children.add(label("Pane footer")); children.add(label("Footer content", 12.0)) }
    val options = navigationViewNavigationViewWithCustomizablePropertiesSample7(paneLink, paneFooterContent)
    example("A NavigationView with customizable properties.", options, stack {
        children.add(option("AlwaysShowHeader", true) { options.alwaysShowHeader = it })
        children.add(option("IsSettingsVisible", true) { options.isSettingsVisible = it })
        children.add(option("IsBackButtonVisible", true) { options.isBackButtonVisible = if (it) NavigationViewBackButtonVisible.Visible else NavigationViewBackButtonVisible.Collapsed })
        children.add(option("IsBackEnabled") { options.isBackEnabled = it })
        children.add(option("AutoSuggestBox") { options.autoSuggestBox = if (it) AutoSuggestBox().apply { queryIcon = SymbolIcon(Symbol.Find) } else null })
        children.add(option("SelectionFollowsFocus") { options.selectionFollowsFocus = if (it) NavigationViewSelectionFollowsFocus.Enabled else NavigationViewSelectionFollowsFocus.Disabled })
        children.add(option("Suppress selection of Item2") { checkNotNull(options.menuItems[1]).asWinRT<NavigationViewItem>().selectsOnInvoked = !it })
        children.add(option("PaneCustomContent visible", true) { paneLink.visibility = if (it) Visibility.Visible else Visibility.Collapsed })
        children.add(option("PaneFooter visible", true) { paneFooterContent.visibility = if (it) Visibility.Visible else Visibility.Collapsed })
        children.add(TextBox().apply {
            header = "Header text"; text = "This is Header Text"
            textChanged.add { _, _ -> options.header = text }
        })
        children.add(TextBox().apply {
            header = "Pane title"
            textChanged.add { _, _ -> options.paneTitle = text }
        })
        children.add(choices("Pane position", listOf("Left", "Top")) {
            options.paneDisplayMode = if (it == 0) NavigationViewPaneDisplayMode.Left else NavigationViewPaneDisplayMode.Top
            paneFooterContent.orientation = if (it == 0) Orientation.Vertical else Orientation.Horizontal
        })
    })
}

@GallerySample(route = "NavigationView", title = "A NavigationView with the default PaneDisplayMode.")
internal fun navigationViewNavigationViewWithTheDefaultPaneDisplayModeSample() = NavigationView().apply {
    height = 460.0
    header = "This is Header Text"
    paneDisplayMode = NavigationViewPaneDisplayMode.Auto
    isTabStop = false
    val frame = sampleFrame()
    content = frame
    val symbols = listOf(Symbol.Play, Symbol.Save, Symbol.Refresh, Symbol.Download)
    repeat(4) { index -> menuItems.add(NavigationViewItem().apply { tag = index + 1; content = "Menu Item${index + 1}"; icon = SymbolIcon(symbols[index]) }) }
    selectionChanged.add { _, args ->
        if (args.isSettingsSelected) frame.navigate(Page::class, 0)
        else args.selectedItem?.asWinRT<NavigationViewItem>()?.tag?.toString()?.toIntOrNull()?.let { page ->
            header = "Sample Page $page"
            val transition = args.recommendedNavigationTransitionInfo
            if (transition == null) frame.navigate(Page::class, page) else frame.navigate(Page::class, page, transition)
        }
    }
    backRequested.add { _, _ -> if (frame.canGoBack) frame.goBack() }
    frame.navigated.add { _, _ -> isBackEnabled = frame.canGoBack }
    selectedItem = menuItems[0]
}

@GallerySample(route = "NavigationView", title = "A NavigationView with PaneDisplayMode Top.")
internal fun navigationViewNavigationViewWithPaneDisplayModeTopSample1() = NavigationView().apply {
    height = 460.0
    header = "This is Header Text"
    paneDisplayMode = NavigationViewPaneDisplayMode.Top
    isTabStop = false
    val frame = sampleFrame()
    content = frame
    repeat(4) { index -> menuItems.add(NavigationViewItem().apply { tag = index + 1; content = "Menu Item${index + 1}" }) }
    selectionChanged.add { _, args ->
        if (args.isSettingsSelected) frame.navigate(Page::class, 0)
        else args.selectedItem?.asWinRT<NavigationViewItem>()?.tag?.toString()?.toIntOrNull()?.let { page ->
            header = "Sample Page $page"
            val transition = args.recommendedNavigationTransitionInfo
            if (transition == null) frame.navigate(Page::class, page) else frame.navigate(Page::class, page, transition)
        }
    }
    backRequested.add { _, _ -> if (frame.canGoBack) frame.goBack() }
    frame.navigated.add { _, _ -> isBackEnabled = frame.canGoBack }
    selectedItem = menuItems[0]
}

@GallerySample(route = "NavigationView", title = "Tying selection and focus together for tabs.")
internal fun navigationViewTyingSelectionAndFocusTogetherForTabsSample3() = NavigationView().apply {
    height = 460.0
    header = "This is Header Text"
    paneDisplayMode = NavigationViewPaneDisplayMode.Top
    isTabStop = false
    isBackButtonVisible = NavigationViewBackButtonVisible.Collapsed
    selectionFollowsFocus = NavigationViewSelectionFollowsFocus.Enabled
    val frame = sampleFrame().apply { isNavigationStackEnabled = false }
    content = frame
    repeat(4) { index -> menuItems.add(NavigationViewItem().apply { tag = index + 1; content = "Menu Item${index + 1}" }) }
    selectionChanged.add { _, args ->
        if (args.isSettingsSelected) frame.navigate(Page::class, 0)
        else args.selectedItem?.asWinRT<NavigationViewItem>()?.tag?.toString()?.toIntOrNull()?.let { page ->
            header = "Sample Page $page"
            val transition = args.recommendedNavigationTransitionInfo
            if (transition == null) frame.navigate(Page::class, page) else frame.navigate(Page::class, page, transition)
        }
    }
    backRequested.add { _, _ -> if (frame.canGoBack) frame.goBack() }
    frame.navigated.add { _, _ -> isBackEnabled = frame.canGoBack }
    selectedItem = menuItems[0]
}

@GallerySample(route = "NavigationView", title = "A NavigationView that switches pane orientation.")
internal fun navigationViewNavigationViewThatSwitchesPaneOrientationSample2() = NavigationView().apply {
    height = 460.0
    header = "This is Header Text"
    paneDisplayMode = NavigationViewPaneDisplayMode.Top
    isTabStop = false
    sizeChanged.add { _, _ -> paneDisplayMode = if (actualWidth < 640.0) NavigationViewPaneDisplayMode.LeftMinimal else NavigationViewPaneDisplayMode.Top }
    val frame = sampleFrame()
    content = frame
    repeat(4) { index -> menuItems.add(NavigationViewItem().apply { tag = index + 1; content = "Menu Item${index + 1}" }) }
    selectionChanged.add { _, args ->
        if (args.isSettingsSelected) frame.navigate(Page::class, 0)
        else args.selectedItem?.asWinRT<NavigationViewItem>()?.tag?.toString()?.toIntOrNull()?.let { page ->
            header = "Sample Page $page"
            val transition = args.recommendedNavigationTransitionInfo
            if (transition == null) frame.navigate(Page::class, page) else frame.navigate(Page::class, page, transition)
        }
    }
    backRequested.add { _, _ -> if (frame.canGoBack) frame.goBack() }
    frame.navigated.add { _, _ -> isBackEnabled = frame.canGoBack }
    selectedItem = menuItems[0]
}

@GallerySample(route = "NavigationView", title = "A NavigationView with a bound collection.")
internal fun navigationViewNavigationViewWithABoundCollectionSample4() = NavigationView().apply {
    height = 460.0
    header = "This is Header Text"
    paneDisplayMode = NavigationViewPaneDisplayMode.Auto
    isTabStop = false
    val frame = sampleFrame()
    content = frame
    val categories = listOf(Symbol.Home, Symbol.Keyboard, Symbol.Library, Symbol.Mail).mapIndexed { index, symbol ->
        NavigationViewItem().apply {
            tag = index + 1
            content = "Category ${index + 1}"
            icon = SymbolIcon(symbol)
            ToolTipService.setToolTip(this, "This is category ${index + 1}")
        }
    }
    menuItemsSource = categories
    selectionChanged.add { _, args ->
        if (args.isSettingsSelected) frame.navigate(Page::class, 0)
        else args.selectedItem?.asWinRT<NavigationViewItem>()?.tag?.toString()?.toIntOrNull()?.let { page ->
            header = "Sample Page $page"
            val transition = args.recommendedNavigationTransitionInfo
            if (transition == null) frame.navigate(Page::class, page) else frame.navigate(Page::class, page, transition)
        }
    }
    backRequested.add { _, _ -> if (frame.canGoBack) frame.goBack() }
    frame.navigated.add { _, _ -> isBackEnabled = frame.canGoBack }
    selectedItem = categories.first()
}

@GallerySample(route = "NavigationView", title = "A NavigationView with footer menu items.")
internal fun navigationViewNavigationViewWithFooterMenuItemsSample5() = NavigationView().apply {
    height = 460.0
    header = "This is Header Text"
    paneDisplayMode = NavigationViewPaneDisplayMode.Left
    isTabStop = false
    isSettingsVisible = false
    val frame = sampleFrame()
    content = frame
    listOf("Browse" to Symbol.Library, "Track an Order" to Symbol.Map, "Order History" to Symbol.Tag).forEachIndexed { index, (title, symbol) ->
        menuItems.add(NavigationViewItem().apply { tag = index + 1; content = title; icon = SymbolIcon(symbol) })
    }
    footerMenuItems.add(NavigationViewItem().apply { tag = 4; content = "Account"; icon = SymbolIcon(Symbol.Contact) })
    footerMenuItems.add(NavigationViewItem().apply { tag = 5; content = "Your Cart"; icon = SymbolIcon(Symbol.Shop) })
    footerMenuItems.add(NavigationViewItem().apply { tag = 6; content = "Help"; icon = SymbolIcon(Symbol.Help) })
    selectionChanged.add { _, args ->
        if (args.isSettingsSelected) frame.navigate(Page::class, 0)
        else args.selectedItem?.asWinRT<NavigationViewItem>()?.tag?.toString()?.toIntOrNull()?.let { page ->
            header = "Sample Page $page"
            val transition = args.recommendedNavigationTransitionInfo
            if (transition == null) frame.navigate(Page::class, page) else frame.navigate(Page::class, page, transition)
        }
    }
    backRequested.add { _, _ -> if (frame.canGoBack) frame.goBack() }
    frame.navigated.add { _, _ -> isBackEnabled = frame.canGoBack }
    selectedItem = menuItems[0]
}

@GallerySample(route = "NavigationView", title = "A hierarchical NavigationView.")
internal fun navigationViewHierarchicalNavigationViewSample6() = NavigationView().apply {
    height = 460.0
    header = "This is Header Text"
    paneDisplayMode = NavigationViewPaneDisplayMode.Left
    isTabStop = false
    val frame = sampleFrame()
    content = frame
    fun item(title: String, symbol: Symbol, page: Int) = NavigationViewItem().apply {
        tag = page
        content = title
        icon = SymbolIcon(symbol)
        ToolTipService.setToolTip(this, title)
    }
    menuItems.add(item("Home", Symbol.Home, 1))
    menuItems.add(item("Account", Symbol.Contact, 2).apply {
        menuItems.add(item("Mail", Symbol.Mail, 3))
        menuItems.add(item("Calendar", Symbol.Calendar, 4))
    })
    menuItems.add(item("Document options", Symbol.Page2, 0).apply {
        selectsOnInvoked = false
        menuItems.add(item("Create new", Symbol.NewFolder, 5))
        menuItems.add(item("Upload file", Symbol.OpenLocal, 6))
    })
    selectionChanged.add { _, args ->
        if (args.isSettingsSelected) frame.navigate(Page::class, 0)
        else args.selectedItem?.asWinRT<NavigationViewItem>()?.tag?.toString()?.toIntOrNull()?.let { page ->
            header = "Sample Page $page"
            val transition = args.recommendedNavigationTransitionInfo
            if (transition == null) frame.navigate(Page::class, page) else frame.navigate(Page::class, page, transition)
        }
    }
    backRequested.add { _, _ -> if (frame.canGoBack) frame.goBack() }
    frame.navigated.add { _, _ -> isBackEnabled = frame.canGoBack }
    selectedItem = menuItems[0]
}

@GallerySample(route = "NavigationView", title = "A NavigationView with customizable properties.")
internal fun navigationViewNavigationViewWithCustomizablePropertiesSample7(paneLink: HyperlinkButton, paneFooterContent: StackPanel) = NavigationView().apply {
    height = 460.0
    header = "This is Header Text"
    paneDisplayMode = NavigationViewPaneDisplayMode.Left
    isTabStop = false
    val frame = sampleFrame()
    content = frame
    paneCustomContent = paneLink
    paneFooter = paneFooterContent
    val symbols = listOf(Symbol.Play, Symbol.Save, Symbol.Refresh, Symbol.Download)
    repeat(4) { index -> menuItems.add(NavigationViewItem().apply { tag = index + 1; content = "Menu Item${index + 1}"; icon = SymbolIcon(symbols[index]) }) }
    selectionChanged.add { _, args ->
        if (args.isSettingsSelected) frame.navigate(Page::class, 0)
        else args.selectedItem?.asWinRT<NavigationViewItem>()?.tag?.toString()?.toIntOrNull()?.let { page ->
            header = "Sample Page $page"
            val transition = args.recommendedNavigationTransitionInfo
            if (transition == null) frame.navigate(Page::class, page) else frame.navigate(Page::class, page, transition)
        }
    }
    backRequested.add { _, _ -> if (frame.canGoBack) frame.goBack() }
    frame.navigated.add { _, _ -> isBackEnabled = frame.canGoBack }
    selectedItem = menuItems[0]
}
