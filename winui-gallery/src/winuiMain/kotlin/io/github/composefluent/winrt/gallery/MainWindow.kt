package io.github.composefluent.winrt.gallery

import io.github.composefluent.winrt.runtime.asWinRT

import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.imaging.BitmapImage
import microsoft.ui.xaml.input.KeyboardAccelerator
import windows.foundation.Uri
import windows.system.VirtualKey
import windows.system.VirtualKeyModifiers

/** Kotlin composition of WinUI Gallery MainWindow; no XAML files or runtime markup. */
internal class MainWindow : winui3package.WindowEx() {
    val root = Grid()
    private val tasks = GalleryPageTasks(root)
    private val navigation = NavigationView()
    private val titleBar = TitleBar()
    private val search = AutoSuggestBox()
    private val host = Frame()
    private val history = mutableListOf<String>()
    private val focusTargets = mutableMapOf<String, String>()
    private val pageIds = GalleryCatalog.pages.mapTo(mutableSetOf()) { it.id }
    private val recent = GalleryPreferences.routes("Recent").filterTo(mutableListOf()) { it in pageIds }
    private val favorites = GalleryPreferences.routes("Favorites").filterTo(mutableSetOf()) { it in pageIds }
    private val menuItems = mutableMapOf<String, NavigationViewItem>()
    private var current = "Home"
    private var selecting = false
    private var theme = when (GalleryPreferences.text("Theme")) {
        "Light" -> ElementTheme.Light
        "Dark" -> ElementTheme.Dark
        else -> ElementTheme.Default
    }

