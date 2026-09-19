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

@GalleryPage(route = "XamlUICommand", title = "XamlUICommand", group = "MenusAndToolbars", order = 9)
internal fun xamlUICommandPage() = ExamplePage {
    example("Creating a reusable command with XamlUICommand.", xamlUICommandReusableCommandSample())
}

@GallerySample(route = "XamlUICommand", title = "Creating a reusable command with XamlUICommand.")
internal fun xamlUICommandReusableCommandSample() = stack {
    val output = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val command = XamlUICommand().apply {
        label = "Custom XamlUICommand"
        description = "This is a custom command"
        iconSource = SymbolIconSource().apply { symbol = Symbol.Favorite }
        keyboardAccelerators.add(shortcut(VirtualKey.D))
        executeRequested.add { _, _ -> output.text = "You fired the custom command" }
    }
    children.add(TextBlock().apply { this.text = "XamlUICommand allows the sharing of the UX associated with a command. The button automatically gets its label, icon, shortcut, and description."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    children.add(StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; children.add(AppBarButton().apply { this.command = command.asWinRT<ICommand>() })
        children.add(output) })
}
