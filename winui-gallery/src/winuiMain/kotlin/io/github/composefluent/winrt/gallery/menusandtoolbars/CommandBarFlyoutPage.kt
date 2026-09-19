package io.github.composefluent.winrt.gallery.menusandtoolbars

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.input.*
import microsoft.ui.xaml.media.*
import windows.foundation.Point
import windows.foundation.Uri
import windows.system.VirtualKey
import windows.system.VirtualKeyModifiers

@GalleryPage(route = "CommandBarFlyout", title = "CommandBarFlyout", group = "MenusAndToolbars", order = 4)
internal fun commandBarFlyoutPage() = ExamplePage {
    val output = label("")
    example("Commands associated with an app object.", commandBarFlyoutSample(output))

}

@GallerySample(route = "CommandBarFlyout", title = "Commands associated with an app object.")
internal fun commandBarFlyoutSample(output: TextBlock) = stack {
    val menu = CommandBarFlyout().apply {
        listOf("Share" to Symbol.Share, "Save" to Symbol.Save, "Delete" to Symbol.Delete).forEach { (title, icon) ->
            primaryCommands.add(appCommand(title, icon) { output.text = "You clicked: $title" })
        }
        listOf("Resize", "Move").forEach { title ->
            secondaryCommands.add(AppBarButton().apply { label = title; click.add { _, _ -> output.text = "You clicked: $title" } })
        }
    }
    children.add(TextBlock().apply { this.text = "Click or right click the image to open a CommandBarFlyout"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    children.add(Button().apply {
        padding = inset(0.0)
        named(this, "mountain")
        content = Image().apply { this.width = 400.0; this.height = 400.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/rainier.jpg") ) }.apply { height = 300.0 }
        click.add { _, _ -> menu.showAt(this, FlyoutShowOptions().apply {
            showMode = FlyoutShowMode.Transient
            placement = FlyoutPlacementMode.RightEdgeAlignedTop
        }) }
        contextRequested.add { _, args ->
            menu.showAt(this, FlyoutShowOptions().apply {
                showMode = FlyoutShowMode.Standard
                placement = FlyoutPlacementMode.RightEdgeAlignedTop
            })
            args.handled = true
        }
    })
    children.add(output)
}