    init {
        title = "Kotlin WinUI Gallery"
        content = root
        systemBackdrop = winui3package.TenMicaBackdrop().apply { bindThemeTo = root }
        contextMenu = winui3package.ModernStandardWindowContextMenu()
        GalleryNavigationHost.navigate = ::navigate
        root.requestedTheme = theme
        GalleryTheme.attach(root, checkNotNull(window))
        navigation.paneDisplayMode = if (GalleryPreferences.flag("TopNavigation")) NavigationViewPaneDisplayMode.Top else NavigationViewPaneDisplayMode.Auto
        ElementSoundPlayer.state = if (GalleryPreferences.flag("Sound")) ElementSoundPlayerState.On else ElementSoundPlayerState.Off
        ElementSoundPlayer.spatialAudioMode = if (GalleryPreferences.flag("SpatialAudio")) ElementSpatialAudioMode.On else ElementSpatialAudioMode.Off
        root.rowDefinitions.add(autoRow())
        root.rowDefinitions.add(starRow())
        titleBar.apply {
            title = "Kotlin WinUI Gallery"
            isPaneToggleButtonVisible = !GalleryPreferences.flag("TopNavigation")
        }
        titleBar.resources["TitleBarContentHorizontalAlignment"] = HorizontalAlignment.Stretch
        titleBar.apply {
            iconSource = ImageIconSource().apply { imageSource = BitmapImage(Uri("ms-appx:///Assets/AppList.png")) }
        }
        search.maxWidth = 580.0
        search.horizontalAlignment = HorizontalAlignment.Stretch
        search.verticalAlignment = VerticalAlignment.Center
        search.keyboardAcceleratorPlacementMode = microsoft.ui.xaml.input.KeyboardAcceleratorPlacementMode.Hidden
        search.apply {
            placeholderText = "Search controls and samples..."
            queryIcon = SymbolIcon(Symbol.Find)
        }
        titleBar.content = search
        search.keyboardAccelerators.add(KeyboardAccelerator().apply {
            key = VirtualKey.F
            modifiers = VirtualKeyModifiers.Control
            invoked.add { _, args -> search.focus(FocusState.Programmatic); args.handled = true }
        })
        root.children.add(titleBar)
        Grid.setRow(navigation, 1)
        navigation.isBackButtonVisible = NavigationViewBackButtonVisible.Collapsed
        navigation.isPaneToggleButtonVisible = false
        navigation.isTabStop = false
        host.horizontalContentAlignment = HorizontalAlignment.Stretch
        host.verticalContentAlignment = VerticalAlignment.Stretch
        host.isNavigationStackEnabled = false
        navigation.content = host
        root.children.add(navigation)
        navigation.menuItems.add(menu(GalleryCatalog.home.id, GalleryCatalog.home.title, GalleryCatalog.home.glyph))
        GalleryCatalog.groups.forEachIndexed { index, group ->
            if (index == 3) {
                navigation.menuItems.add(NavigationViewItemHeader().apply { content = "Controls" })
                navigation.menuItems.add(menu("All", "All", "\uE8A9"))
            }
            val item = menu(group.id, group.title, group.glyph, expandable = true)
            group.pages.forEach { page -> item.menuItems.add(menu(page.id, page.title, page.glyph.ifEmpty { null })) }
            navigation.menuItems.add(item)
        }
        navigation.selectionChanged.add { _, args ->
            if (!selecting) {
                if (args.isSettingsSelected) navigate("Settings")
                else {
                    // NavigationView reports the selected data item and its realized
                    // container separately.  The container is the stable source of the
                    // route tag when nested group items are selected.
                    navigationRoute(args.selectedItemContainer, args.selectedItem)?.let(::navigate)
                }
            }
        }
        // ItemInvoked is raised for nested NavigationViewItem entries even when
        // SelectionChanged only exposes the parent container.  Resolve the route
        // from the invoked container first so every sample leaf is actionable.
        navigation.itemInvoked.add { _, args ->
            if (!selecting) navigationRoute(args.invokedItemContainer, args.invokedItem)?.let(::navigate)
        }
        titleBar.paneToggleRequested.add { _, _ -> navigation.isPaneOpen = !navigation.isPaneOpen }
        titleBar.backRequested.add { _, _ -> if (history.isNotEmpty()) show(history.removeAt(history.lastIndex)) }
        // The accelerator belongs to the page-wide Grid for key routing, but
        // its default placement would show an Alt+Left tooltip on empty content.
        root.keyboardAcceleratorPlacementMode = microsoft.ui.xaml.input.KeyboardAcceleratorPlacementMode.Hidden
        root.keyboardAccelerators.add(KeyboardAccelerator().apply {
            key = VirtualKey.Left; modifiers = VirtualKeyModifiers.Menu
            invoked.add { _, args -> if (history.isNotEmpty()) { show(history.removeAt(history.lastIndex)); args.handled = true } }
        })
        root.pointerPressed.add { _, args ->
            if (args.getCurrentPoint(root).properties?.isXButton1Pressed == true && history.isNotEmpty()) {
                show(history.removeAt(history.lastIndex)); args.handled = true
            }
        }
        search.textChanged.add { _, args ->
            if (args.reason != AutoSuggestionBoxTextChangeReason.UserInput) return@add
            val suggestions = results(search.text).take(12).map { it.title }
            search.itemsSource = suggestions.ifEmpty { listOf("No results found") }
        }
        search.querySubmitted.add { _, args ->
            val page = GalleryCatalog.pages.firstOrNull { it.title == args.chosenSuggestion?.toString() }
            navigate(page?.id ?: "Search:${args.queryText}")
        }
        extendsContentIntoTitleBar = true
        setTitleBar(titleBar)
        fun captionTheme() {
            val dark = root.actualTheme == ElementTheme.Dark
            appWindow?.titleBar?.apply {
                buttonForegroundColor = rgb(if (dark) 0xFFFFFFu else 0x000000u)
                buttonBackgroundColor = windows.ui.Color(0u, 0u, 0u, 0u)
                buttonInactiveBackgroundColor = windows.ui.Color(0u, 0u, 0u, 0u)
            }
        }
        root.actualThemeChanged.add { _, _ -> captionTheme() }
        root.loaded.add { _, _ -> captionTheme(); updateJumpList() }
        show("Home")
    }

    private fun menu(id: String, title: String, icon: String? = null, expandable: Boolean = false) = NavigationViewItem().apply {
        tag = id
        selectsOnInvoked = true
        content = title
        icon?.let { this.icon = glyph(it) }
        named(this, id)
        // Keep route activation on the item itself as a fallback for projected
        // NavigationView event args that expose only the item's content string.
        tapped.add { _, args ->
            if (!selecting) navigate(id)
            if (!expandable) args.handled = true
        }
        this@MainWindow.menuItems[id] = this
    }

