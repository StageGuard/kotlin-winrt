package io.github.composefluent.winrt.gallery.menusandtoolbars

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.input.KeyboardAccelerator
import microsoft.ui.xaml.media.*
import windows.foundation.Point
import windows.foundation.Uri
import windows.system.VirtualKey
import windows.system.VirtualKeyModifiers

@GalleryPage(route = "MenuFlyout", title = "MenuFlyout", group = "MenusAndToolbars", order = 6)
internal fun menuFlyoutPage() = ExamplePage {
    val output = label("")

    example("An AppBarButton with a MenuFlyout.", menuFlyoutAppBarButtonSample(output), output = output)
    example("ToggleMenuFlyoutItems and a separator.", menuFlyoutToggleItemsSample())
    example("Cascading menus.", menuFlyoutCascadingSample())
    val splitOutput = label("")

    example("A MenuFlyout with SplitMenuFlyoutItems.", menuFlyoutSplitItemsSample(splitOutput), output = splitOutput)
    example("A MenuFlyout with icons.", menuFlyoutIconsSample())
    example("Icons and keyboard accelerators.", menuFlyoutKeyboardAcceleratorsSample())
    example("A MenuFlyout with radio items.", menuFlyoutRadioItemsSample())


}

@GallerySample(route = "MenuFlyout", title = "An AppBarButton with a MenuFlyout.")
internal fun menuFlyoutAppBarButtonSample(output: TextBlock) = AppBarButton().apply {
    label = "Sort"
    icon = SymbolIcon(Symbol.Sort)
    isCompact = true
    flyout = MenuFlyout().apply {
        listOf("rating", "match", "distance").forEach { value ->
            items.add(menuItem("By $value") { output.text = "Sort by: $value" })
        }
    }
}

@GallerySample(route = "MenuFlyout", title = "A MenuFlyout with SplitMenuFlyoutItems.")
internal fun menuFlyoutSplitItemsSample(output: TextBlock) = Button().apply {
    content = "File Options"
    flyout = MenuFlyout().apply {
        listOf(
            "Save" to listOf("Save as .docx", "Save as .pdf", "Save as .txt"),
            "Share" to listOf("Share via email", "Share via link"),
        ).forEach { (title, commands) ->
            items.add(SplitMenuFlyoutItem().apply {
                text = title
                icon = SymbolIcon(if (title == "Save") Symbol.Save else Symbol.Share)
                click.add { _, _ -> output.text = "Clicked: $title" }
                commands.forEach { command -> items.add(menuItem(command) { output.text = "Clicked: $command" }) }
            })
        }
    }
}

@GallerySample(route = "MenuFlyout", title = "ToggleMenuFlyoutItems and a separator.")
internal fun menuFlyoutToggleItemsSample() = Button().apply {
    content = "Options"
    flyout = MenuFlyout().apply {
        items.add(menuItem("Reset"))
        items.add(MenuFlyoutSeparator())
        listOf("Repeat", "Shuffle").forEach { items.add(ToggleMenuFlyoutItem().apply { text = it; isChecked = true }) }
    }
}

@GallerySample(route = "MenuFlyout", title = "Cascading menus.")
internal fun menuFlyoutCascadingSample() = Button().apply {
    content = "File Options"
    flyout = MenuFlyout().apply {
        items.add(menuItem("Open"))
        items.add(MenuFlyoutSubItem().apply {
            text = "Send to"
            items.add(menuItem("Bluetooth"))
            items.add(menuItem("Desktop (shortcut)"))
            items.add(MenuFlyoutSubItem().apply {
                text = "Compressed file"
                listOf("Compress and email", "Compress to .7z", "Compress to .zip").forEach { items.add(menuItem(it)) }
            })
        })
    }
}

@GallerySample(route = "MenuFlyout", title = "A MenuFlyout with icons.")
internal fun menuFlyoutIconsSample() = Button().apply {
    content = "Edit Options"
    flyout = MenuFlyout().apply {
        listOf("Share" to Symbol.Share, "Copy" to Symbol.Copy, "Delete" to Symbol.Delete).forEach { (title, symbol) ->
            items.add(menuItem(title).apply { icon = SymbolIcon(symbol) })
        }
        items.add(MenuFlyoutSeparator())
        items.add(menuItem("Rename"))
        items.add(menuItem("Select"))
    }
}

@GallerySample(route = "MenuFlyout", title = "Icons and keyboard accelerators.")
internal fun menuFlyoutKeyboardAcceleratorsSample() = Button().apply {
    content = "Edit Options"
    flyout = MenuFlyout().apply {
        listOf("Share" to Symbol.Share, "Copy" to Symbol.Copy, "Delete" to Symbol.Delete).forEachIndexed { index, (title, symbol) ->
            items.add(menuItem(title).apply {
                icon = SymbolIcon(symbol)
                keyboardAccelerators.add(shortcut(
                    listOf(VirtualKey.S, VirtualKey.C, VirtualKey.Delete)[index],
                    if (index == 2) VirtualKeyModifiers.None else VirtualKeyModifiers.Control,
                ))
            })
        }
        items.add(MenuFlyoutSeparator())
        items.add(menuItem("Rename"))
        items.add(menuItem("Select"))
    }
}

@GallerySample(route = "MenuFlyout", title = "A MenuFlyout with radio items.")
internal fun menuFlyoutRadioItemsSample() = Button().apply {
    content = "Options"
    flyout = MenuFlyout().apply {
        listOf("Landscape", "Portrait", "Small icons", "Medium icons", "Large icons").forEachIndexed { index, title ->
            if (index == 2) items.add(MenuFlyoutSeparator())
            items.add(RadioMenuFlyoutItem().apply {
                text = title
                groupName = if (index < 2) "MenuFlyoutOrientation" else "MenuFlyoutSize"
                isChecked = index == 1 || index == 3
            })
        }
    }
}
