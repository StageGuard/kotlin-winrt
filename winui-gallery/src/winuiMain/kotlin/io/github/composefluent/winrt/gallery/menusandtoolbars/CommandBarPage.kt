package io.github.composefluent.winrt.gallery.menusandtoolbars

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*
import windows.system.VirtualKey
import windows.system.VirtualKeyModifiers

@GalleryPage(route = "CommandBar", title = "CommandBar", group = "MenusAndToolbars", order = 3)
internal fun commandBarPage() = ExamplePage {
    val output = label("")
    val bar = commandBarSideLabelSample(output)

    example("A CommandBar with labels on the side.", stack { children.add(bar); children.add(output) }, stack {
        children.add(label("Show or hide"))
        children.add(Button("Open command bar") { bar.isOpen = true; bar.isSticky = true })
        children.add(Button("Close command bar") { bar.isOpen = false; bar.isSticky = false })
        children.add(label("Modify content"))
        children.add(Button("Add secondary commands") {
            if (bar.secondaryCommands.size == 1) {
                listOf(Symbol.Add, Symbol.Delete, Symbol.FontDecrease, Symbol.FontIncrease).forEachIndexed { index, symbol ->
                    if (index == 2) bar.secondaryCommands.add(AppBarSeparator())
                    bar.secondaryCommands.add(appCommand("Button ${index + 1}", symbol).apply {
                        keyboardAccelerators.add(shortcut(listOf(VirtualKey.N, VirtualKey.Delete, VirtualKey.Subtract, VirtualKey.Add)[index], if (index == 1) VirtualKeyModifiers.None else VirtualKeyModifiers.Control))
                    })
                }
            }
        })
        children.add(Button("Remove secondary commands") { while (bar.secondaryCommands.size > 1) bar.secondaryCommands.removeAt(bar.secondaryCommands.size - 1) })
    })

}

@GallerySample(route = "CommandBar", title = "A CommandBar with labels on the side.")
internal fun commandBarSideLabelSample(output: TextBlock) = CommandBar().apply {
    defaultLabelPosition = CommandBarDefaultLabelPosition.Right
    isOpen = false
    listOf("Add" to Symbol.Add, "Edit" to Symbol.Edit, "Share" to Symbol.Share).forEachIndexed { index, (title, icon) ->
        primaryCommands.add(appCommand(title, icon) { output.text = "You clicked: $title" }.apply {
            keyboardAccelerators.add(shortcut(
                listOf(VirtualKey.A, VirtualKey.E, VirtualKey.F4)[index],
                if (index == 2) VirtualKeyModifiers.None else VirtualKeyModifiers.Control,
            ))
        })
    }
    secondaryCommands.add(appCommand("Settings", Symbol.Setting) { output.text = "You clicked: Settings" }.apply {
        keyboardAccelerators.add(shortcut(VirtualKey.I))
    })
}
