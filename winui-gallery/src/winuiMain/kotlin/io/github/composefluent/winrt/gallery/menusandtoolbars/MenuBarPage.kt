package io.github.composefluent.winrt.gallery.menusandtoolbars

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*
import windows.system.VirtualKey

@GalleryPage(route = "MenuBar", title = "MenuBar", group = "MenusAndToolbars", order = 5)
internal fun menuBarPage() = ExamplePage {
    example("A simple MenuBar.", menuBarSimpleSample())
    example("A MenuBar with keyboard accelerators.", menuBarKeyboardAcceleratorsSample())
    example("A MenuBar with submenus, separators, and radio items.", menuBarSubmenusSample())
}

@GallerySample(route = "MenuBar", title = "A simple MenuBar.")
internal fun menuBarSimpleSample() = run {
    val output = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    fun entry(title: String) = MenuFlyoutItem().apply { text = title; click.add { _, _ -> output.text = "You clicked: $title" } }
    val menu = MenuBar().apply {
        items.add(MenuBarItem().apply {
            title = "File"; items.add(entry("New")); items.add(entry("Open")); items.add(entry("Save")); items.add(entry("Exit"))
        })
        items.add(MenuBarItem().apply {
            title = "Edit"; listOf("Undo", "Cut", "Copy", "Paste").forEach { items.add(entry(it)) }
        })
        items.add(MenuBarItem().apply { title = "Help"; items.add(entry("About")) })
    }
    stack { children.add(output); children.add(menu) }
}

@GallerySample(route = "MenuBar", title = "A MenuBar with keyboard accelerators.")
internal fun menuBarKeyboardAcceleratorsSample() = run {
    val output = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    fun entry(title: String, key: VirtualKey) = MenuFlyoutItem().apply {
        text = title; keyboardAccelerators.add(shortcut(key)); click.add { _, _ -> output.text = "You clicked: $title" }
    }
    val menu = MenuBar().apply {
        items.add(MenuBarItem().apply {
            title = "File"; items.add(entry("New", VirtualKey.N)); items.add(entry("Open", VirtualKey.O)); items.add(entry("Save", VirtualKey.S)); items.add(entry("Exit", VirtualKey.E))
        })
        items.add(MenuBarItem().apply {
            title = "Edit"; listOf("Undo" to VirtualKey.Z, "Cut" to VirtualKey.X, "Copy" to VirtualKey.C, "Paste" to VirtualKey.V).forEach { (title, key) -> items.add(entry(title, key)) }
        })
        items.add(MenuBarItem().apply { title = "Help"; items.add(entry("About", VirtualKey.I)) })
    }
    stack { children.add(output); children.add(menu) }
}

@GallerySample(route = "MenuBar", title = "A MenuBar with submenus, separators, and radio items.")
internal fun menuBarSubmenusSample() = run {
    val output = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    fun entry(title: String) = MenuFlyoutItem().apply { text = title; click.add { _, _ -> output.text = "You clicked: $title" } }
    val menu = MenuBar().apply {
        items.add(MenuBarItem().apply {
            title = "File"
            items.add(MenuFlyoutSubItem().apply {
                text = "New"; listOf("Plain Text Document", "Rich Text Document", "Other Formats").forEach { items.add(entry(it)) }
            })
            items.add(entry("Open")); items.add(entry("Save")); items.add(MenuFlyoutSeparator()); items.add(entry("Exit"))
        })
        items.add(MenuBarItem().apply {
            title = "Edit"
            listOf("Undo", "Cut", "Copy", "Paste").forEach { items.add(entry(it)) }
        })
        items.add(MenuBarItem().apply {
            title = "View"; items.add(entry("Output")); items.add(MenuFlyoutSeparator())
            listOf("Landscape", "Portrait", "Small icons", "Medium icons", "Large icons").forEachIndexed { index, title ->
                if (index == 2) items.add(MenuFlyoutSeparator())
                items.add(RadioMenuFlyoutItem().apply {
                    text = title; groupName = if (index < 2) "Orientation" else "Size"; isChecked = index == 1 || index == 3
                    click.add { _, _ -> output.text = "You clicked: $title" }
                })
            }
        })
        items.add(MenuBarItem().apply { title = "Help"; items.add(entry("About")) })
    }
    stack { children.add(output); children.add(menu) }
}
