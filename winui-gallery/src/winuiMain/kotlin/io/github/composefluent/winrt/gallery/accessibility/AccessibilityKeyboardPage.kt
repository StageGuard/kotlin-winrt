package io.github.composefluent.winrt.gallery.accessibility

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.automation.AutomationProperties
import microsoft.ui.xaml.input.XYFocusKeyboardNavigationMode
import microsoft.ui.xaml.shapes.Rectangle
import windows.system.VirtualKey

@GalleryPage(route = "AccessibilityKeyboard", title = "Keyboard Navigation", group = "AccessibilityItem", order = 1)
internal fun keyboardAccessibilityPage() = ExamplePage {
    spacing = 12.0
    children.add(label("Accessibility is about building experiences that make your Windows application usable by people of all abilities. For more information about designing accessible apps:"))
    children.add(referenceLink("Accessibility overview", "https://learn.microsoft.com/windows/apps/design/accessibility/accessibility-overview"))
    children.add(label("If your app does not provide good keyboard access, users who are blind or have mobility issues can have difficulty using your app or may not be able to use it at all."))
    children.add(accessibleHeading("Tab order"))
    children.add(label("To use the keyboard with a control, the control must have focus. The most common way to receive focus is via Tab navigation, which cycles through controls that are tab stops. The order of these tab stops is called the tab order.\n\nAll interactive controls, like buttons, should be tab stops (unless they are in a group that's accessible in some other way), but non-interactive controls, like labels, should not. Try to put initial focus on the most useful or logical element."))
    children.add(referenceLink("Keyboard interactions", "https://learn.microsoft.com/windows/apps/design/input/keyboard-interactions"))
    children.add(referenceLink("Keyboard accessibility", "https://learn.microsoft.com/windows/apps/design/accessibility/keyboard-accessibility"))
    children.add(accessibleHeading("Automatic tab order", 3))
    children.add(label("By default, tab order matches the order elements are added to the visual tree. This is usually the best order:"))
    example("Automatic tab order.", accessibilityKeyboardAutomaticTabOrderSample())
    children.add(accessibleHeading("Manual tab order", 3))
    children.add(label("When the element order doesn't match the logical tab order, though, you can specify tab order manually:"))
    example("Manual tab order.", accessibilityKeyboardManualTabOrderSample1())
    children.add(accessibleHeading("Arrow keys"))
    children.add(label("Users expect groups of similar, related controls to be navigable via Arrow keys, too. This can be instead of or in addition to tab navigation, depending on the situation.\n\nGroups of controls that support arrow key navigation typically support Home/End and PgUp/PgDn, too."))
    children.add(label("See also:"))
    listOf("Navigation" to "navigation", "Home and End keys" to "home-and-end-keys", "Page up and Page down keys" to "page-up-and-page-down-keys", "Control group" to "control-group").forEach { (title, anchor) -> children.add(referenceLink("Keyboard interactions: $title", "https://learn.microsoft.com/windows/apps/design/input/keyboard-interactions#$anchor")) }
    children.add(accessibleHeading("Automatically supporting arrow keys", 3))
    children.add(label("Most controls that group elements support arrow keys (and Home/End and PgUp/PgDn) by default:"))
    example("Automatic arrow keys.", accessibilityKeyboardAutomaticArrowKeysSample2())
    children.add(accessibleHeading("Manually supporting arrow keys with XYFocusKeyboardNavigation", 3))
    children.add(label("You can enable arrow key navigation between items manually, too.\n\nNote that if you're implementing a list of items, users may expect additional affordances like support for Home/End, PgUp/PgDn, and additional accessibility properties like PositionInSet and SizeOfSet. Keyboard navigation can get complicated, but getting it right can make your app a lot easier to use — for everyone."))
    children.add(referenceLink("Focus navigation for keyboard, gamepad, remote control, and accessibility tools", "https://learn.microsoft.com/windows/apps/design/input/focus-navigation"))
    example("Manual arrow keys.", accessibilityKeyboardManualArrowKeysSample3())
    children.add(accessibleHeading("Keyboard shortcuts"))
    children.add(label("Keyboard shortcuts are extremely helpful for Narrator users, keyboard users, and power users. Since keyboard shortcuts generally lack the ability to quickly switch between sections of UI like a mouse can, adding a few keyboard shortcuts for common actions can make your app much easier to use.\n\nWinUI 3 offers 2 types of keyboard shortcuts: Accelerators and Access keys. Accelerators invoke specific app commands, while access keys set focus to specific parts of your UI."))
    children.add(accessibleHeading("Accelerators", 3))
    children.add(label("Accelerators are hotkeys (typically starting with the Ctrl key) that invoke specific app commands.\n\nIt's important to provide an easy way of discovering keyboard accelerators. For example, with tooltips, visible labels, AutomationProperties.AcceleratorKey, accessible descriptions, etc. By default, WinUI adds a tooltip with the hotkey, but consider including the accelerator manually if you use a custom tooltip."))
    children.add(referenceLink("Keyboard accelerators", "https://learn.microsoft.com/windows/apps/design/input/keyboard-accelerators"))
    example("Keyboard accelerators.", accessibilityKeyboardAcceleratorsSample())
    children.add(accessibleHeading("Access keys", 3))
    children.add(label("Access keys are keyboard shortcuts, starting with Alt, that move system focus around your UI.\n\nWhen users press the Alt key, WinUI shows Key Tips next to each control with an access key, so users can discover them. WinUI also populates AccessKey UIA property, so screen reader users can learn access keys, too."))
    children.add(referenceLink("Access keys", "https://learn.microsoft.com/windows/apps/design/input/access-keys"))
    example("Access keys.", accessibilityKeyboardAccessKeysSample5())

}