    private fun navigationRoute(container: NavigationViewItemBase?, value: Any?): String? {
        fun routeOf(candidate: Any?): String? {
            val element = runCatching { candidate?.asWinRT<FrameworkElement>() }.getOrNull() ?: return null
            val tag = element.tag?.toString()?.takeIf { it.isNotBlank() }
            if (tag != null && menuItems.containsKey(tag)) return tag
            val content = runCatching { element.asWinRT<ContentControl>().content }.getOrNull()
            val contentText = content?.toString()?.takeIf { it.isNotBlank() }
            return menuItems.entries.firstOrNull { it.value.content?.toString() == contentText }?.key
        }

        fun routeFromText(candidate: Any?): String? {
            val text = candidate?.toString()?.takeIf { it.isNotBlank() } ?: return null
            return menuItems.entries.firstOrNull { it.value.content?.toString() == text }?.key
        }

        // For nested entries WinUI can report the leaf's content string while
        // SelectedItemContainer still points at its group. Resolve the leaf
        // string before falling back to the realized parent container.
        val valueRoute = routeOf(value) ?: routeFromText(value)
        val containerRoute = routeOf(container) ?: routeFromText(container)
        if (valueRoute != null) return valueRoute
        if (containerRoute != null) return containerRoute
        return routeFromText(value)
    }

    private fun updateJumpList() {
        tasks.launch {
            // Shell integration is best effort, as in WinUI Gallery's JumpListHelper.
            try { updateGalleryJumpList() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) { println("Kotlin WinUI Gallery: JumpList update failed: ${error.message}") }
        }
    }

    private fun results(query: String) = GalleryCatalog.pages.filter { page ->
        query.isNotBlank() && (page.title.contains(query.trim(), true) || page.tags.any { it.contains(query.trim(), true) })
    }.sortedWith(compareBy({ !it.title.startsWith(query.trim(), true) }, { it.title }))

    private fun canonicalRoute(route: String): String {
        val raw = route.trim().trim('/').ifEmpty { "Home" }
        return when (raw.lowercase()) {
            // The reference catalog displays these titles while the generated
            // factory keeps their stable route ids.  Accept both forms from
            // cards, deep links, and projected NavigationView event payloads.
            "style", "xamlstyle", "xamlstyles" -> "XamlStyles"
            "tooltip", "tool-tip", "tool tip" -> "ToolTip"
            else -> GalleryCatalog.pages.firstOrNull {
                it.id.equals(raw, ignoreCase = true) || it.title.equals(raw, ignoreCase = true)
            }?.id ?: GalleryCatalog.groups.firstOrNull {
                it.id.equals(raw, ignoreCase = true) || it.title.equals(raw, ignoreCase = true)
            }?.id ?: raw
        }
    }

    private fun updateNavigationSelection(route: String) {
        if (route.startsWith("Search:")) return
        val page = GalleryCatalog.pages.firstOrNull { it.id == route }
        val groupItem = page?.group?.let(menuItems::get)
        val selectedItem: Any? = when {
            route == "Settings" -> navigation.settingsItem
            page != null && navigation.paneDisplayMode == NavigationViewPaneDisplayMode.Top -> groupItem
            else -> menuItems[route]
        }
        if (selectedItem == null) return

        selecting = true
        try {
            if (page != null) {
                groupItem?.isExpanded = true
                if (navigation.paneDisplayMode != NavigationViewPaneDisplayMode.Top) navigation.updateLayout()
            }
            navigation.selectedItem = selectedItem
            if (route != "Settings") {
                val item = selectedItem.asWinRT<NavigationViewItem>()
                item.isSelected = true
                item.startBringIntoView()
            }
        } finally {
            selecting = false
        }
    }

    private fun navigate(route: String) {
        val canonical = canonicalRoute(route)
        if (canonical == current) {
            updateNavigationSelection(canonical)
            return
        }
        val previous = current
        try {
            show(canonical)
            if (GalleryCatalog.pages.any { it.id == canonical }) focusTargets[previous] = canonical
            history += previous
            titleBar.isBackButtonVisible = history.isNotEmpty()
        } catch (error: Throwable) {
            println("Kotlin WinUI Gallery: navigation failed for $canonical\n${error.stackTraceToString()}")
            current = previous
        }
    }

