package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "Button", title = "Button", group = "BasicInput", order = 0)
internal fun buttonPage() = ExamplePage {
    val output = label("")
    val standard = buttonTextSample().apply { click.add { _, _ -> output.text = "You clicked: Standard XAML button" } }
    val disabled = CheckBox().apply {
        content = "Disable button"
        click.add { _, _ -> standard.isEnabled = isChecked != true }
    }
    val imageOutput = label("")
    val imageButton = buttonImageSample().apply { click.add { _, _ -> imageOutput.text = "You clicked: Image button" } }
    example("A simple Button with text content.", standard, disabled, output)
    example("A Button with image content.", imageButton, output = imageOutput)
    example("Buttons with built-in styles.", stack(16.0, true) {
        buttonStylesSample().forEach { children.add(it) }
    })
    example("Buttons with long content.", buttonLongContentDemo())
}

@GallerySample(route = "Button", title = "A simple Button with text content.")
internal fun buttonTextSample() = Button().apply {
    content = "Standard XAML button"
}

@GallerySample(route = "Button", title = "A Button with image content.")
internal fun buttonImageSample() = Button().apply {
    width = 50.0
    height = 50.0
    content = FontIcon().apply { glyph = "\uE8B9"; fontSize = 24.0 }
}

@GallerySample(route = "Button", title = "Buttons with built-in styles.")
internal fun buttonStylesSample() = listOf(
    Button().apply { content = "Accent style button"; style = controlStyle("AccentButtonStyle") },
    Button().apply { content = "Subtle style button"; style = controlStyle("SubtleButtonStyle") },
)

@GallerySample(route = "Button", title = "Buttons with long content.")
internal fun buttonLongContentSample() = listOf(
    Button().apply {
        content = "This is some text that is too long and will get cut off"
        horizontalAlignment = HorizontalAlignment.Stretch
    },
    Button().apply {
        content = "This is another text that would result in being cut off"
        horizontalAlignment = HorizontalAlignment.Stretch
    },
    Button().apply {
        maxWidth = 240.0
        content = TextBlock().apply {
            text = "This is some text that is too long and will get cut off without wrapping"
            textWrapping = TextWrapping.Wrap
        }
    },
    Button().apply {
        maxWidth = 240.0
        content = TextBlock().apply {
            text = "This is another text that would result in being cut off without wrapping"
            textWrapping = TextWrapping.Wrap
        }
    },
)

private fun buttonLongContentDemo() = stack(8.0) {
    val buttons = buttonLongContentSample()
    children.add(label("One option to mitigate clipped content is to place Buttons underneath each other, allowing for more space to grow horizontally:"))
    children.add(buttons[0])
    children.add(buttons[1])
    children.add(label("Another option is to explicitly wrap the Button's content"))
    children.add(stack(8.0, true) {
        children.add(buttons[2])
        children.add(buttons[3])
    })
}
