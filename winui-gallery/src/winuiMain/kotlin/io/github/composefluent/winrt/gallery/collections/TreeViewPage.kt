package io.github.composefluent.winrt.gallery.collections

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "TreeView", title = "TreeView", group = "Collections", order = 7)
internal fun treeViewPage() = ExamplePage {
    example("A simple TreeView with drag and drop.", treeViewDragAndDropSample())
    example("A TreeView with multiple selection.", treeViewMultipleSelectionSample())
    example("A TreeView with a data source.", treeViewDataSourceSample())
    example("A TreeView with different item presentations.", treeViewItemPresentationsSample())
}

@GallerySample(route = "TreeView", title = "A simple TreeView with drag and drop.")
internal fun treeViewDragAndDropSample() = TreeView().apply {
    minWidth = 345.0; maxHeight = 400.0; height = 280.0; canDragItems = true; allowDrop = true
    rootNodes.add(TreeViewNode().apply {
        content = "Work Documents"; isExpanded = true
        children.add(TreeViewNode().apply { content = "XYZ Functional Spec"; isExpanded = true })
        children.add(TreeViewNode().apply { content = "Feature Schedule"; isExpanded = true })
    })
    rootNodes.add(TreeViewNode().apply {
        content = "Personal Documents"; isExpanded = true
        children.add(TreeViewNode().apply {
            content = "Home Remodel"; isExpanded = true
            children.add(TreeViewNode().apply { content = "Contractor Contact Info"; isExpanded = true })
            children.add(TreeViewNode().apply { content = "Paint Color Scheme"; isExpanded = true })
        })
    })
}

@GallerySample(route = "TreeView", title = "A TreeView with multiple selection.")
internal fun treeViewMultipleSelectionSample() = TreeView().apply {
    minWidth = 345.0; maxHeight = 400.0; height = 280.0; selectionMode = TreeViewSelectionMode.Multiple
    rootNodes.add(TreeViewNode().apply {
        content = "Work Documents"; isExpanded = true
        children.add(TreeViewNode().apply { content = "XYZ Functional Spec"; isExpanded = true })
        children.add(TreeViewNode().apply { content = "Feature Schedule"; isExpanded = true })
    })
    rootNodes.add(TreeViewNode().apply {
        content = "Personal Documents"; isExpanded = true
        children.add(TreeViewNode().apply {
            content = "Home Remodel"; isExpanded = true
            children.add(TreeViewNode().apply { content = "Contractor Contact Info"; isExpanded = true })
            children.add(TreeViewNode().apply { content = "Paint Color Scheme"; isExpanded = true })
        })
    })
}

@GallerySample(route = "TreeView", title = "A TreeView with a data source.")
internal fun treeViewDataSourceSample() = TreeView().apply {
    minWidth = 345.0; maxHeight = 400.0; height = 200.0
    fun item(title: String, descendants: List<TreeViewItem> = emptyList()) = TreeViewItem().apply { isExpanded = true; itemsSource = descendants; content = title }
    itemsSource = listOf(
        item("Documents", listOf(item("ProjectProposal"), item("BudgetReport"))),
        item("Projects", listOf(item("Project Plan"))),
    )
}

@GallerySample(route = "TreeView", title = "A TreeView with different item presentations.")
internal fun treeViewItemPresentationsSample() = TreeView().apply {
    minWidth = 345.0; maxHeight = 400.0; height = 200.0
    fun item(title: String, descendants: List<TreeViewItem> = emptyList()) = TreeViewItem().apply {
        isExpanded = true; itemsSource = descendants
        content = StackPanel().apply { this.spacing = 10.0; this.orientation = Orientation.Horizontal; children.add(FontIcon().apply { this.glyph = if (descendants.isEmpty()) "\uE8A5" else "\uE8B7"; this.fontSize = 16.0 }); children.add(TextBlock().apply { this.text = title; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }
    }
    itemsSource = listOf(
        item("Documents", listOf(item("ProjectProposal"), item("BudgetReport"))),
        item("Projects", listOf(item("Project Plan"))),
    )
}