    private fun show(route: String) {
        val content = when {
            route == "Home" -> home()
            route == "All" -> index("All controls", GalleryCatalog.pages.sortedBy { it.title })
            route == "Settings" -> settings()
            route.startsWith("Search:") -> index("Search results for \"${route.removePrefix("Search:")}\"", results(route.removePrefix("Search:")))
            else -> GalleryCatalog.groups.firstOrNull { it.id == route }?.let { index(it.title, it.pages) }
                ?: GalleryCatalog.pages.firstOrNull { it.id == route }?.let { page ->
                    detail(page)
                }
                ?: error("Unknown Gallery route: $route")
        }
        current = route
        titleBar.isBackButtonVisible = history.isNotEmpty()
        // The projected Frame navigation overload requires a generated Page
        // type and can leave the old Page visible when the parameter is a
        // code-only UIElement.  Assigning the projected content directly keeps
        // the same single host while making every sample route deterministic.
        host.content = content
        updateNavigationSelection(route)
    }

    private fun card(page: GalleryPageInfo, singleRow: Boolean = false, indented: Boolean = false): Grid = Grid().apply {
        width = 300.0
        height = 96.0
        padding = inset(8.0)
        margin = Thickness(if (indented) 12.0 else 0.0, 0.0, 12.0, if (singleRow) 0.0 else 12.0)
        cornerRadius = corners(8.0)
        // The Gallery item template uses the control fill for its resting tile;
        // GridView supplies the secondary fill for pointer-over and pressed.
        // Keep the state on the projected element as well so direct pointer
        // activation and the GridView item container stay visually identical.
        var pointerOver = false
        var pressed = false
        fun updateInteractionState() {
            background = GalleryTheme.brush(
                when {
                    pressed -> "ControlFillColorSecondaryBrush"
                    pointerOver -> "ControlFillColorSecondaryBrush"
                    else -> "ControlFillColorDefaultBrush"
                }
            )
            borderBrush = GalleryTheme.brush("CardStrokeColorDefaultBrush")
        }
        updateInteractionState()
        borderThickness = inset(1.0)
        tag = page.id
        children.add(Grid().apply {
            // The tile is the interaction surface. Keep its presentational
            // children from taking the pointer target so the route handler on
            // the root remains deterministic for every catalog view.
            isHitTestVisible = false
            columnDefinitions.add(column(56.0, GridUnitType.Pixel))
            columnDefinitions.add(column(1.0, GridUnitType.Star))
            columnSpacing = 16.0
            rowDefinitions.add(autoRow()); rowDefinitions.add(starRow())
            if (page.image.isNotBlank()) children.add(picture(page.image, 32.0).apply {
                Grid.setRowSpan(this, 2)
                verticalAlignment = VerticalAlignment.Top
                margin = Thickness(8.0, 12.0, 16.0, 0.0)
            })
            else if (page.glyph.isNotBlank()) children.add(glyph(page.glyph, 24.0).apply {
                Grid.setRowSpan(this, 2)
                verticalAlignment = VerticalAlignment.Top
                margin = Thickness(12.0, 16.0, 16.0, 0.0)
            })
            children.add(label(page.title).apply {
                Grid.setColumn(this, 1)
                margin = Thickness(0.0, 12.0, 0.0, 0.0); verticalAlignment = VerticalAlignment.Bottom
                fontWeight = windows.ui.text.FontWeight(600u); textWrapping = TextWrapping.NoWrap
                textLineBounds = TextLineBounds.TrimToCapHeight
            })
            children.add(label(page.subtitle, 12.0).apply {
                Grid.setRow(this, 1); Grid.setColumn(this, 1)
                foreground = GalleryTheme.brush("TextFillColorSecondaryBrush"); textTrimming = TextTrimming.WordEllipsis
                ToolTipService.setToolTip(this, page.subtitle)
            })
        })
        if (page.isExperimental) children.add(Border().apply {
            isHitTestVisible = false
            margin = inset(-8.0); padding = Thickness(8.0, 4.0, 8.0, 4.0)
            verticalAlignment = VerticalAlignment.Bottom; cornerRadius = CornerRadius(0.0, 0.0, 7.0, 7.0)
            background = GalleryTheme.brush("SystemFillColorCautionBackgroundBrush")
            child = label("Experimental", 12.0).apply { horizontalAlignment = HorizontalAlignment.Center; foreground = GalleryTheme.brush("SystemFillColorCautionBrush") }
        })
        // GridView's item-click event is not raised consistently for a projected
        // Grid used as the item itself.  Handle the pointer on the card as well so
        // every catalog tile, including recent and related tiles, has a direct route.
        pointerEntered.add { _, _ ->
            pointerOver = true
            updateInteractionState()
        }
        pointerExited.add { _, _ ->
            pointerOver = false
            pressed = false
            updateInteractionState()
        }
        pointerPressed.add { _, args ->
            pressed = true
            updateInteractionState()
            navigate(page.id)
            args.handled = true
        }
        tapped.add { _, args ->
            navigate(page.id)
            args.handled = true
        }
        pointerReleased.add { _, _ ->
            pressed = false
            updateInteractionState()
        }
        named(this, page.title)
    }

