package io.github.composefluent.winrt.gallery.collections

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.Stretch

@GalleryPage(route = "GridView", title = "GridView", group = "Collections", order = 1)
internal fun gridViewPage() = ExamplePage {
    val basic = gridViewBasicGridViewWithImageItemsSample()
    example("A basic GridView with image items.", basic.first, output = basic.third)

    val layout = gridViewLayoutCustomizationSample()
    example("GridView layout customization.", layout.first, layout.second)

    val content = gridViewContentInsideAGridViewSample()
    example("Content inside a GridView.", content.first, content.second, content.third)
}

@GallerySample(route = "GridView", title = "A basic GridView with image items.")
internal fun gridViewBasicGridViewWithImageItemsSample() = run {
    val output = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val grid = GridView().apply {
        isItemClickEnabled = true; selectionMode = ListViewSelectionMode.Single
        repeat(8) { index -> items.add(Image().apply { this.width = 190.0; this.height = 190.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(galleryPhotos[index].image) ) }.apply {
            height = 130.0; stretch = Stretch.UniformToFill; tag = index; named(this, "Item ${index + 1}")
        }) }
        itemClick.add { _, args -> output.text = "You clicked Item ${args.clickedItem?.asWinRT<FrameworkElement>()?.tag.toString().toInt() + 1}." }
    }
    Triple(grid, null, output)
}

@GallerySample(route = "GridView", title = "GridView layout customization.")
internal fun gridViewLayoutCustomizationSample() = run {
    var horizontal = 5.0
    var vertical = 5.0
    val grid = GridView().apply {
        repeat(8) { index -> items.add(Image().apply { this.width = 190.0; this.height = 190.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(galleryPhotos[index].image) ) }.apply {
            height = 130.0; stretch = Stretch.UniformToFill; tag = index; named(this, "Item ${index + 1}")
        }) }
    }
    fun margins() { grid.items.forEach { it?.asWinRT<FrameworkElement>()?.margin = Thickness(horizontal, vertical, horizontal, vertical) } }
    margins()
    val options = stack {
        children.add(Slider().apply { this.width = 196.0; this.header = "Space between columns"; this.value = 5.0; this.minimum = 0.0; this.maximum = 100.0 }.also { gallerySlider -> gallerySlider.valueChanged.add { _, _ -> val it = gallerySlider.value; horizontal = it; margins() } })
        children.add(Slider().apply { this.width = 196.0; this.header = "Space between rows"; this.value = 5.0; this.minimum = 0.0; this.maximum = 100.0 }.also { gallerySlider -> gallerySlider.valueChanged.add { _, _ -> val it = gallerySlider.value; vertical = it; margins() } })
        children.add(Slider().apply { this.width = 196.0; this.header = "Maximum number of items before wrapping"; this.value = 3.0; this.minimum = 1.0; this.maximum = 8.0 }.also { gallerySlider -> gallerySlider.valueChanged.add { _, _ -> val it = gallerySlider.value; grid.itemsPanelRoot?.asWinRT<ItemsWrapGrid>()?.maximumRowsOrColumns = it.toInt() } })
    }
    Triple(grid, options, null)
}

@GallerySample(route = "GridView", title = "Content inside a GridView.")
internal fun gridViewContentInsideAGridViewSample() = run {
    fun tile(index: Int, mode: Int): FrameworkElement = when (mode) {
        0 -> Image().apply { this.width = 190.0; this.height = 190.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(galleryPhotos[index].image) ) }.apply { height = 130.0; stretch = Stretch.UniformToFill }
        1 -> StackPanel().apply { this.spacing = 8.0; width = 280.0; minHeight = 160.0
            children.add(StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; children.add(Image().apply { this.width = 18.0; this.height = 18.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(galleryPhotos[index].image) ) }); children.add(TextBlock().apply { this.text = "Item ${index + 1}"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) })
            children.add(TextBlock().apply { this.text = galleryPhotos[index].description; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }
        2 -> StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; width = 280.0; children.add(Image().apply { this.width = 100.0; this.height = 100.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(galleryPhotos[index].image) ) })
            children.add(stack {
                children.add(TextBlock().apply { this.text = "Item ${index + 1}"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "${galleryPhotos[index].views} Views"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "${galleryPhotos[index].likes} Likes"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            }) }
        else -> TextBlock().apply { this.text = "Item ${index + 1}"; this.fontSize = 24.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { width = 240.0 }
    }.apply { tag = index; named(this, "Item ${index + 1}") }
    fun fill(grid: GridView, mode: Int) {
        grid.items.clear(); repeat(8) { grid.items.add(tile(it, mode)) }
    }
    val output = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val grid = GridView().apply {
        isItemClickEnabled = true; selectionMode = ListViewSelectionMode.Single
        fill(this, 0)
        itemClick.add { _, args -> output.text = "You clicked Item ${args.clickedItem?.asWinRT<FrameworkElement>()?.tag.toString().toInt() + 1}." }
        selectionChanged.add { _, _ -> output.text = "You have selected ${selectedItems.size} item(s)." }
    }
    val options = stack {
        children.add(RadioButtons().apply { this.header = "ItemTemplate"; listOf("Image", "Icon/Text", "Image/Text", "Text").forEach { this.items.add(it) }; this.selectedIndex = 0 }.also { galleryRadioButtons -> galleryRadioButtons.selectionChanged.add { _, _ -> val gallerySelectedIndex = galleryRadioButtons.selectedIndex; if (gallerySelectedIndex in 0 until galleryRadioButtons.items.size) { val it = gallerySelectedIndex; fill(grid, it) } } })
        children.add(Button().apply { this.content = "Reverse FlowDirection" }.also { galleryButton -> galleryButton.click.add { _, _ -> grid.flowDirection = if (grid.flowDirection == FlowDirection.LeftToRight) FlowDirection.RightToLeft else FlowDirection.LeftToRight } })
        children.add(CheckBox().apply { this.content = "IsItemClickEnabled"; this.isChecked = false }.also { galleryCheckBox -> galleryCheckBox.click.add { _, _ -> val it = galleryCheckBox.isChecked == true; grid.isItemClickEnabled = it } })
        children.add(CheckBox().apply { this.content = "CanDragItems"; this.isChecked = false }.also { galleryCheckBox -> galleryCheckBox.click.add { _, _ -> val it = galleryCheckBox.isChecked == true; grid.canDragItems = it } })
        children.add(CheckBox().apply { this.content = "CanReorderItems"; this.isChecked = false }.also { galleryCheckBox -> galleryCheckBox.click.add { _, _ -> val it = galleryCheckBox.isChecked == true; grid.canReorderItems = it } })
        children.add(CheckBox().apply { this.content = "AllowDrop"; this.isChecked = false }.also { galleryCheckBox -> galleryCheckBox.click.add { _, _ -> val it = galleryCheckBox.isChecked == true; grid.allowDrop = it } })
        children.add(ComboBox().apply { this.header = "SelectionMode"; listOf("None", "Single", "Multiple", "Extended").forEach { this.items.add(it) }; this.selectedIndex = 1 }.also { galleryComboBox -> galleryComboBox.selectionChanged.add { _, _ -> val gallerySelectedIndex = galleryComboBox.selectedIndex; if (gallerySelectedIndex >= 0) { val it = gallerySelectedIndex; grid.selectionMode = listOf(ListViewSelectionMode.None, ListViewSelectionMode.Single, ListViewSelectionMode.Multiple, ListViewSelectionMode.Extended)[it] } } })
    }
    Triple(grid, options, output)
}
