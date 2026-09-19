package io.github.composefluent.winrt.gallery.dialogsandflyouts

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "ContentDialog", title = "ContentDialog", group = "DialogsAndFlyouts", order = 0)
internal fun contentDialogPage() = ExamplePage {
    val basic = contentDialogBasicSample()
    val noDefault = contentDialogNoDefaultButtonSample()
    example("A basic ContentDialog.", basic.first, output = basic.second)
    example("A ContentDialog without a default button.", noDefault.first, output = noDefault.second)
}

@GallerySample(route = "ContentDialog", title = "A basic ContentDialog.")
internal fun contentDialogBasicSample() = run {
    val output = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val launch = Button().apply { content = "Show dialog" }
    launch.click.add { _, _ ->
            ContentDialog().apply {
                xamlRoot = launch.xamlRoot
                requestedTheme = launch.actualTheme
                style = controlStyle("DefaultContentDialogStyle")
                title = "Save your work?"; primaryButtonText = "Save"; secondaryButtonText = "Don't Save"
                closeButtonText = "Cancel"; defaultButton = ContentDialogButton.Primary
                content = StackPanel().apply { this.spacing = 0.0; children.add(TextBlock().apply { this.text = "Lorem ipsum dolor sit amet, adipisicing elit."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
                    children.add(CheckBox().apply { content = "Upload your content to the cloud." }) }
                closed.add { _, args -> output.text = when (args.result) {
                    ContentDialogResult.Primary -> "User saved their work"
                    ContentDialogResult.Secondary -> "User did not save their work"
                    else -> "User cancelled the dialog"
                } }
            }.showAsync()
        }
    launch to output
}

@GallerySample(route = "ContentDialog", title = "A ContentDialog without a default button.")
internal fun contentDialogNoDefaultButtonSample() = run {
    val output = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val launch = Button().apply { content = "Show dialog without default button" }
    launch.click.add { _, _ ->
            ContentDialog().apply {
                xamlRoot = launch.xamlRoot
                requestedTheme = launch.actualTheme
                style = controlStyle("DefaultContentDialogStyle")
                title = "Replace file?"; primaryButtonText = "Replace"; secondaryButtonText = "Keep"
                closeButtonText = "Cancel"; defaultButton = ContentDialogButton.None
                content = StackPanel().apply { this.spacing = 0.0; children.add(TextBlock().apply { this.text = "Lorem ipsum dolor sit amet, adipisicing elit."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
                    children.add(CheckBox().apply { content = "Upload your content to the cloud." }) }
                closed.add { _, args -> output.text = when (args.result) {
                    ContentDialogResult.Primary -> "User replaced the file"
                    ContentDialogResult.Secondary -> "User kept the file"
                    else -> "User cancelled the dialog"
                } }
            }.showAsync()
        }
    launch to output
}
