package io.github.composefluent.winrt.gallery.fundamentals

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.data.Binding
import microsoft.ui.xaml.data.BindingMode
import microsoft.ui.xaml.data.UpdateSourceTrigger
import windows.globalization.datetimeformatting.DateTimeFormatter

@GalleryPage(route = "Binding", title = "Binding", group = "FundamentalsItem", order = 2)
internal fun bindingPage() = ExamplePage {
    children.add(label("Key concepts\n• Target: The property of a control to which data is bound (e.g., Text, Background, Visibility).\n• Source: The data being bound, such as a property in a class, another control, or a static resource.\n• Binding Modes:\n    ◦ OneWay updates the target when the source changes.\n    ◦ TwoWay updates both the target and the source.\n    ◦ OneTime sets the target once and does not update afterward.").apply { margin = Thickness(0.0, 12.0, 0.0, 0.0) })
    example("Binding controls.", bindingBindingControlsSample())
    example("Binding a property in code.", bindingPropertyInCodeSample())
    example("Binding a function.", bindingFunctionSample())
    example("Converting a binding value.", bindingValueConverterSample())
    example("Binding a view model.", bindingViewModelSample())
    children.add(InfoBar().apply {
        title = "MVVM Toolkit"; isOpen = true; isClosable = false; severity = InfoBarSeverity.Informational; margin = Thickness(0.0, 8.0, 0.0, 0.0)
        message = "The MVVM Toolkit, part of the .NET Community Toolkit, is designed to simplify the implementation of the Model-View-ViewModel (MVVM) pattern in applications. The toolkit includes a sample app to demonstrate its features and usage."
        actionButton = referenceLink("Go to the MVVM Toolkit repository", "https://github.com/CommunityToolkit/MVVM-Samples")
    })
    example("Binding TargetNullValue.", bindingTargetNullSample())
    example("Binding a collection and item content.", bindingCollectionAndItemContentSample())






}

