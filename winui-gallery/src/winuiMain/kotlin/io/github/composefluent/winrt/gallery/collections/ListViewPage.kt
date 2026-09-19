package io.github.composefluent.winrt.gallery.collections

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import io.github.composefluent.winrt.runtime.await
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.data.CollectionViewSource
import microsoft.ui.xaml.data.ICollectionViewGroup
import microsoft.ui.xaml.media.VisualTreeHelper
import microsoft.ui.xaml.shapes.Ellipse
import windows.applicationmodel.datatransfer.DataPackageOperation
import windows.foundation.Uri
import windows.globalization.Calendar
import windows.globalization.datetimeformatting.DateTimeFormatter
import windows.storage.FileIO
import windows.storage.StorageFile

@GalleryPage(route = "ListView", title = "ListView", group = "Collections", order = 5)
internal fun listViewPage() = stack(0.0) {
    val page = this
    val tasks = GalleryPageTasks(page)
    var initialized = false
    children.add(ProgressRing().apply { isActive = true; width = 32.0; height = 32.0 })
    loaded.add { _, _ ->
        if (!initialized) {
            initialized = true
            tasks.launch {
                val file = StorageFile.getFileFromApplicationUriAsync(Uri("ms-appx:///Assets/SampleMedia/Contacts.txt")).await()
                val lines = FileIO.readLinesAsync(file).await()
                val contacts = lines.chunked(3).filter { it.size == 3 }.map { GalleryContact(it[0], it[1], it[2]) }
                page.children.clear()
                page.children.add(contactExamples(contacts))
            }
        }
    }
}

// Ported from WinUI Gallery Samples/ListView and Assets/SampleMedia/Contacts.txt (MIT).


internal data class GalleryContact(val first: String, val last: String, val company: String) {
    val name get() = "$first $last"
}

internal fun contactTile(contact: GalleryContact) = Grid().apply {
    tag = contact.last.first().uppercase()
    rowDefinitions.add(starRow()); rowDefinitions.add(starRow())
    columnDefinitions.add(column(1.0, GridUnitType.Auto)); columnDefinitions.add(column(1.0, GridUnitType.Star))
    children.add(Ellipse().apply {
        width = 32.0; height = 32.0; margin = inset(6.0)
        fill = GalleryTheme.brush("ControlStrongFillColorDefaultBrush"); Grid.setRowSpan(this, 2)
    })
    children.add(label(contact.name).apply { margin = Thickness(12.0, 6.0, 0.0, 0.0); Grid.setColumn(this, 1) })
    children.add(label(contact.company).apply { margin = Thickness(12.0, 0.0, 0.0, 6.0); Grid.setColumn(this, 1); Grid.setRow(this, 1) })
}

/** Header content uses a native container style instead of a markup DataTemplate. */
class GalleryContactGroupStyleSelector : GroupStyleSelector() {
    override fun selectGroupStyleCore(group: Any?, level: UInt): GroupStyle {
        val items = when (group) {
            null -> emptyList()
            is List<*> -> group
            else -> group.asWinRT<ICollectionViewGroup>().groupItems
        }
        val title = items.firstOrNull()?.asWinRT<FrameworkElement>()?.tag?.toString().orEmpty()
        return GroupStyle().apply {
            headerContainerStyle = Style(ListViewHeaderItem::class).apply {
                setters.add(Setter(ContentControl.contentProperty, title))
                setters.add(Setter(Control.fontSizeProperty, 28.0))
            }
        }
    }
}