    private fun cards(pages: List<GalleryPageInfo>, indented: Boolean = false) = VariableSizedWrapGrid().apply {
        // Keep one pointer surface per tile. The stable SDK supplies this
        // projected wrapping panel even though its experimental WrapPanel is gone.
        orientation = Orientation.Horizontal
        itemWidth = if (indented) 324.0 else 312.0
        itemHeight = 108.0
        maximumRowsOrColumns = 1
        horizontalAlignment = HorizontalAlignment.Stretch
        isHitTestVisible = true
        val ownerRoute = current
        val tiles = pages.map { page -> card(page, indented = indented) }
        tiles.forEach { children.add(it) }
        fun resizeTiles() {
            val available = actualWidth.takeIf { it > 0.0 }
                ?: xamlRoot?.size?.width?.toDouble() ?: 1200.0
            val narrow = available < 640.0
            val cellWidth = if (narrow) available.coerceAtLeast(116.0) else if (indented) 324.0 else 312.0
            val width = if (narrow) cellWidth - 16.0 else 300.0
            itemWidth = cellWidth
            itemHeight = if (narrow) 132.0 else 108.0
            maximumRowsOrColumns = if (narrow) 1 else (available / cellWidth).toInt().coerceAtLeast(1)
            tiles.forEach {
                it.width = width
                it.height = if (narrow) 120.0 else 96.0
                it.margin = Thickness(
                    if (narrow) 0.0 else if (indented) 12.0 else 0.0,
                    0.0,
                    if (narrow) 0.0 else 12.0,
                    12.0,
                )
            }
        }
        sizeChanged.add { _, _ -> resizeTiles() }
        loaded.add { _, _ ->
            resizeTiles()
            tiles.firstOrNull { it.tag == focusTargets[ownerRoute] }?.focus(FocusState.Programmatic)
        }
    }

    private fun index(title: String, pages: List<GalleryPageInfo>): UIElement {
        val allControls = title == "All controls"
        val header = label(title, 28.0).apply {
            microsoft.ui.xaml.automation.AutomationProperties.setHeadingLevel(
                this, microsoft.ui.xaml.automation.peers.AutomationHeadingLevel.Level1,
            )
        }
        val body = ScrollViewer().apply {
            verticalScrollBarVisibility = ScrollBarVisibility.Auto
            horizontalScrollBarVisibility = ScrollBarVisibility.Disabled
            horizontalContentAlignment = HorizontalAlignment.Stretch
            verticalContentAlignment = VerticalAlignment.Top
            content = if (pages.isEmpty()) {
                label("No results found.").apply { margin = Thickness(36.0, 0.0, 36.0, 36.0) }
            } else {
                cards(pages, indented = allControls)
            }
        }
        return Grid().apply {
            rowDefinitions.add(autoRow())
            rowDefinitions.add(starRow())
            children.add(header)
            Grid.setRow(body, 1)
            children.add(body)
            fun updateLayout() {
                val narrow = (xamlRoot?.size?.width ?: actualWidth.toFloat()) < 640f
                header.margin = if (allControls) {
                    if (narrow) Thickness(16.0, 24.0, 16.0, 0.0) else Thickness(36.0, 24.0, 16.0, 0.0)
                } else {
                    if (narrow) Thickness(24.0, 24.0, 16.0, 24.0) else Thickness(36.0, 24.0, 16.0, 24.0)
                }
                body.padding = if (allControls) {
                    if (narrow) Thickness(16.0, 16.0, 16.0, 36.0) else Thickness(24.0, 16.0, 24.0, 36.0)
                } else {
                    if (narrow) Thickness(16.0, 0.0, 16.0, 36.0) else Thickness(36.0, 0.0, 36.0, 0.0)
                }
            }
            sizeChanged.add { _, _ -> updateLayout() }
            loaded.add { _, _ -> updateLayout() }
        }
    }

