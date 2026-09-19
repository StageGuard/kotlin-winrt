package io.github.composefluent.winrt.gallery.styles

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "CompactSizing", title = "Compact Sizing", group = "Styles", order = 2)
internal fun compactSizingPage() = ExamplePage {
    children.add(label("Controls that support compact styling:\n• ListView\n• TextBox\n• PasswordBox\n• AutoSuggestBox\n• ComboBox\n• DatePicker\n• TimePicker\n• TreeView\n• NavigationView\n• MenuBar").apply { margin = Thickness(0.0, 24.0, 0.0, 0.0) })
    val host = compactSizingCompactSizingControlsSample()
    var firstName = ""; var lastName = ""; var passwordValue = ""; var confirmation = ""
    val date = DatePicker().apply { header = "Pick a date" }
    var selectedDateValue = date.selectedDate
    fun show(compact: Boolean) {
        host.content = stack(if (compact) 8.0 else 16.0) {
            if (compact) {
                resources[TextBlock::class] = Style(TextBlock::class).apply {
                    setters.add(Setter(TextBlock.fontSizeProperty, 14.0))
                }
                resources["ControlContentThemeFontSize"] = 14.0; resources["ContentControlFontSize"] = 14.0
                resources["TextControlThemeMinHeight"] = 24.0; resources["TextControlThemePadding"] = Thickness(2.0, 2.0, 6.0, 1.0)
                resources["ListViewItemMinHeight"] = 32.0; resources["TreeViewItemMinHeight"] = 24.0
                resources["TreeViewItemMultiSelectCheckBoxMinHeight"] = 24.0; resources["TreeViewItemPresenterMargin"] = 0.0; resources["TreeViewItemPresenterPadding"] = 0.0
                resources["TimePickerHostPadding"] = Thickness(0.0, 1.0, 0.0, 2.0); resources["DatePickerHostPadding"] = Thickness(0.0, 1.0, 0.0, 2.0)
                resources["DatePickerHostMonthPadding"] = Thickness(9.0, 0.0, 0.0, 1.0); resources["ComboBoxEditableTextPadding"] = Thickness(10.0, 0.0, 30.0, 0.0)
                resources["ComboBoxMinHeight"] = 24.0; resources["ComboBoxPadding"] = Thickness(12.0, 1.0, 0.0, 3.0); resources["NavigationViewItemOnLeftMinHeight"] = 32.0
                resources["TextBoxTopHeaderMargin"] = Thickness(0.0, 2.0, 0.0, 2.0); resources["PasswordBoxTopHeaderMargin"] = Thickness(0.0, 2.0, 0.0, 2.0)
            }
            children.add(label(if (compact) "Compact Size" else "Standard Size", 18.0))
            children.add(TextBox().apply { header = "First Name:"; text = firstName; textChanged.add { _, _ -> firstName = text } })
            children.add(TextBox().apply { header = "Last Name:"; text = lastName; textChanged.add { _, _ -> lastName = text } })
            children.add(PasswordBox().apply { header = "Password:"; password = passwordValue; passwordChanged.add { _, _ -> passwordValue = password } })
            children.add(PasswordBox().apply { header = "Confirm Password:"; this.password = confirmation; passwordChanged.add { _, _ -> confirmation = this.password } })
            children.add(DatePicker().apply { header = "Pick a date"; selectedDate = selectedDateValue; selectedDateChanged.add { _, _ -> selectedDateValue = selectedDate } })
        }
    }
    show(false)
    example("Compact sizing controls.", host, choices("Fluent Standard and Compact Sizing", listOf("Standard", "Compact")) { show(it == 1) })
}

@GallerySample(route = "CompactSizing", title = "Compact sizing controls.")
internal fun compactSizingCompactSizingControlsSample() = ContentControl().apply { horizontalContentAlignment = HorizontalAlignment.Stretch }
