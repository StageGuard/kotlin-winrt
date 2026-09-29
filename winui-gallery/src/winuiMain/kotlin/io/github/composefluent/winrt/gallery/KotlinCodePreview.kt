package io.github.composefluent.winrt.gallery

import io.github.composefluent.winrt.gallery.code.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.documents.Run
import microsoft.ui.xaml.media.FontFamily
import windows.applicationmodel.datatransfer.Clipboard
import windows.applicationmodel.datatransfer.DataPackage
import windows.ui.text.FontStyle
import windows.ui.viewmanagement.AccessibilitySettings

/** Rendering consumes build-time spans; displaying XAML never executes the markup. */
internal fun kotlinCodePreview(document: KotlinCodeDocument, xamlDocument: KotlinCodeDocument? = null): UIElement = Grid().apply {
    val documents = listOfNotNull(xamlDocument, document)
    var currentDocument = documents.first()
    fun language(value: KotlinCodeDocument) = if (value.fileName.endsWith(".xaml", true)) "XAML" else "Kotlin"
    rowSpacing = 16.0
    rowDefinitions.add(autoRow())
    rowDefinitions.add(starRow())
    val code = TextBlock().apply {
        fontFamily = FontFamily("Consolas")
        fontSize = 14.0
        textWrapping = TextWrapping.NoWrap
        isTextSelectionEnabled = true
    }
    val runs = mutableListOf<Pair<Run, KotlinCodeKind>>()
    val accessibility = AccessibilitySettings()
    fun applyPalette() {
        val highContrast = accessibility.highContrast
        val palette = if (actualTheme == ElementTheme.Dark) KotlinCodePalette.Dark else KotlinCodePalette.Light
        val ink = KotlinCodeKind.entries.associateWith { kind ->
            if (highContrast) GalleryTheme.brush("TextFillColorPrimaryBrush") else brush(palette.style(kind).foreground)
        }
        runs.forEach { (run, kind) ->
            run.foreground = ink.getValue(kind)
            run.fontStyle = if (palette.style(kind).italic) FontStyle.Italic else FontStyle.Normal
        }
    }
    fun showDocument() {
        code.inlines.clear()
        runs.clear()
        named(code, "${language(currentDocument)} source: ${currentDocument.fileName}")
        var start = 0
        currentDocument.spans.forEach { span ->
            val run = Run().apply { text = currentDocument.source.substring(start, span.end) }
            code.inlines.add(run)
            runs += run to span.kind
            start = span.end
        }
        applyPalette()
    }
    children.add(SelectorBar().apply {
        Grid.setRow(this, 0)
        margin = Thickness(-12.0, 0.0, 0.0, 0.0)
        val tabs = documents.map { value -> SelectorBarItem().apply { text = language(value) } }
        tabs.forEach { items.add(it) }
        selectedItem = tabs.first()
        selectionChanged.add { _, _ ->
            val index = tabs.indexOf(selectedItem)
            if (index >= 0 && currentDocument !== documents[index]) {
                currentDocument = documents[index]
                showDocument()
            }
        }
    })
    children.add(Grid().apply {
        Grid.setRow(this, 1)
        columnDefinitions.add(column(1.0, GridUnitType.Star))
        columnDefinitions.add(column(1.0, GridUnitType.Auto))
        children.add(ScrollViewer().apply {
            horizontalScrollBarVisibility = ScrollBarVisibility.Auto
            horizontalScrollMode = ScrollMode.Auto
            verticalScrollBarVisibility = ScrollBarVisibility.Auto
            verticalScrollMode = ScrollMode.Auto
            verticalAlignment = VerticalAlignment.Top
            content = code.apply { margin = Thickness(0.0, 0.0, 0.0, 16.0) }
        })
        children.add(Button().apply {
            Grid.setColumn(this, 1)
            horizontalAlignment = HorizontalAlignment.Right
            verticalAlignment = VerticalAlignment.Top
            margin = Thickness(5.0, 0.0, 0.0, 0.0)
            padding = inset(6.0)
            content = glyph("\uE8C8")
            named(this, "Copy code")
            ToolTipService.setToolTip(this, "Copy to clipboard")
            click.add { _, _ -> Clipboard.setContent(DataPackage().apply { setText(currentDocument.source) }) }
        })
    })
    actualThemeChanged.add { _, _ -> applyPalette() }
    GalleryTheme.observe(this) { applyPalette() }
    showDocument()
}