@GallerySample(route = "AccessibilityKeyboard", title = "Keyboard accelerators.")
internal fun accessibilityKeyboardAcceleratorsSample() = Grid().apply {
    val rectangle = Rectangle().apply { height = 30.0; radiusX = 4.0; radiusY = 4.0; fill = brush(0xFF0000u) }
    columnSpacing = 8.0; rowSpacing = 8.0
    repeat(3) { rowDefinitions.add(autoRow()); columnDefinitions.add(column(1.0, GridUnitType.Auto)) }
    columnDefinitions.add(column(1.0, GridUnitType.Star))
    Grid.setColumnSpan(rectangle, 3); children.add(rectangle)
    val titles = listOf("Red", "Blue", "Chartreuse")
    val keys = listOf(VirtualKey.R, VirtualKey.B, VirtualKey.G)
    val colors = listOf(0xFF0000u, 0x0000FFu, 0x7FFF00u)
    titles.forEachIndexed { index, title -> children.add(Button().apply { this.content = title }.also { galleryButton -> galleryButton.click.add { _, _ -> rectangle.fill = brush(colors[index]) } }.apply {
        Grid.setRow(this, 1); Grid.setColumn(this, index); keyboardAccelerators.add(shortcut(keys[index]))
        AutomationProperties.setAcceleratorKey(this, "Ctrl+${listOf("R", "B", "G")[index]}")
        if (index == 2) ToolTipService.setToolTip(this, "A greenish-yellow (Ctrl+G)")
    }) }
    children.add(TextBlock().apply { this.text = "Ctrl+R, Ctrl+B, and Ctrl+G trigger Red, Blue, and Chartreuse respectively"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply {
        Grid.setRow(this, 2); Grid.setColumnSpan(this, 4)
    })
}

@GallerySample(route = "AccessibilityKeyboard", title = "Automatic tab order.")
internal fun accessibilityKeyboardAutomaticTabOrderSample() = StackPanel().apply { this.spacing = 4.0; children.add(Button().apply { content = "First" }); children.add(TextBlock().apply { this.text = "(not present)"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(Button().apply { content = "Second" })
        children.add(Button().apply { content = "(not present)"; isEnabled = false }); children.add(Button().apply { content = "Third" }) }

@GallerySample(route = "AccessibilityKeyboard", title = "Manual tab order.")
internal fun accessibilityKeyboardManualTabOrderSample1() = Grid().apply {
        columnSpacing = 8.0; rowSpacing = 8.0
        repeat(3) { rowDefinitions.add(autoRow()); columnDefinitions.add(column(1.0, GridUnitType.Auto)) }
        repeat(2) { index ->
            children.add(TextBlock().apply { this.text = "Column ${index + 1}"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { Grid.setColumn(this, index + 1); horizontalAlignment = HorizontalAlignment.Center })
            children.add(TextBlock().apply { this.text = "Row ${index + 1}"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { Grid.setRow(this, index + 1); verticalAlignment = VerticalAlignment.Center })
        }
        listOf("First stop", "Third stop", "Second stop", "Not a stop").forEachIndexed { index, title -> children.add(Button().apply {
            content = title; Grid.setRow(this, index / 2 + 1); Grid.setColumn(this, index % 2 + 1); horizontalAlignment = HorizontalAlignment.Stretch
            tabIndex = listOf(1, 3, 2, 0)[index]; isTabStop = index != 3
        }) }
    }

@GallerySample(route = "AccessibilityKeyboard", title = "Automatic arrow keys.")
internal fun accessibilityKeyboardAutomaticArrowKeysSample2() = StackPanel().apply { this.spacing = 0.0; children.add(ListView().apply { width = 300.0; named(this, "Colors"); listOf("Red", "Blue", "Green", "Yellow").forEach { items.add(it) } })
        children.add(TextBlock().apply { this.text = "Tab navigates to the control, arrow keys navigate within the control"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }

@GallerySample(route = "AccessibilityKeyboard", title = "Manual arrow keys.")
internal fun accessibilityKeyboardManualArrowKeysSample3() = StackPanel().apply { this.spacing = 4.0; children.add(StackPanel().apply { this.spacing = 8.0; padding = inset(8.0); background = GalleryTheme.brush("CardBackgroundFillColorSecondaryBrush"); borderBrush = GalleryTheme.brush("SurfaceStrokeColorDefaultBrush"); borderThickness = inset(1.0); cornerRadius = corners(4.0); named(this, "Potatoes?")
            children.add(TextBlock().apply { this.text = "Potatoes?"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(StackPanel().apply { this.spacing = 4.0; this.orientation = Orientation.Horizontal; xYFocusKeyboardNavigation = XYFocusKeyboardNavigationMode.Enabled; listOf("Boil 'em", "Mash 'em", "Stick 'em in a stew").forEach { children.add(Button().apply { content = it }) } }) })
        children.add(TextBlock().apply { this.text = "Arrow keys navigate between each button, but tab still navigates between each button, and other buttons like Home/End do not work"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }

@GallerySample(route = "AccessibilityKeyboard", title = "Access keys.")
internal fun accessibilityKeyboardAccessKeysSample5() = StackPanel().apply { this.spacing = 0.0; children.add(MenuBar().apply {
            listOf(Triple("File", "F", listOf("New" to "N", "Open..." to "O", "Save" to "S", "Exit" to "E")), Triple("Edit", "E", listOf("Undo" to "U", "Cut" to "X", "Copy" to "C", "Paste" to "V")), Triple("Help", "H", listOf("About" to "A"))).forEach { (title, key, entries) ->
                items.add(MenuBarItem().apply { this.title = title; accessKey = key; entries.forEach { (text, entryKey) -> items.add(MenuFlyoutItem().apply { this.text = text; accessKey = entryKey }) } })
            }
        })
        children.add(TextBlock().apply { this.text = "Press and release Alt to display Key Tips; use Alt+letter to move focus to items"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }
