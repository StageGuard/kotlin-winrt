package io.github.composefluent.winrt.gallery.collections

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.Stretch

@GalleryPage(route = "ItemsView", title = "ItemsView", group = "Collections", order = 3)
internal fun itemsViewPage() = ExamplePage {
    val basic = itemsViewBasicItemsViewSample()
    val basicOutput = label("")
    basic.itemInvoked.add { _, args -> basicOutput.text = "You invoked Item ${args.invokedItem.toString().toInt() + 1}." }
    example("A basic ItemsView.", basic, output = basicOutput)

    val lined = LinedFlowLayout().apply {
        itemsStretch = LinedFlowLayoutItemsStretch.Fill
        lineHeight = 160.0; lineSpacing = 5.0; minItemSpacing = 5.0
    }
    val uniform = UniformGridLayout().apply {
        minColumnSpacing = 5.0; minRowSpacing = 5.0; maximumRowsOrColumns = 3
    }
    val vertical = StackLayout().apply { spacing = 5.0 }

    val swappable = itemsViewSwappableLayoutsSample(lined)
    val linedOptions = stack {
        children.add(range("LineSpacing", 5.0, 0.0, 100.0) { lined.lineSpacing = it })
        children.add(range("MinItemSpacing", 5.0, 0.0, 100.0) { lined.minItemSpacing = it })
        children.add(choices("LineHeight", listOf("Small", "Large"), 1) { lined.lineHeight = if (it == 0) 80.0 else 160.0 })
    }
    val uniformOptions = stack {
        visibility = Visibility.Collapsed
        children.add(range("MinColumnSpacing", 5.0, 0.0, 100.0) { uniform.minColumnSpacing = it })
        children.add(range("MinRowSpacing", 5.0, 0.0, 100.0) { uniform.minRowSpacing = it })
        children.add(range("MaximumRowsOrColumns", 3.0, 1.0, 8.0) { uniform.maximumRowsOrColumns = it.toInt() })
    }
    val stackOptions = range("Spacing", 5.0, 0.0, 100.0) { vertical.spacing = it }.apply { visibility = Visibility.Collapsed }
    example("ItemsView with swappable layouts.", swappable, stack {
        children.add(choices("Layout", listOf("LinedFlowLayout", "UniformGridLayout", "StackLayout")) { index ->
            swappable.layout = listOf(lined, uniform, vertical)[index]
            listOf(linedOptions, uniformOptions, stackOptions).forEachIndexed { position, options ->
                options.visibility = if (position == index) Visibility.Visible else Visibility.Collapsed
            }
        })
        children.add(linedOptions); children.add(uniformOptions); children.add(stackOptions)
    })

    val interactive = itemsViewItemsViewSelectionAndItemInvocationSample2()
    val invocation = label("")
    val selection = label("")
    interactive.itemInvoked.add { _, args -> invocation.text = "You invoked Item ${args.invokedItem.toString().toInt() + 1}." }
    interactive.selectionChanged.add { _, _ -> selection.text = "You have selected ${interactive.selectedItems.size} item(s)." }
    example("ItemsView selection and item invocation.", interactive, stack {
        children.add(option("IsItemInvokedEnabled") { interactive.isItemInvokedEnabled = it })
        children.add(select("SelectionMode", listOf("None", "Single", "Multiple", "Extended"), 2) {
            interactive.selectionMode = listOf(ItemsViewSelectionMode.None, ItemsViewSelectionMode.Single, ItemsViewSelectionMode.Multiple, ItemsViewSelectionMode.Extended)[it]
        })
    }, stack { children.add(invocation); children.add(selection) })

}

@GallerySample(route = "ItemsView", title = "A basic ItemsView.")
internal fun itemsViewBasicItemsViewSample() = ItemsView().apply {
    width = 220.0; height = 400.0
    horizontalAlignment = HorizontalAlignment.Left
    itemTemplate = GalleryElementFactory { value ->
        val index = value.toString().toInt()
        Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(index)) ) }.apply {
            minHeight = 100.0; stretch = Stretch.UniformToFill
            named(this, "Item ${index + 1}")
        }
    }
    itemsSource = (0 until 12).toList()
    isItemInvokedEnabled = true
}

@GallerySample(route = "ItemsView", title = "ItemsView selection and item invocation.")
internal fun itemsViewItemsViewSelectionAndItemInvocationSample2() = ItemsView().apply {
    width = 500.0; height = 400.0
    horizontalAlignment = HorizontalAlignment.Left
    layout = UniformGridLayout().apply { maximumRowsOrColumns = 3; minColumnSpacing = 5.0; minRowSpacing = 5.0 }
    itemTemplate = GalleryElementFactory { value ->
        val index = value.toString().toInt()
        Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(index)) ) }.apply {
            minHeight = 100.0; stretch = Stretch.UniformToFill
            named(this, "Item ${index + 1}")
        }
    }
    itemsSource = (0 until 12).toList()
    selectionMode = ItemsViewSelectionMode.Multiple
}

@GallerySample(route = "ItemsView", title = "ItemsView with swappable layouts.")
internal fun itemsViewSwappableLayoutsSample(initialLayout: Layout) = ItemsView().apply {
    width = 500.0; height = 400.0
    horizontalAlignment = HorizontalAlignment.Left
    layout = initialLayout
    itemTemplate = GalleryElementFactory { value ->
        val index = value.toString().toInt()
        Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(landscape(index)) ) }.apply {
            minHeight = 100.0; stretch = Stretch.UniformToFill
            named(this, "Item ${index + 1}")
        }
    }
    itemsSource = (0 until 12).toList()
}
