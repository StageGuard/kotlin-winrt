package io.github.composefluent.winrt.gallery.motion

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.input.*
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.media.animation.*
import windows.system.VirtualKey

@GalleryPage(route = "ConnectedAnimation", title = "Connected Animation", group = "Motion", order = 1)
internal fun connectedAnimationPage() = ExamplePage {
    example("Connected animation from a list to a detail page.", connectedAnimationListToDetailSample())
    example("Connected animation between elements on the same page.", connectedAnimationSamePageSample())
    val simple = connectedAnimationSimpleSample()
    example("Simple connected animation.", simple.first, simple.second)
    example("Connected animation with ItemsRepeater.", connectedAnimationItemsRepeaterSample())
}

@GallerySample(route = "ConnectedAnimation", title = "Connected animation from a list to a detail page.")
internal fun connectedAnimationListToDetailSample() = run {
    val frame = Frame().apply { height = 750.0; minWidth = 500.0 }
    val images = mutableMapOf<Int, Image>()
    var pending: ConnectedAnimation? = null
    var selected = 0
    var returning = false
    fun open(index: Int, image: Image) {
        selected = index
        returning = false
        pending = ConnectedAnimationService.getForCurrentView().prepareToAnimate("GalleryCollectionForward", image)
        frame.navigate(Page::class, index + 1, SuppressNavigationTransitionInfo())
    }
    fun item(index: Int) = Grid().apply {
        margin = Thickness(0.0, 12.0, 0.0, 12.0)
        columnDefinitions.add(column(150.0, GridUnitType.Pixel)); columnDefinitions.add(column(1.0, GridUnitType.Star))
        val image = Image().apply { this.width = 150.0; this.height = 150.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(index)) ) }.apply { height = 100.0; stretch = Stretch.UniformToFill }
        images[index] = image
        children.add(image)
        children.add(StackPanel().apply { this.spacing = 6.0; Grid.setColumn(this, 1); margin = Thickness(12.0, 0.0, 0.0, 0.0)
            children.add(TextBlock().apply { this.text = "Item ${index + 1}"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "Views: ${galleryPhotos[index].views}    Likes: ${galleryPhotos[index].likes}"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(TextBlock().apply { this.text = galleryPhotos[index].description; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { maxWidth = 500.0; maxHeight = 40.0; textTrimming = TextTrimming.CharacterEllipsis }) })
    }
    frame.navigated.add { _, args ->
        val page = checkNotNull(args.content).asWinRT<Page>()
        val index = args.parameter.toString().toInt() - 1
        if (index < 0) {
            images.clear()
            page.content = ListView().apply {
                selectionMode = ListViewSelectionMode.None; isItemClickEnabled = true
                repeat(8) { items.add(item(it)) }
                itemClick.add { _, event ->
                    val itemIndex = items.indexOf(event.clickedItem)
                    if (itemIndex >= 0) images[itemIndex]?.let { open(itemIndex, it) }
                }
            }
            page.loaded.add { _, _ ->
                if (returning) {
                    page.updateLayout()
                    images[selected]?.let { image -> pending?.tryStart(image); image.startBringIntoView() }
                    pending = null; returning = false
                }
            }
        } else {
            val image = Image().apply { this.width = 300.0; this.height = 300.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(index)) ) }.apply { height = Double.NaN; maxHeight = 400.0; stretch = Stretch.Uniform; verticalAlignment = VerticalAlignment.Top }
            val details = stack {
                children.add(TextBlock().apply { this.text = "Item ${index + 1}"; this.fontSize = 24.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "Views: ${galleryPhotos[index].views}"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "Likes: ${galleryPhotos[index].likes}"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            }
            page.content = Grid().apply {
                rowDefinitions.add(autoRow()); rowDefinitions.add(starRow())
                children.add(Button().apply { this.content = "Go Back" }.also { galleryButton -> galleryButton.click.add { _, _ -> pending = ConnectedAnimationService.getForCurrentView().prepareToAnimate("GalleryCollectionBackward", image).apply { configuration = DirectConnectedAnimationConfiguration() }
                    returning = true; frame.goBack() } }.apply { horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top })
                children.add(Grid().apply {
                    margin = Thickness(20.0, 52.0, 20.0, 20.0)
                    columnDefinitions.add(column(1.0, GridUnitType.Auto)); columnDefinitions.add(column(1.0, GridUnitType.Star)); children.add(image)
                    Grid.setColumn(details, 1); details.margin = Thickness(20.0, 0.0, 20.0, 0.0); children.add(details)
                })
                children.add(TextBlock().apply { this.text = galleryPhotos[index].description; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { Grid.setRow(this, 1); margin = inset(10.0) })
            }
            page.loaded.add { _, _ -> pending?.tryStart(image, listOf(details)); pending = null }
        }
    }
    frame.navigate(Page::class, 0)
    frame
}

@GallerySample(route = "ConnectedAnimation", title = "Connected animation between elements on the same page.")
internal fun connectedAnimationSamePageSample() = Grid().apply {
    minWidth = 500.0; minHeight = 300.0; maxHeight = 700.0
    val grid = GridView().apply { maxWidth = 1400.0; horizontalAlignment = HorizontalAlignment.Center; isItemClickEnabled = true }
    val smoke = Grid().apply { background = GalleryTheme.brush("SmokeFillColorDefaultBrush"); visibility = Visibility.Collapsed }
    val destination = Grid().apply {
        width = 400.0; height = 320.0; cornerRadius = corners(8.0); borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("CardStrokeColorDefaultBrush")
        rowDefinitions.add(starRow()); rowDefinitions.add(autoRow())
    }
    val image = Image().apply { this.width = 400.0; this.height = 400.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(1)) ) }.apply { height = Double.NaN; stretch = Stretch.UniformToFill }
    val title = TextBlock().apply { this.text = ""; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val cards = List(13) { index -> Grid().apply {
        width = 150.0; height = 110.0; margin = inset(4.0); cornerRadius = corners(4.0); named(this, "Item ${index + 1}")
        children.add(Image().apply { this.width = 150.0; this.height = 150.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(index)) ) }.apply { height = 110.0; stretch = Stretch.UniformToFill })
        children.add(Border().apply { padding = Thickness(8.0, 4.0, 8.0, 4.0); verticalAlignment = VerticalAlignment.Bottom; background = GalleryTheme.brush("AcrylicBackgroundFillColorBaseBrush"); child = TextBlock().apply { this.text = "Item ${index + 1}"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap } })
    } }
    var selected = 0
    destination.children.add(image)
    destination.children.add(Button().apply { this.content = "Close" }.also { galleryButton -> galleryButton.click.add { _, _ -> val animation = ConnectedAnimationService.getForCurrentView().prepareToAnimate("GalleryCardBackward", destination).apply { configuration = DirectConnectedAnimationConfiguration() }
        destination.visibility = Visibility.Collapsed
        grid.scrollIntoView(cards[selected]); grid.updateLayout()
        animation.completed.add { _, _ -> smoke.visibility = Visibility.Collapsed; destination.visibility = Visibility.Visible }
        if (!animation.tryStart(cards[selected])) { smoke.visibility = Visibility.Collapsed; destination.visibility = Visibility.Visible } } }.apply { content = FontIcon().apply { this.glyph = "\uE711"; this.fontSize = 14.0 }; width = 36.0; height = 36.0; margin = inset(8.0); horizontalAlignment = HorizontalAlignment.Right; verticalAlignment = VerticalAlignment.Top; named(this, "Close") })
    destination.children.add(StackPanel().apply { this.spacing = 4.0; Grid.setRow(this, 1); padding = Thickness(16.0, 12.0, 16.0, 12.0); background = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush")
        children.add(title); children.add(TextBlock().apply { this.text = sampleLorem; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { maxLines = 3; foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") }) })
    cards.forEach { grid.items.add(it) }
    grid.itemClick.add { _, args ->
        selected = grid.items.indexOf(args.clickedItem)
        if (selected >= 0) {
            val animation = ConnectedAnimationService.getForCurrentView().prepareToAnimate("GalleryCardForward", cards[selected])
            image.source = Image().apply { this.width = 1.0; this.height = 1.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(selected)) ) }.source; title.text = "Item ${selected + 1}"
            smoke.visibility = Visibility.Visible; smoke.updateLayout(); animation.tryStart(destination)
        }
    }
    smoke.children.add(destination); children.add(grid); children.add(smoke)
}

@GallerySample(route = "ConnectedAnimation", title = "Simple connected animation.")
internal fun connectedAnimationSimpleSample() = run {
    var configuration = 0
    var current = 1
    var source: UIElement? = null
    var pending: ConnectedAnimation? = null
    val frame = Frame().apply {
        height = 500.0; minWidth = 500.0; minHeight = 300.0
        navigated.add { _, args ->
            val page = checkNotNull(args.content).asWinRT<Page>()
            page.content = sampleContent(args.parameter.toString().toInt()) { source = it }
            page.loaded.add { _, _ -> source?.let { pending?.tryStart(it) }; pending = null }
        }
    }
    frame.navigate(Page::class, current)
    val options = stack {
        children.add(Button().apply { this.content = "Navigate" }.also { galleryButton -> galleryButton.click.add { _, _ -> source?.let { element ->
                pending = ConnectedAnimationService.getForCurrentView().prepareToAnimate("GallerySimpleConnection", element).apply {
                    when (configuration) {
                        1 -> this.configuration = GravityConnectedAnimationConfiguration()
                        2 -> this.configuration = DirectConnectedAnimationConfiguration()
                        3 -> this.configuration = BasicConnectedAnimationConfiguration()
                    }
                }
            }
            current = if (current == 1) 2 else 1
            frame.navigate(Page::class, current, SuppressNavigationTransitionInfo()) } }.apply { horizontalAlignment = HorizontalAlignment.Stretch })
        children.add(RadioButtons().apply { this.header = "Configurations"; listOf("Default", "Gravity", "Direct", "Basic").forEach { this.items.add(it) }; this.selectedIndex = 0 }.also { galleryRadioButtons -> galleryRadioButtons.selectionChanged.add { _, _ -> val gallerySelectedIndex = galleryRadioButtons.selectedIndex; if (gallerySelectedIndex in 0 until galleryRadioButtons.items.size) { val it = gallerySelectedIndex; configuration = it } } })
    }
    frame to options
}

@GallerySample(route = "ConnectedAnimation", title = "Connected animation with ItemsRepeater.")
internal fun connectedAnimationItemsRepeaterSample() = run {
    val frame = Frame().apply { height = 500.0; minWidth = 500.0; minHeight = 300.0 }
    val images = mutableMapOf<Int, Image>()
    var pending: ConnectedAnimation? = null
    var selected = 0
    var returning = false
    var repeater: ItemsRepeater? = null
    fun open(index: Int, image: Image) {
        selected = index; returning = false
        pending = ConnectedAnimationService.getForCurrentView().prepareToAnimate("GalleryRepeaterForward", image)
        frame.navigate(Page::class, index + 1, SuppressNavigationTransitionInfo())
    }
    fun item(index: Int) = Grid().apply {
        cornerRadius = corners(4.0); named(this, "Item ${index + 1}")
        val image = Image().apply { this.width = 150.0; this.height = 150.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(index)) ) }.apply { height = 120.0; stretch = Stretch.UniformToFill }
        images[index] = image
        isTabStop = true; useSystemFocusVisuals = true
        children.add(image.apply { width = Double.NaN })
        children.add(Border().apply { padding = Thickness(8.0, 4.0, 8.0, 4.0); verticalAlignment = VerticalAlignment.Bottom; background = GalleryTheme.brush("AcrylicBackgroundFillColorBaseBrush"); child = TextBlock().apply { this.text = "Item ${index + 1}"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap } })
        tapped.add { _, _ -> open(index, image) }
        keyDown.add { _, args -> if (args.key == VirtualKey.Enter || args.key == VirtualKey.Space) { open(index, image); args.handled = true } }
    }
    frame.navigated.add { _, args ->
        val page = checkNotNull(args.content).asWinRT<Page>()
        val index = args.parameter.toString().toInt() - 1
        if (index < 0) {
            images.clear()
            val items = ItemsRepeater().apply {
                layout = UniformGridLayout().apply { minItemWidth = 150.0; minItemHeight = 120.0; minRowSpacing = 8.0; minColumnSpacing = 8.0 }
                itemsSource = (0 until 8).toList(); itemTemplate = GalleryElementFactory { item(it.toString().toInt()) }
                xYFocusKeyboardNavigation = XYFocusKeyboardNavigationMode.Enabled
            }
            repeater = items
            page.content = ScrollViewer().apply { this.content = items; this.verticalScrollBarVisibility = ScrollBarVisibility.Auto; this.horizontalScrollBarVisibility = ScrollBarVisibility.Disabled; this.horizontalContentAlignment = HorizontalAlignment.Stretch; this.verticalContentAlignment = VerticalAlignment.Top }.apply { tabFocusNavigation = KeyboardNavigationMode.Once }
            page.loaded.add { _, _ ->
                if (returning) {
                    repeater?.getOrCreateElement(selected)?.startBringIntoView(); page.updateLayout()
                    images[selected]?.let { image -> pending?.tryStart(image); image.startBringIntoView() }
                    pending = null; returning = false
                }
            }
        } else {
            val image = Image().apply { this.width = 300.0; this.height = 300.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(index)) ) }.apply { height = Double.NaN; maxHeight = 400.0; stretch = Stretch.Uniform; verticalAlignment = VerticalAlignment.Top }
            val details = stack {
                children.add(TextBlock().apply { this.text = "Item ${index + 1}"; this.fontSize = 24.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "Views: ${galleryPhotos[index].views}"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "Likes: ${galleryPhotos[index].likes}"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            }
            page.content = Grid().apply {
                rowDefinitions.add(autoRow()); rowDefinitions.add(starRow())
                children.add(Button().apply { this.content = "Go Back" }.also { galleryButton -> galleryButton.click.add { _, _ -> pending = ConnectedAnimationService.getForCurrentView().prepareToAnimate("GalleryRepeaterBackward", image).apply { configuration = DirectConnectedAnimationConfiguration() }
                    returning = true; frame.goBack() } }.apply { horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top })
                children.add(Grid().apply {
                    margin = Thickness(20.0, 52.0, 20.0, 20.0)
                    columnDefinitions.add(column(1.0, GridUnitType.Auto)); columnDefinitions.add(column(1.0, GridUnitType.Star)); children.add(image)
                    Grid.setColumn(details, 1); details.margin = Thickness(20.0, 0.0, 20.0, 0.0); children.add(details)
                })
                children.add(TextBlock().apply { this.text = galleryPhotos[index].description; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { Grid.setRow(this, 1); margin = inset(10.0) })
            }
            page.loaded.add { _, _ -> pending?.tryStart(image, listOf(details)); pending = null }
        }
    }
    frame.navigate(Page::class, 0)
    stack {
        children.add(TextBlock().apply { this.text = "Unlike ListView and GridView, ItemsRepeater does not have built-in ConnectedAnimation methods. Use ConnectedAnimationService.PrepareToAnimate() directly and manually find the target element in the visual tree. Select an item to navigate with a connected animation."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(frame)
    }
}
