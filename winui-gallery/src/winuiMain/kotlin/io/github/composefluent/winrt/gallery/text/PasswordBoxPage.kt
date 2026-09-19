package io.github.composefluent.winrt.gallery.text

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.Visibility
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "PasswordBox", title = "PasswordBox", group = "Text", order = 2)
internal fun passwordBoxPage() = ExamplePage {
    val output = label("").apply { visibility = Visibility.Collapsed }
    example("A simple PasswordBox.", stack {
        children.add(passwordBoxSimpleSample(output))
        children.add(output)
    })
    example("A PasswordBox with a header and placeholder text.", passwordBoxHeaderSample())

    val password = passwordBoxRevealSample()
    example("Password reveal mode.", stack(8.0, true) {
        children.add(password)
        children.add(option("Show password") {
            password.passwordRevealMode = if (it) PasswordRevealMode.Visible else PasswordRevealMode.Hidden
        })
    })
}

@GallerySample(route = "PasswordBox", title = "A simple PasswordBox.")
internal fun passwordBoxSimpleSample(output: TextBlock) = PasswordBox().apply {
    width = 300.0
    named(this, "Simple PasswordBox")
    passwordChanged.add { _, _ ->
        val invalid = password.isEmpty() || password == "Password"
        output.text = if (invalid) "'Password' is not allowed." else ""
        output.visibility = if (invalid) Visibility.Visible else Visibility.Collapsed
        if (password == "Password") password = ""
    }
}

@GallerySample(route = "PasswordBox", title = "A PasswordBox with a header and placeholder text.")
internal fun passwordBoxHeaderSample() = PasswordBox().apply {
    width = 300.0
    header = "Password"
    passwordChar = "#"
    placeholderText = "Enter your password"
}

@GallerySample(route = "PasswordBox", title = "Password reveal mode.")
internal fun passwordBoxRevealSample() = PasswordBox().apply {
    width = 250.0
    named(this, "Sample password box")
    passwordRevealMode = PasswordRevealMode.Hidden
}
