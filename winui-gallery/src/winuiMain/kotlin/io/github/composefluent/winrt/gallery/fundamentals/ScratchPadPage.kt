package io.github.composefluent.winrt.gallery.fundamentals

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.await
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.FontFamily

@GalleryPage(route = "ScratchPad", title = "Scratch Pad", group = "FundamentalsItem", order = 6)
internal fun scratchPadPage() = Grid().apply {
    margin = Thickness(0.0, 12.0, 0.0, 0.0); minHeight = 600.0; cornerRadius = corners(8.0)
    borderBrush = GalleryTheme.brush("CardStrokeColorDefaultBrush"); borderThickness = inset(1.0)
    rowDefinitions.add(starRow()); rowDefinitions.add(starRow())
    val owner = this
    val tasks = GalleryPageTasks(this)
    fun initialContent() = label("Click the Load button to load the sample below.").apply { horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
    val preview = scroll(initialContent()).apply { horizontalScrollBarVisibility = ScrollBarVisibility.Visible; horizontalScrollMode = ScrollMode.Auto; background = GalleryTheme.brush("SolidBackgroundFillColorBaseBrush") }
    val status = label("")
    // The user allows the code-view area to remain a placeholder. Do not expose
    // an editable program and pretend that loading a fixed factory executes it.
    val editor = TextBox().apply {
        isReadOnly = true; acceptsReturn = true; fontFamily = FontFamily("Consolas"); fontSize = 12.0
        text = "Kotlin source editor — coming soon\n\nThis preview is built from projected StackPanel, TextBlock and Button classes."
        named(this, "Kotlin source editor placeholder")
    }
    children.add(preview)
    children.add(Grid().apply {
        Grid.setRow(this, 1); padding = inset(12.0); columnSpacing = 12.0; rowSpacing = 8.0
        background = GalleryTheme.brush("ExpanderContentBackground"); borderBrush = GalleryTheme.brush("DividerStrokeColorDefaultBrush"); borderThickness = Thickness(0.0, 1.0, 0.0, 0.0)
        rowDefinitions.add(starRow()); rowDefinitions.add(autoRow()); columnDefinitions.add(column(1.0, GridUnitType.Star)); columnDefinitions.add(column(1.0, GridUnitType.Auto).apply { minWidth = 168.0 })
        children.add(editor)
        children.add(stack(8.0) {
            Grid.setColumn(this, 1); verticalAlignment = VerticalAlignment.Top
            children.add(Button("Load") {
                preview.content = stack(0.0) {
                    borderThickness = inset(1.0); borderBrush = brush(0x008000u); cornerRadius = corners(4.0); padding = inset(3.0)
                    children.add(label("This is a sample TextBlock.")); children.add(Button().apply { content = "Click me!" })
                }; status.text = "Sample loaded."
            }.apply { horizontalAlignment = HorizontalAlignment.Stretch; style = controlStyle("AccentButtonStyle") })
            children.add(Button("Reset") { tasks.launch {
                val dialog = ContentDialog().apply {
                    xamlRoot = owner.xamlRoot; requestedTheme = owner.actualTheme; title = "Are you sure you want to reset?"
                    content = "Resetting to the default content will replace your current content. Are you sure you want to reset?"
                    primaryButtonText = "Reset"; closeButtonText = "Cancel"; defaultButton = ContentDialogButton.Primary
                }
                val response = try { dialog.showAsync().await() } finally { dialog.hide() }
                if (response == ContentDialogResult.Primary) { preview.content = initialContent(); status.text = "" }
            } }.apply { horizontalAlignment = HorizontalAlignment.Stretch; ToolTipService.setToolTip(this, "Resets to the default scratch pad content") })
        })
        Grid.setRow(status, 1); Grid.setColumnSpan(status, 2); children.add(status)
    })
}
