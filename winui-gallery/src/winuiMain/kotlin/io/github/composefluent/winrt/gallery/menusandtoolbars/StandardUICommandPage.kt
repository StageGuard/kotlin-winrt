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

@GalleryPage(route = "StandardUICommand", title = "StandardUICommand", group = "MenusAndToolbars", order = 8)
internal fun standardUICommandPage() = ExamplePage {
    example("Exposing a StandardUICommand through multiple controls.", standardUICommandMultipleControlsSample())
}

@GallerySample(route = "StandardUICommand", title = "Exposing a StandardUICommand through multiple controls.")
internal fun standardUICommandMultipleControlsSample() = stack {
    val list = ListView().apply { height = 500.0; selectionMode = ListViewSelectionMode.Single }
    val command = StandardUICommand(StandardUICommandKind.Delete)
    command.executeRequested.add { _, args ->
        val target = args.parameter ?: list.selectedItem
        if (target != null) list.items.remove(target)
    }
    repeat(15) { index ->
        val item = ListViewItem().apply {
            height = 60.0
            padding = inset(0.0)
            horizontalContentAlignment = HorizontalAlignment.Stretch
        }
        val hover = AppBarButton().apply {
            this.command = command.asWinRT<ICommand>()
            commandParameter = item
            horizontalAlignment = HorizontalAlignment.Right
            visibility = Visibility.Collapsed
        }
        item.content = SwipeControl().apply {
            rightItems = SwipeItems().apply {
                mode = SwipeMode.Execute
                add(SwipeItem().apply {
                    this.command = command.asWinRT<ICommand>()
                    commandParameter = item
                    background = brush(0xFF0000u)
                })
            }
            content = Grid().apply {
                children.add(TextBlock().apply { this.text = "List item $index"; this.fontSize = 18.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(10.0) })
                children.add(hover)
                pointerEntered.add { _, _ -> hover.visibility = Visibility.Visible }
                pointerExited.add { _, _ -> hover.visibility = Visibility.Collapsed }
            }
        }
        item.contextFlyout = MenuFlyout().apply {
            items.add(MenuFlyoutItem().apply {
                this.command = command.asWinRT<ICommand>()
                commandParameter = item
            })
        }
        list.items.add(item)
    }
    children.add(TextBlock().apply { this.text = "StandardUICommand contains the icon, label, keyboard shortcut, and description for a shared command."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    children.add(MenuBar().apply {
        items.add(MenuBarItem().apply { title = "File"; listOf("New", "Open...", "Save", "Exit").forEach { items.add(menuItem(it)) } })
        items.add(MenuBarItem().apply { title = "Edit"; items.add(MenuFlyoutItem().apply { this.command = command.asWinRT<ICommand>() }) })
        items.add(MenuBarItem().apply { title = "Help"; items.add(menuItem("About")) })
    })
    children.add(list)
}