    @GalleryPage(route = "Home", title = "Home", group = "", order = -1, glyph = "\uE80F")
    private fun home(): UIElement {
        val pageRoot = Grid().apply {
            rowDefinitions.add(autoRow())
            rowDefinitions.add(autoRow())
            rowDefinitions.add(starRow())
        }
        val pagesHost = ContentControl().apply {
            horizontalContentAlignment = HorizontalAlignment.Stretch
            verticalContentAlignment = VerticalAlignment.Top
            margin = Thickness(36.0, 1.0, 36.0, 36.0)
        }
        fun update(favoritesOnly: Boolean) {
            pagesHost.content = stack(12.0) {
                if (favoritesOnly) {
                    if (favorites.isEmpty()) {
                        children.add(stack(0.0) {
                            margin = Thickness(24.0, 36.0, 24.0, 36.0)
                            children.add(picture("ms-appx:///Assets/ControlImages/RatingControl.png", 36.0))
                            children.add(label("No favorites yet").apply {
                                horizontalAlignment = HorizontalAlignment.Center
                                margin = Thickness(0.0, 8.0, 0.0, 8.0)
                                fontWeight = windows.ui.text.FontWeight(600u)
                            })
                            children.add(label("Favorite samples by clicking the star icon on the sample page.").apply {
                                horizontalAlignment = HorizontalAlignment.Center
                                textAlignment = TextAlignment.Center
                                foreground = GalleryTheme.brush("TextFillColorSecondaryBrush")
                            })
                        })
                    } else children.add(cards(GalleryCatalog.pages.filter { it.id in favorites }))
                } else {
                    val recentPages = recent.mapNotNull { id -> GalleryCatalog.pages.firstOrNull { it.id == id } }
                    if (recentPages.isNotEmpty()) {
                        children.add(label("Recently visited", 16.0))
                        children.add(galleryHorizontalScroll(stack(0.0, horizontal = true) {
                            recentPages.forEach { page ->
                                children.add(card(page, singleRow = true))
                            }
                        }).apply { margin = Thickness(-36.0, 0.0, -36.0, 12.0) })
                    }
                    children.add(label("Recently added or updated", 16.0).apply {
                        margin = Thickness(0.0, 24.0, 0.0, 0.0)
                        fontWeight = windows.ui.text.FontWeight(600u)
                    })
                    children.add(cards(GalleryCatalog.pages.filter { it.isNew || it.isUpdated }.sortedBy { it.title }))
                }
            }
        }
        pageRoot.children.add(homeHeader())
        val filterBar = GalleryHomeSelectorBar { favoritesSelected ->
            update(favoritesSelected)
        }.apply {
            margin = Thickness(36.0, 26.0, 0.0, 16.0)
            horizontalAlignment = HorizontalAlignment.Center
            verticalAlignment = VerticalAlignment.Top
        }
        Grid.setRow(filterBar, 1)
        pageRoot.children.add(filterBar)
        Grid.setRow(pagesHost, 2)
        pageRoot.children.add(pagesHost)
        update(false)
        return scroll(pageRoot)
    }

