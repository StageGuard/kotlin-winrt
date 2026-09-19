package io.github.composefluent.winrt.gallery.fundamentals

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.automation.AutomationProperties
import microsoft.ui.xaml.automation.peers.*
import microsoft.ui.xaml.documents.*
import windows.globalization.numberformatting.DecimalFormatter

@GalleryPage(route = "CustomUserControls", title = "Custom & User Controls", group = "FundamentalsItem", order = 4)
internal fun customControlsPage() = ExamplePage {
    children.add(label("Custom control", 20.0).apply { margin = Thickness(0.0, 8.0, 0.0, 8.0) })
    children.add(label("A custom control is a reusable component that encapsulates behavior and UI logic. It supports styling and theming.\n\n• Encapsulation: custom controls encapsulate behavior and UI logic, making them reusable across different projects.\n• Theming: they support light and dark themes through theme resources.\n\nKey points\n• Build reusable visual trees from projected controls.\n• Use DependencyProperty for properties that support data binding."))
    example("Counter control: increment and decrement.", customUserControlsCounterControlIncrementAndDecrementSample())
    example("Basic custom password box.", customPasswordBoxSample())
    children.add(label("UserControl", 20.0).apply { margin = Thickness(0.0, 24.0, 0.0, 8.0) })
    children.add(label("A UserControl is a reusable component that combines existing controls and logic into a cohesive unit. It allows for encapsulation of functionality and a consistent design across multiple instances."))
    example("Temperature converter UserControl example.", customUserControlsTemperatureConverterUserControlExampleSample2())

}

@GallerySample(route = "CustomUserControls", title = "Counter control: increment and decrement.")
internal fun customUserControlsCounterControlIncrementAndDecrementSample() = StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; children.add(GalleryCounterControl()); children.add(GalleryCounterControl(false)) }

@GallerySample(route = "CustomUserControls", title = "Temperature converter UserControl example.")
internal fun customUserControlsTemperatureConverterUserControlExampleSample2() = GalleryTemperatureConverter()

@GallerySample(route = "CustomUserControls", title = "Basic custom password box.")
internal fun customPasswordBoxSample() = StackPanel().apply { this.spacing = 8.0; val password = GalleryValidatedPasswordBox().apply {
        minLength = 8; width = 240.0; horizontalAlignment = HorizontalAlignment.Left
        header = "Password"; placeholderText = "Enter password..."
    }
    val submit = Button().apply {
        content = "Submit"; width = 240.0; isEnabled = password.isValid; style = controlStyle("AccentButtonStyle")
    }
    password.validityChanged = { submit.isEnabled = it }
    children.add(password); children.add(submit) }

// Ported from WinUI Gallery CustomUserControls (MIT).
// Visual trees are composed in Kotlin because runtime markup is not supported.


internal fun raiseLiveRegionChanged(element: FrameworkElement) {
    if (!AutomationPeer.listenerExists(AutomationEvents.LiveRegionChanged)) return
    FrameworkElementAutomationPeer.createPeerForElement(element)
        .raiseAutomationEvent(AutomationEvents.LiveRegionChanged)
}

class GalleryCounterControl(private val increment: Boolean) : UserControl() {
    constructor() : this(true)
    var count: Int
        get() = getValue(countProperty) as Int
        set(value) { setValue(countProperty, value) }
    init {
        isTabStop = false
        val number = label("0", 20.0).apply { horizontalAlignment = HorizontalAlignment.Center; named(this, "Counter value"); AutomationProperties.setLiveSetting(this, AutomationLiveSetting.Polite) }
        content = stack(8.0) {
            horizontalAlignment = HorizontalAlignment.Left; children.add(number)
            children.add(Button(if (increment) "Increase" else "Decrease") {
                count += if (increment) 1 else -1
            }.apply {
                horizontalAlignment = HorizontalAlignment.Center; minWidth = 100.0; named(this, "${if (increment) "Increase" else "Decrease"} counter")
            })
        }
        registerPropertyChangedCallback(countProperty) { _, _ ->
            number.text = count.toString()
            raiseLiveRegionChanged(number)
        }
    }
    companion object {
        val countProperty = DependencyProperty.register("Count", Int::class, GalleryCounterControl::class, PropertyMetadata(0))
    }
}

class GalleryValidatedPasswordBox : UserControl() {
    private val input = PasswordBox()
    private val validation = RichTextBlock().apply { isTextSelectionEnabled = false; AutomationProperties.setLiveSetting(this, AutomationLiveSetting.Polite) }
    var minLength = 8
        set(value) { field = value; update() }
    var header: String
        get() = input.header.toString()
        set(value) { input.header = value }
    var placeholderText: String
        get() = input.placeholderText
        set(value) { input.placeholderText = value }
    var password: String
        get() = input.password
        set(value) { input.password = value }
    var isValid = false
        private set
    // This callback connects controls within the Kotlin page; it is not part
    // of the control's exported WinRT contract.
    internal var validityChanged: (Boolean) -> Unit = {}
    init {
        isTabStop = false
        content = stack(4.0) { children.add(input); children.add(validation) }
        input.passwordChanged.add { _, _ -> update() }
        validation.actualThemeChanged.add { _, _ -> update() }
        update()
    }
    private fun update() {
        val value = input.password
        val errors = buildList {
            if (value.none(Char::isUpperCase)) add("Missing uppercase")
            if (value.none(Char::isDigit)) add("Missing number")
            if (value.length < minLength) add("Too short!")
        }
        isValid = errors.isEmpty(); validityChanged(isValid)
        validation.visibility = if (value.isEmpty()) Visibility.Collapsed else Visibility.Visible
        validation.blocks.clear()
        if (value.isEmpty()) return
        val light = validation.actualTheme == ElementTheme.Light
        val color = brush(if (isValid) { if (light) 0x0F7B0Fu else 0x6CCB5Fu } else { if (light) 0xC42B1Cu else 0xFF99A4u })
        validation.blocks.add(Paragraph().apply {
            (if (isValid) listOf("Password is valid") else errors).forEachIndexed { index, message ->
                if (index > 0) inlines.add(LineBreak())
                inlines.add(InlineUIContainer().apply { child = glyph(if (isValid) "\uE930" else "\uEA39", 14.0).apply { foreground = color } })
                inlines.add(Run().apply { text = " $message"; foreground = color })
            }
        })
        raiseLiveRegionChanged(validation)
    }
}

class GalleryTemperatureConverter : UserControl() {
    init {
        isTabStop = false
        val input = TextBox().apply { header = "Enter Temperature in Celsius"; width = 200.0; placeholderText = "Celsius" }
        val result = label("")
        val formatter = DecimalFormatter().apply { fractionDigits = 2; integerDigits = 1 }
        val convert = Button("Convert to Fahrenheit") {
            result.text = formatter.parseDouble(input.text)?.let { "Fahrenheit: ${formatter.formatDouble(it * 9 / 5 + 32)}°F" } ?: "Invalid input!"
        }.apply { width = 200.0; isEnabled = false }
        input.textChanged.add { _, _ -> convert.isEnabled = input.text.isNotBlank() }
        content = stack(8.0) { children.add(input); children.add(convert); children.add(result) }
    }
}