internal fun contactExamples(contacts: List<GalleryContact>) = ExamplePage {
    example("A basic ListView with a simple item presentation.", listViewBasicSample(contacts))

    val selection = listViewSelectionSample(contacts)
    example("ListView selection support.", selection,
        select("SelectionMode", listOf("None", "Single", "Multiple", "Extended"), 1) {
            selection.selectionMode = listOf(ListViewSelectionMode.None, ListViewSelectionMode.Single, ListViewSelectionMode.Multiple, ListViewSelectionMode.Extended)[it]
        })

    example("ListViews with drag, drop, and reordering.", listViewDragDropSample(contacts))

    val stickyHeaders = ToggleSwitch().apply { header = "Sticky Headers" }
    val grouped = listViewGroupedHeadersSample(contacts, stickyHeaders)
    example("A ListView with grouped headers.", stack {
        children.add(label("Switch the toggle on the right to enable sticky group headers, which makes the headers stay put at the top of the ListView while scrolling."))
        children.add(grouped)
    }, stickyHeaders)


    val first = TextBox().apply { header = "First name" }
    val last = TextBox().apply { header = "Last name" }
    val company = TextBox().apply { header = "Company" }
    val filtered = listViewFilteringSample(contacts, first, last, company)
    example("Filtering a ListView.", filtered, stack { width = 200.0; children.add(label("Filter by...")); children.add(first); children.add(last); children.add(company) })

    val messaging = ListViewMessagingState()
    example("A ListView for messaging and data logging.", listViewMessagingSample(messaging), stack {
        children.add(Button("Send Message") { messaging.addMessage(true) })
        children.add(Button("Receive Message") { messaging.addMessage(false) })
    })


    example("A ListView with images.", listViewImagesSample())
    example("ListView context menus.", listViewListViewContextMenusSample7(contacts))


    val save = Button("Save position")
    val restore = Button("Restore position").apply { isEnabled = false }
    val position = listViewScrollPositionSample(contacts, save, restore)
    example("Restore a ListView scroll position.", position, stack {
        children.add(save)
        children.add(restore)
    })
    val many = List(10) { contacts }.flatten().mapIndexed { index, contact -> "${index + 1}. ${contact.name}" }

    val index = NumberBox().apply { header = "Index"; minimum = 0.0; maximum = (many.size - 1).toDouble(); value = 0.0; spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline }
    var alignment = ScrollIntoViewAlignment.Default
    val scrollButton = Button("Scroll into view")
    val target = listViewScrollItemSample(contacts, index, { alignment }, scrollButton)
    example("Scroll an item into view.", target, stack {
        children.add(index)
        children.add(select("Alignment", listOf("Default", "Leading")) { alignment = if (it == 0) ScrollIntoViewAlignment.Default else ScrollIntoViewAlignment.Leading })
        children.add(scrollButton)
    })









}

@GallerySample(route = "ListView", title = "ListView context menus.")
internal fun listViewListViewContextMenusSample7(contacts: List<GalleryContact>) = ListView().apply listView@ {
    width = 400.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
    borderThickness = inset(1.0)
    borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
    contacts.forEach { contact ->
        val item = ListViewItem().apply { content = contactTile(contact) }
        item.contextFlyout = MenuFlyout().apply { items.add(menuItem("Delete") { this@listView.items.remove(item) }) }
        items.add(item)
    }
}

@GallerySample(route = "ListView", title = "A basic ListView with a simple item presentation.")
internal fun listViewBasicSample(contacts: List<GalleryContact>) = ListView().apply {
    width = 400.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
    borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
    contacts.forEach { contact -> items.add(TextBlock().apply { this.text = contact.name; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(0.0, 5.0, 0.0, 5.0) }) }
}

@GallerySample(route = "ListView", title = "ListView selection support.")
internal fun listViewSelectionSample(contacts: List<GalleryContact>) = ListView().apply {
    width = 400.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
    borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
    contacts.forEach { items.add(contactTile(it)) }
}