    private fun homeHeader() = Grid().apply {
        children.add(galleryHero())
        children.add(stack(0.0) {
            padding = Thickness(0.0, 48.0, 0.0, 0.0)
            children.add(label("Windows App SDK 2.5", 18.0).apply { margin = Thickness(36.0, 0.0, 0.0, 0.0) })
            children.add(label("Kotlin WinUI Gallery", 40.0).apply {
                margin = Thickness(36.0, 0.0, 0.0, 0.0)
                fontWeight = windows.ui.text.FontWeight(600u)
                microsoft.ui.xaml.automation.AutomationProperties.setHeadingLevel(
                    this, microsoft.ui.xaml.automation.peers.AutomationHeadingLevel.Level1,
                )
            })
            children.add(galleryHorizontalScroll(stack(12.0, horizontal = true) {
                    headerLinks.forEach { link -> children.add(Grid().apply {
                        width = 232.0
                        height = 172.0
                        background = GalleryTheme.brush("AcrylicBackgroundFillColorDefaultBrush")
                        borderBrush = GalleryTheme.brush("SurfaceStrokeColorFlyoutBrush")
                        borderThickness = inset(1.0)
                        cornerRadius = corners(8.0)
                        val button = HyperlinkButton().apply {
                            navigateUri = Uri(link.uri)
                            padding = inset(-1.0)
                            horizontalAlignment = HorizontalAlignment.Stretch
                            verticalAlignment = VerticalAlignment.Stretch
                            background = transparentBrush()
                            borderThickness = inset(1.0)
                            cornerRadius = corners(8.0)
                            horizontalContentAlignment = HorizontalAlignment.Stretch
                            verticalContentAlignment = VerticalAlignment.Stretch
                            var pointerOver = false
                            var pressed = false
                            fun updateButtonBorder() {
                                borderBrush = GalleryTheme.brush(
                                    when {
                                        pressed -> "ControlStrokeColorDefaultBrush"
                                        pointerOver -> "ControlStrokeColorSecondaryBrush"
                                        else -> "ControlStrokeColorDefaultBrush"
                                    }
                                )
                            }
                            content = Grid().apply {
                                horizontalAlignment = HorizontalAlignment.Stretch
                                verticalAlignment = VerticalAlignment.Stretch
                                padding = inset(24.0)
                                rowSpacing = 16.0
                                rowDefinitions.add(RowDefinition().apply { height = GridLength(36.0, GridUnitType.Pixel) })
                                rowDefinitions.add(starRow())
                                children.add((link.image?.let { file -> picture("ms-appx:///Assets/HomeHeaderTiles/$file", 36.0).apply {
                                    if (file.startsWith("Header-Store")) {
                                        fun updateImage() { source = BitmapImage(Uri("ms-appx:///Assets/HomeHeaderTiles/Header-Store.${if (actualTheme == ElementTheme.Dark) "dark" else "light"}.png")) }
                                        loaded.add { _, _ -> updateImage() }; actualThemeChanged.add { _, _ -> updateImage() }
                                    }
                                } } ?: if (link.title == "WinUI on GitHub") Viewbox().apply { width = 36.0; height = 36.0; child = githubIcon() }
                                else glyph("\uE943", 24.0)).apply { horizontalAlignment = HorizontalAlignment.Left })
                                children.add(stack(4.0) {
                                    Grid.setRow(this, 1)
                                    children.add(label(link.title).apply { fontWeight = windows.ui.text.FontWeight(600u); foreground = GalleryTheme.brush("TextFillColorPrimaryBrush") })
                                    children.add(label(link.description, 12.0).apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") })
                                })
                                children.add(glyph("\uE8A7", 14.0).apply {
                                    Grid.setRowSpan(this, 2); margin = inset(-12.0)
                                    horizontalAlignment = HorizontalAlignment.Right; verticalAlignment = VerticalAlignment.Bottom
                                    foreground = GalleryTheme.brush("TextFillColorSecondaryBrush")
                                })
                            }
                            pointerEntered.add { _, _ -> pointerOver = true; updateButtonBorder() }
                            pointerExited.add { _, _ -> pointerOver = false; pressed = false; updateButtonBorder() }
                            pointerPressed.add { _, _ -> pressed = true; updateButtonBorder() }
                            pointerReleased.add { _, _ -> pressed = false; updateButtonBorder() }
                            updateButtonBorder()
                        }
                        children.add(button)
                    }) }
            }).apply { margin = Thickness(0.0, 77.0, 0.0, 0.0) })
        })
    }

    private fun detail(page: GalleryPageInfo): UIElement {
        recent.remove(page.id)
        recent.add(0, page.id)
        while (recent.size > 20) recent.removeAt(recent.lastIndex)
        GalleryPreferences.putRoutes("Recent", recent)
        updateJumpList()
        return Grid().apply {
            rowDefinitions.add(autoRow()); rowDefinitions.add(autoRow()); rowDefinitions.add(starRow())
            if (page.isExperimental) children.add(InfoBar().apply {
                isOpen = true; isClosable = false; severity = InfoBarSeverity.Warning; title = "Experimental"
                message = "This sample uses an experimental Windows App SDK API that may change before release."
            })
            val samplePage = GallerySamples.create(page.id)
            val samples = samplePage.element
            val header = galleryPageHeader(page, page.id in favorites,
                toggleTheme = samplePage::toggleTheme,
                setFavorite = {
                    if (it) favorites.add(page.id) else favorites.remove(page.id)
                    GalleryPreferences.putRoutes("Favorites", favorites)
                    updateJumpList()
                },
            ).apply { Grid.setRow(this, 1); margin = Thickness(36.0, 24.0, 36.0, 0.0) }
            children.add(header)
            val body = stack(12.0) {
                padding = Thickness(36.0, 0.0, 36.0, 36.0)
                if (page.description.isNotEmpty()) children.add(label(page.description).apply {
                    maxWidth = 1064.0; horizontalAlignment = HorizontalAlignment.Left; margin = Thickness(0.0, 4.0, 24.0, 0.0)
                })
                children.add(samples)
            }
            children.add(scroll(body).apply { Grid.setRow(this, 2) })
            sizeChanged.add { _, _ ->
                val narrow = (xamlRoot?.size?.width ?: actualWidth.toFloat()) < 640f
                header.margin = if (narrow) Thickness(16.0, 12.0, 16.0, 0.0) else Thickness(36.0, 24.0, 36.0, 0.0)
                body.padding = if (narrow) Thickness(16.0, 0.0, 16.0, 16.0) else Thickness(36.0, 0.0, 36.0, 36.0)
            }
        }
    }

    private fun settings(): UIElement = gallerySettingsPage(root, theme,
        setTheme = {
            theme = it; GalleryPreferences.put("Theme", it.toString()); root.requestedTheme = it
        },
        setTopNavigation = {
            navigation.paneDisplayMode = if (it) NavigationViewPaneDisplayMode.Top else NavigationViewPaneDisplayMode.Auto
            titleBar.isPaneToggleButtonVisible = !it
            GalleryPreferences.putFlag("TopNavigation", it)
        },
        hasRecents = recent.isNotEmpty(), hasFavorites = favorites.isNotEmpty(),
        clearRecents = { recent.clear(); GalleryPreferences.putRoutes("Recent", recent); updateJumpList() },
        clearFavorites = { favorites.clear(); GalleryPreferences.putRoutes("Favorites", favorites); updateJumpList() },
    )

    private data class HeaderLink(val title: String, val description: String, val uri: String, val image: String? = null)
    private val headerLinks get() = listOf(
        HeaderLink("Getting started", "Get started with WinUI and explore detailed documentation.", "https://aka.ms/winui-getstarted", "Header-WinUI.png"),
        HeaderLink("Design", "Guidelines and toolkits for creating stunning WinUI experiences.", "https://learn.microsoft.com/windows/apps/design/", "Header-WindowsDesign.png"),
        HeaderLink("WinUI on GitHub", "Explore the WinUI source code and repository.", "https://github.com/microsoft/microsoft-ui-xaml"),
        HeaderLink("Community Toolkit", "A collection of helper functions, controls, and app services.", "https://apps.microsoft.com/detail/9nblggh4tlcq", "Header-Toolkit.png"),
        HeaderLink("Code samples", "Find samples that demonstrate specific tasks, features, and APIs.", "https://learn.microsoft.com/windows/apps/get-started/samples"),
        HeaderLink("Partner Center", "Upload your app to the Store.", "https://developer.microsoft.com/windows/", "Header-Store.light.png"),
    )
}
