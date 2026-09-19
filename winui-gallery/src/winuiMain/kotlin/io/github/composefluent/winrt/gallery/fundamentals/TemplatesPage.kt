package io.github.composefluent.winrt.gallery.fundamentals

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Ellipse
import windows.ui.Color

@GalleryPage(route = "Templates", title = "Templates", group = "FundamentalsItem", order = 3)
internal fun templatesPage() = ExamplePage {
    children.add(label("Placement of Templates\nTemplates can be defined at the app, page, or control level, similar to styles and resources. The placement is determined by the intended scope and reuse of the template.\n\nThere are 3 types of templates:\n• ControlTemplate: customizes the structure of a control.\n• DataTemplate: changes how individual items are displayed in a control like a ComboBox or ListView.\n• ItemsPanelTemplate: defines how a collection of items is laid out.").apply { margin = Thickness(0.0, 12.0, 0.0, 0.0) })
    children.add(label("These examples construct the equivalent visual trees and item factories directly in Kotlin."))
    example("Customize the look of a TextBox.", templatesCustomizeTheLookOfATextBoxSample())
    example("Customize ComboBox item content.", templatesCustomizeComboBoxItemContentSample1())
    val layoutSample = templatesCustomizeLayoutOfItemContentSample()
    val items = checkNotNull(layoutSample.content).asWinRT<ItemsView>()
    example("Customize the layout of item content.", layoutSample,
        choices("", listOf("WrapGrid", "StackPanel")) {
            items.layout = if (it == 0) UniformGridLayout() else StackLayout()
        })

}

@GallerySample(route = "Templates", title = "Customize the look of a TextBox.")
internal fun templatesCustomizeTheLookOfATextBoxSample() = StackPanel().apply { this.spacing = 8.0; children.add(TextBlock().apply { this.text = "Enter text here"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(Border().apply {
            minWidth = 200.0; background = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush"); borderBrush = GalleryTheme.brush("AccentFillColorDefaultBrush"); borderThickness = inset(2.0); cornerRadius = corners(4.0)
            child = Grid().apply {
                margin = inset(4.0); columnSpacing = 4.0; columnDefinitions.add(column(1.0, GridUnitType.Auto)); columnDefinitions.add(column(1.0, GridUnitType.Star))
                children.add(SymbolIcon(Symbol.Edit))
                children.add(TextBox().apply {
                    Grid.setColumn(this, 1); padding = inset(8.0); borderThickness = inset(0.0); background = SolidColorBrush(Color(0u, 0u, 0u, 0u)); named(this, "Enter text here")
                })
            }
        }) }

@GallerySample(route = "Templates", title = "Customize ComboBox item content.")
internal fun templatesCustomizeComboBoxItemContentSample1() = ComboBox().apply {
        header = "Options"
        repeat(3) { index -> items.add(ComboBoxItem().apply {
            content = StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; children.add(Ellipse().apply { width = 8.0; height = 8.0; fill = GalleryTheme.brush("AccentFillColorDefaultBrush") }); children.add(TextBlock().apply { this.text = "Option ${index + 1}"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }
        }) }; selectedIndex = 0
    }

@GallerySample(route = "Templates", title = "Customize the layout of item content.")
internal fun templatesCustomizeLayoutOfItemContentSample() = ScrollView().apply {
    content = ItemsView().apply {
        layout = UniformGridLayout()
        selectionMode = ItemsViewSelectionMode.Single
        itemTemplate = GalleryElementFactory {
            TextBlock().apply { this.text = "Item ${it.toString().padStart(2, '0')}"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(8.0) }
        }
        itemsSource = (1..20).toList()
    }
}