@GallerySample(route = "ListView", title = "ListViews with drag, drop, and reordering.")
internal fun listViewDragDropSample(contacts: List<GalleryContact>) = StackPanel().apply { this.spacing = 16.0; this.orientation = Orientation.Horizontal; val left = ListView().apply {
        width = 300.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
        borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
        contacts.forEach { items.add(contactTile(it)) }
    }
    val rightContacts = listOf(
        GalleryContact("John", "Doe", "ABC Printers"),
        GalleryContact("Jane", "Doe", "XYZ Refrigerators"),
        GalleryContact("Santa", "Claus", "North Pole Toy Factory Inc."),
    )
    val right = ListView().apply {
        width = 300.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
        borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
        rightContacts.forEach { items.add(contactTile(it)) }
    }
    var dragSource: ListView? = null
    var moving = emptyList<Any?>()
    listOf(left, right).forEach { target ->
        target.canDragItems = true
        target.canReorderItems = true
        target.allowDrop = true
        target.selectionMode = ListViewSelectionMode.Multiple
        target.dragItemsStarting.add { _, args ->
            dragSource = target
            moving = args.items.toList()
            checkNotNull(args.data).requestedOperation = DataPackageOperation.Move
        }
        target.dragItemsCompleted.add { _, _ -> dragSource = null; moving = emptyList() }
        target.dragOver.add { _, args -> if (dragSource != null) args.acceptedOperation = DataPackageOperation.Move }
        target.dragEnter.add { _, args -> checkNotNull(args.dragUIOverride).isGlyphVisible = false }
        target.drop.add { _, args ->
            val source = dragSource
            if (source != null && source != target) {
                var insertion = target.items.size
                for (index in 0 until target.items.size) {
                    val container = target.containerFromIndex(index)?.asWinRT<FrameworkElement>() ?: continue
                    if (args.getPosition(container).y < container.actualHeight / 2) { insertion = index; break }
                }
                moving.forEach { item -> source.items.remove(item); target.items.add(insertion++, item) }
                args.acceptedOperation = DataPackageOperation.Move
                args.handled = true
            }
        }
    }
    children.add(left)
    children.add(right) }

@GallerySample(route = "ListView", title = "A ListView with grouped headers.")
internal fun listViewGroupedHeadersSample(contacts: List<GalleryContact>, stickyHeaders: ToggleSwitch) = ListView().apply {
    width = 400.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
    borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
    val source = CollectionViewSource().apply {
        isSourceGrouped = true
        this.source = contacts.groupBy { it.last.first().uppercase() }
            .entries.sortedBy { it.key }.map { (_, group) -> group.map(::contactTile) }
    }
    itemsSource = source.view
    groupStyleSelector = GalleryContactGroupStyleSelector()
    loaded.add { _, _ -> itemsPanelRoot?.asWinRT<ItemsStackPanel>()?.areStickyGroupHeadersEnabled = false }
    stickyHeaders.toggled.add { _, _ -> itemsPanelRoot?.asWinRT<ItemsStackPanel>()?.areStickyGroupHeadersEnabled = stickyHeaders.isOn }
}

@GallerySample(route = "ListView", title = "Filtering a ListView.")
internal fun listViewFilteringSample(
    contacts: List<GalleryContact>,
    first: TextBox,
    last: TextBox,
    company: TextBox,
) = ListView().apply {
    width = 400.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
    borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
    fun filter() {
        val matches = contacts.filter {
            it.first.contains(first.text, true) && it.last.contains(last.text, true) && it.company.contains(company.text, true)
        }
        items.clear()
        matches.forEach { items.add(contactTile(it)) }
        announce(this, "Found ${matches.size} contacts", "ContactListViewFilteredActivityId")
    }
    contacts.forEach { items.add(contactTile(it)) }
    listOf(first, last, company).forEach { it.textChanged.add { _, _ -> filter() } }
}

internal class ListViewMessagingState {
    var addMessage: (Boolean) -> Unit = {}
}

