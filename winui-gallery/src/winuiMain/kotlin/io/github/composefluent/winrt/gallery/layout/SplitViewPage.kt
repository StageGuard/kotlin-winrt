package io.github.composefluent.winrt.gallery.layout

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.ToggleButton

@GalleryPage(route = "SplitView", title = "SplitView", group = "Layout", order = 5)
internal fun splitViewPage() = ExamplePage {
    val output = label("")
    val split = splitViewSample(output)
    var openState = true
    val open = ToggleButton().apply {
        content = "IsPaneOpen"
        // ToggleButton.IsChecked can return E_FAIL before WinUI realizes its
        // template. Apply the initial visual state after Loaded and keep the
        // interaction state locally so page construction remains reliable.
        loaded.add { _, _ -> runCatching { isChecked = openState } }
        click.add { _, _ -> openState = !openState; split.isPaneOpen = openState }
    }
    split.paneClosed.add { _, _ -> openState = false; runCatching { open.isChecked = false } }
    example("A basic SplitView.", split, stack {
        children.add(open)
        children.add(ToggleSwitch().apply {
            header = "Placement"; offContent = "Left"; onContent = "Right"
            toggled.add { _, _ -> split.panePlacement = if (isOn) SplitViewPanePlacement.Right else SplitViewPanePlacement.Left; split.pane = splitViewPane(isOn, output) }
        })
        children.add(select("DisplayMode", listOf("Inline", "CompactInline", "Overlay", "CompactOverlay")) { split.displayMode = listOf(SplitViewDisplayMode.Inline, SplitViewDisplayMode.CompactInline, SplitViewDisplayMode.Overlay, SplitViewDisplayMode.CompactOverlay)[it] })
        children.add(select("PaneBackground", listOf("SystemControlBackgroundChromeMediumLowBrush", "Red", "Blue", "Green")) {
            split.paneBackground = if (it == 0) GalleryTheme.brush("SystemControlBackgroundChromeMediumLowBrush") else brush(layoutColors[it - 1])
        })
        children.add(range("OpenPaneLength", 256.0, 128.0, 500.0) { split.openPaneLength = it }.apply { stepFrequency = 8.0 })
        children.add(range("CompactPaneLength", 48.0, 24.0, 128.0) { split.compactPaneLength = it }.apply { stepFrequency = 8.0 })
    })
}

@GallerySample(route = "SplitView", title = "A basic SplitView.")
internal fun splitViewSample(output: TextBlock) = SplitView().apply {
    width = 400.0; height = 300.0; isPaneOpen = true; openPaneLength = 256.0; compactPaneLength = 48.0
    displayMode = SplitViewDisplayMode.Inline
    paneBackground = GalleryTheme.brush("SystemControlBackgroundChromeMediumLowBrush")
    content = stack { children.add(TextBlock().apply { this.text = "SPLITVIEW CONTENT"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(output) }.apply { margin = inset(12.0) }
    pane = splitViewPane(false, output)
}

private fun splitViewPane(right: Boolean, output: TextBlock) = stack {
    val links = listOf("People" to Symbol.People, "Globe" to Symbol.Globe, "Message" to Symbol.Message, "Mail" to Symbol.Mail)
    children.add(label("PANE CONTENT").apply { margin = Thickness(60.0, 12.0, 0.0, 0.0) })
    children.add(ListView().apply {
        links.forEach { (title, symbol) -> items.add(ListViewItem().apply {
            content = stack(24.0, true) {
                if (!right) children.add(SymbolIcon(symbol))
                children.add(label(title))
                if (right) children.add(SymbolIcon(symbol))
            }
        }) }
        selectionChanged.add { _, _ -> if (selectedIndex >= 0) output.text = "${links[selectedIndex].first} Page" }
    })
}