@GallerySample(route = "Binding", title = "Binding controls.")
internal fun bindingBindingControlsSample() = stack {
        children.add(horizontalScroll(StackPanel().apply { this.spacing = 12.0; this.orientation = Orientation.Horizontal; listOf(BindingMode.OneWay, BindingMode.TwoWay).forEachIndexed { index, bindingMode ->
                if (index == 1) children.add(AppBarSeparator())
                children.add(StackPanel().apply { this.spacing = 8.0; children.add(TextBlock().apply { this.text = if (index == 0) "OneWay binding" else "TwoWay binding"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
                    val sourceText = TextBox().apply { minWidth = 380.0; horizontalAlignment = HorizontalAlignment.Left; placeholderText = "Enter text here" }
                    children.add(sourceText)
                    children.add(TextBox().apply {
                        minWidth = 380.0; horizontalAlignment = HorizontalAlignment.Left; placeholderText = if (index == 0) "Mirrors above text" else "Mirrors and edits above text"
                        setBinding(TextBox.textProperty, Binding().apply { source = sourceText; path = PropertyPath("Text"); mode = bindingMode; updateSourceTrigger = UpdateSourceTrigger.PropertyChanged })
                    }) })
            } }))
        children.add(TextBlock().apply { this.text = "In OneWay binding mode, changes in the source (SourceTextBox) are reflected in the target, but not vice versa.\nIn TwoWay binding mode, changes in either box update the other."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    }

@GallerySample(route = "Binding", title = "Binding a property in code.")
internal fun bindingPropertyInCodeSample() = TextBlock().apply {
    text = "Hello, WinUI 3!"
    fontSize = 24.0
    horizontalAlignment = HorizontalAlignment.Center
    verticalAlignment = VerticalAlignment.Center
}

@GallerySample(route = "Binding", title = "Binding a function.")
internal fun bindingFunctionSample() = StackPanel().apply { this.spacing = 8.0; val formatted = TextBlock().apply { this.text = "No date selected"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    children.add(DatePicker().apply {
        header = "Select a date"
        selectedDateChanged.add { _, _ ->
            formatted.text = selectedDate?.let { "Selected date is: ${DateTimeFormatter("dayofweek month day year").format(it)}" } ?: "No date selected"
        }
    })
    children.add(formatted) }

@GallerySample(route = "Binding", title = "Converting a binding value.")
internal fun bindingValueConverterSample() = StackPanel().apply { this.spacing = 8.0; val nonEmpty = TextBlock().apply { this.text = "The input is not empty."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { visibility = Visibility.Collapsed }
    children.add(TextBox().apply {
        width = 300.0; header = "Enter Text:"
        textChanged.add { _, _ -> nonEmpty.visibility = if (text.isEmpty()) Visibility.Collapsed else Visibility.Visible }
    })
    children.add(nonEmpty) }

@GallerySample(route = "Binding", title = "Binding a view model.")
internal fun bindingViewModelSample() = StackPanel().apply { this.spacing = 8.0; val model = BindingExampleModel("Welcome to WinUI 3", "This is an example of binding to a view model.", null)
    children.add(TextBlock().apply { this.text = "Title:"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = model.title; this.fontSize = 16.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    children.add(TextBlock().apply { this.text = "Description:"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = model.description; this.fontSize = 16.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }

@GallerySample(route = "Binding", title = "Binding TargetNullValue.")
internal fun bindingTargetNullSample() = TextBlock().apply {
    val nullableSource = ContentControl().apply { content = null }
    setBinding(TextBlock.textProperty, Binding().apply {
        source = nullableSource; path = PropertyPath("Content"); mode = BindingMode.OneWay; targetNullValue = "Anonymous User"
    })
}

@GallerySample(route = "Binding", title = "Binding a collection and item content.")
internal fun bindingCollectionAndItemContentSample() = Grid().apply {
    val titles = List(4) { "Item ${it + 1}" }
    val dates = listOf("Jun 15, 2025 9:30 AM", "Jul 22, 2025 2:15 PM", "Aug 3, 2025 11:00 AM", "Sep 10, 2025 4:45 PM")
    val descriptions = listOf(
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer id facilisis lectus. Cras nec convallis ante, quis pulvinar tellus.",
        "Quisque accumsan pretium ligula in faucibus. Mauris sollicitudin augue vitae lorem cursus condimentum quis ac mauris.",
        "Ut consequat magna luctus justo egestas vehicula. Integer pharetra risus libero, et posuere justo mattis et.",
        "Duis facilisis, quam ut laoreet commodo, elit ex aliquet massa, non varius tellus lectus et nunc.",
    )
    val detailTitle = TextBlock().apply { this.text = titles[0]; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val detailDate = TextBlock().apply { this.text = dates[0]; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val detailText = TextBlock().apply { this.text = descriptions[0]; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val list = ListView().apply {
        selectionMode = ListViewSelectionMode.Single
        borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("CardStrokeColorDefaultBrush"); cornerRadius = corners(4.0)
        titles.forEachIndexed { index, name ->
            items.add(StackPanel().apply { this.spacing = 2.0; padding = inset(4.0)
                children.add(TextBlock().apply { this.text = name; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
                children.add(TextBlock().apply { this.text = dates[index]; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") }) })
        }
        selectedIndex = 0
        selectionChanged.add { _, _ ->
            if (selectedIndex >= 0) {
                detailTitle.text = titles[selectedIndex]; detailDate.text = dates[selectedIndex]; detailText.text = descriptions[selectedIndex]
            }
        }
    }
    columnSpacing = 16.0; minHeight = 250.0
    columnDefinitions.add(column(200.0, GridUnitType.Pixel)); columnDefinitions.add(column(1.0, GridUnitType.Star))
    children.add(list)
    children.add(StackPanel().apply { this.spacing = 8.0; Grid.setColumn(this, 1); padding = inset(16.0)
        children.add(detailTitle); children.add(detailDate); children.add(detailText) })
}

// Ported from WinUI Gallery Binding (MIT).


internal data class BindingExampleModel(val title: String, val description: String, val nullString: String?)