@GallerySample(route = "ListView", title = "A ListView for messaging and data logging.")
internal fun listViewMessagingSample(state: ListViewMessagingState) = stack {
    val messages = ListView().apply {
        width = Double.NaN; minWidth = 400.0; selectionMode = ListViewSelectionMode.None
        loaded.add { _, _ -> itemsPanelRoot?.asWinRT<ItemsStackPanel>()?.apply {
            verticalAlignment = VerticalAlignment.Bottom
            itemsUpdatingScrollMode = ItemsUpdatingScrollMode.KeepLastItemInView
        } }
    }
    var messageNumber = 0
    fun addMessage(outgoing: Boolean) {
        messages.items.add(ListViewItem().apply {
            horizontalContentAlignment = HorizontalAlignment.Stretch
            content = StackPanel().apply { this.spacing = 0.0; width = 350.0; minHeight = 75.0; padding = Thickness(10.0, 0.0, 0.0, 10.0); margin = inset(4.0)
                horizontalAlignment = if (outgoing) HorizontalAlignment.Right else HorizontalAlignment.Left
                background = GalleryTheme.brush("SystemColorHighlightColor"); cornerRadius = corners(4.0)
                children.add(TextBlock().apply { this.text = "Message ${++messageNumber}"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply {
                    padding = Thickness(0.0, 10.0, 0.0, 0.0)
                    foreground = GalleryTheme.brush("SystemColorHighlightTextColor")
                })
                children.add(TextBlock().apply { this.text = DateTimeFormatter("shortdate shorttime").format(Calendar().getDateTime()); this.fontSize = 15.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply {
                    padding = Thickness(0.0, 0.0, 0.0, 10.0)
                    foreground = GalleryTheme.brush("SystemColorHighlightTextColor")
                }) }
        })
        messages.scrollIntoView(messages.items.last())
    }
    addMessage(true)
    state.addMessage = ::addMessage
    children.add(TextBlock().apply { this.text = "This ListView is inverted to grow from the bottom up. It's a good way to display logs or messages, with most recent at the bottom."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    children.add(messages)
}

@GallerySample(route = "ListView", title = "A ListView with images.")
internal fun listViewImagesSample() = ListView().apply {
    width = 400.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
    borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
    repeat(8) { index -> items.add(StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; width = 280.0
        children.add(Image().apply { this.width = 100.0; this.height = 100.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(galleryPhotos[index].image) ) })
        children.add(stack {
            children.add(TextBlock().apply { this.text = "Item ${index + 1}"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(TextBlock().apply { this.text = "${galleryPhotos[index].views} Views"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(TextBlock().apply { this.text = "${galleryPhotos[index].likes} Likes"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        }) }.apply { tag = index; named(this, "Item ${index + 1}") }) }
}

@GallerySample(route = "ListView", title = "Restore a ListView scroll position.")
internal fun listViewScrollPositionSample(contacts: List<GalleryContact>, saveButton: Button, restoreButton: Button) = ListView().apply {
    width = 400.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
    borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
    itemsSource = contacts.mapIndexed { index, contact -> "${index + 1}. ${contact.name}" }
    var savedOffset: Double? = null
    fun findScroll(element: DependencyObject): ScrollViewer? {
        if (element is ScrollViewer) return element
        for (index in 0 until VisualTreeHelper.getChildrenCount(element)) {
            findScroll(VisualTreeHelper.getChild(element, index))?.let { return it }
        }
        return null
    }
    saveButton.click.add { _, _ ->
        savedOffset = findScroll(this)?.verticalOffset
        restoreButton.isEnabled = savedOffset != null
    }
    restoreButton.click.add { _, _ -> savedOffset?.let { findScroll(this)?.changeView(null, it, null, true) } }
}

@GallerySample(route = "ListView", title = "Scroll an item into view.")
internal fun listViewScrollItemSample(
    contacts: List<GalleryContact>,
    index: NumberBox,
    alignment: () -> ScrollIntoViewAlignment,
    scrollButton: Button,
) = ListView().apply {
    width = 400.0; height = 400.0; horizontalAlignment = HorizontalAlignment.Left
    borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush")
    val many = List(10) { contacts }.flatten().mapIndexed { itemIndex, contact -> "${itemIndex + 1}. ${contact.name}" }
    itemsSource = many
    scrollButton.click.add { _, _ ->
        if (index.value.isFinite()) many.getOrNull(index.value.toInt())?.let { scrollIntoView(it, alignment()) }
    }
}
