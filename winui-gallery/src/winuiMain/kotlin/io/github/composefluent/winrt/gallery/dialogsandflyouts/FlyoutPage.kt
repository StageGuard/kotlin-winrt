package io.github.composefluent.winrt.gallery.dialogsandflyouts

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "Flyout", title = "Flyout", group = "DialogsAndFlyouts", order = 1)
internal fun flyoutPage() = ExamplePage {
    example("A Button with a Flyout.", flyoutButtonSample())
}

@GallerySample(route = "Flyout", title = "A Button with a Flyout.")
internal fun flyoutButtonSample() = Button().apply {
    content = "Empty cart"
    val confirmation = Flyout()
    confirmation.content = stack {
        children.add(TextBlock().apply { this.text = "All items will be removed. Do you want to continue?"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(Button().apply { this.content = "Yes, empty my cart" }.also { galleryButton -> galleryButton.click.add { _, _ -> confirmation.hide() } })
    }
    flyout = confirmation
}
