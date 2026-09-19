package io.github.composefluent.winrt.gallery.scrolling

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.data.CollectionViewSource
import microsoft.ui.xaml.data.ICollectionViewGroup

@GalleryPage(route = "SemanticZoom", title = "SemanticZoom", group = "Scrolling", order = 4)
internal fun semanticZoomPage() = ExamplePage {
    val zoom = semanticZoomSimpleSemanticZoomSample()
    val expanded = GridView().apply { selectionMode = ListViewSelectionMode.None }
    val collapsed = ListView().apply { selectionMode = ListViewSelectionMode.None }
    val groups = GalleryCatalog.groups
    val expandedGroups = groups.map { group ->
            group.pages.map { page -> stack(0.0) {
                tag = group.title
                minWidth = 200.0; margin = Thickness(12.0, 6.0, 12.0, 6.0)
                children.add(label(page.title)); children.add(label(page.subtitle).apply { width = 300.0; horizontalAlignment = HorizontalAlignment.Left })
            } }
    }
    val source = CollectionViewSource().apply { isSourceGrouped = true; this.source = expandedGroups }
    expanded.itemsSource = source.view
    expanded.groupStyleSelector = GallerySemanticGroupStyleSelector()
    val collapsedGroups = groups.map { group -> ListViewItem().apply { content = label(group.title, 20.0); named(this, group.title) } }
    collapsedGroups.forEach { collapsed.items.add(it) }
    zoom.zoomedInView = expanded; zoom.zoomedOutView = collapsed
    zoom.viewChangeStarted.add { _, args ->
        if (args.isSourceZoomedInView) {
            val item = checkNotNull(args.sourceItem).item
            val index = checkNotNull(source.view).collectionGroups.indexOf(item).takeIf { it >= 0 }
                ?: expandedGroups.indexOfFirst { it.contains(item) }
            if (index >= 0) checkNotNull(args.destinationItem).item = collapsedGroups[index]
        } else {
            val index = collapsed.items.indexOf(checkNotNull(args.sourceItem).item)
            if (index >= 0) checkNotNull(args.destinationItem).item = checkNotNull(source.view).collectionGroups[index]
        }
    }
    expanded.gotFocus.add { _, _ -> zoom.startBringIntoView() }; collapsed.gotFocus.add { _, _ -> zoom.startBringIntoView() }
    ScrollViewer.setIsHorizontalScrollChainingEnabled(expanded, false)
    example("A simple SemanticZoom.", zoom)
}

// Ported from WinUI Gallery SemanticZoom (MIT).


class GallerySemanticGroupStyleSelector : GroupStyleSelector() {
    override fun selectGroupStyleCore(group: Any?, level: UInt): GroupStyle {
        // CollectionViewSource supplies an ICollectionViewGroup here. Its
        // `group` property is the grouping key, while `groupItems` contains
        // the actual items used by the selector.
        val items = when (group) {
            null -> emptyList()
            is List<*> -> group
            else -> group.asWinRT<ICollectionViewGroup>().groupItems
        }
        val title = items.firstOrNull()?.asWinRT<FrameworkElement>()?.tag?.toString().orEmpty()
        return GroupStyle().apply {
            headerContainerStyle = Style(GridViewHeaderItem::class).apply {
                setters.add(Setter(ContentControl.contentProperty, title)); setters.add(Setter(Control.fontSizeProperty, 20.0))
            }
        }
    }
}

@GallerySample(route = "SemanticZoom", title = "A simple SemanticZoom.")
internal fun semanticZoomSimpleSemanticZoomSample() = SemanticZoom().apply { height = 500.0 }
