package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.UIElement
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "CheckBox", title = "CheckBox", group = "BasicInput", order = 7)
internal fun checkBoxPage() = ExamplePage {
    val twoStateOutput = label("")
    val threeStateOutput = label("")
    example("A two-state CheckBox.", checkBoxTwoStateSample(twoStateOutput), output = twoStateOutput)
    example("A three-state CheckBox.", checkBoxThreeStateSample(threeStateOutput), output = threeStateOutput)
    example("Using a three-state CheckBox as a Select all control.", checkBoxSelectAllSample())
}

@GallerySample(route = "CheckBox", title = "A two-state CheckBox.")
internal fun checkBoxTwoStateSample(output: TextBlock) = CheckBox().apply {
    content = "Two-state CheckBox"
    click.add { _, _ -> output.text = if (isChecked == true) "Checked" else "Unchecked" }
}

@GallerySample(route = "CheckBox", title = "A three-state CheckBox.")
internal fun checkBoxThreeStateSample(output: TextBlock) = CheckBox().apply {
    content = "Three-state CheckBox"
    isThreeState = true
    click.add { _, _ -> output.text = when (isChecked) { true -> "Checked"; false -> "Unchecked"; null -> "Indeterminate" } }
}

@GallerySample(route = "CheckBox", title = "Using a three-state CheckBox as a Select all control.")
internal fun checkBoxSelectAllSample() = StackPanel().apply {
    spacing = 0.0
    val all = CheckBox().apply { content = "Select all"; isThreeState = true; isChecked = null }
    val options = (1..3).map { number -> CheckBox().apply {
        content = "Option $number"
        isChecked = number == 2
        margin = microsoft.ui.xaml.Thickness(24.0, 0.0, 0.0, 0.0)
    } }
    all.click.add { _, _ -> options.forEach { it.isChecked = all.isChecked != false } }
    options.forEach { option -> option.click.add { _, _ ->
        all.isChecked = when { options.all { it.isChecked == true } -> true; options.none { it.isChecked == true } -> false; else -> null }
    } }
    children.add(all)
    options.forEach { children.add(it) } }
