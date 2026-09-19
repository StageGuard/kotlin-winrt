package io.github.composefluent.winrt.gallery.design

import io.github.composefluent.winrt.gallery.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.FontFamily

@GalleryPage(route = "Iconography", title = "Iconography", group = "DesignItem", order = 2)
internal fun iconographyPage() = Grid().apply {
    val tasks = GalleryPageTasks(this)
    rowSpacing = 8.0; rowDefinitions.add(autoRow()); rowDefinitions.add(starRow())
    val search = AutoSuggestBox().apply {
        minWidth = 304.0; maxWidth = 320.0; horizontalAlignment = HorizontalAlignment.Left
        placeholderText = "Search icons by name, code, or tags"; queryIcon = SymbolIcon(Symbol.Find); margin = Thickness(0.0, 16.0, 0.0, 0.0)
    }
    val details = stack(2.0).apply { margin = Thickness(16.0, 16.0, 8.0, 16.0) }
    val noResults = label("No icons found.").apply {
        visibility = Visibility.Collapsed; horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center
    }
    val side = Border().apply {
        Grid.setColumn(this, 1); background = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush")
        borderBrush = GalleryTheme.brush("DividerStrokeColorDefaultBrush"); borderThickness = Thickness(1.0, 0.0, 0.0, 0.0); cornerRadius = CornerRadius(0.0, 8.0, 8.0, 0.0)
        child = scroll(details); visibility = Visibility.Collapsed
    }
    var filtered = galleryIcons.indices.toList()
    val icons = ItemsView().apply {
        minWidth = 100.0; minHeight = 500.0; padding = inset(16.0)
        selectionMode = ItemsViewSelectionMode.Single
        layout = UniformGridLayout().apply { minColumnSpacing = 8.0; minRowSpacing = 8.0; orientation = Orientation.Horizontal }
        itemTemplate = GalleryElementFactory { value ->
            val entry = galleryIcons[value.toString().toInt()]
            Grid().apply {
                width = 96.0; height = 96.0; cornerRadius = corners(4.0)
                background = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush"); borderBrush = GalleryTheme.brush("CardStrokeColorDefaultBrush"); borderThickness = inset(1.0)
                named(this, entry.name); ToolTipService.setToolTip(this, entry.name)
                children.add(Viewbox().apply { width = 28.0; height = 28.0; margin = Thickness(0.0, 0.0, 0.0, 16.0); child = glyph(entry.character) })
                children.add(label(entry.name, 12.0).apply {
                    margin = Thickness(8.0, 0.0, 8.0, 8.0); horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Bottom
                    foreground = GalleryTheme.brush("TextFillColorSecondaryBrush"); textTrimming = TextTrimming.CharacterEllipsis; textWrapping = TextWrapping.NoWrap
                })
            }
        }
        itemsSource = filtered
    }
    fun detail(entry: GalleryIcon) {
        side.visibility = Visibility.Visible; details.children.clear()
        details.children.add(stack(8.0, true) {
            margin = Thickness(0.0, 0.0, 0.0, 24.0)
            children.add(Border().apply { padding = inset(8.0); cornerRadius = corners(4.0); background = GalleryTheme.brush("ControlFillColorDefaultBrush"); borderBrush = GalleryTheme.brush("ControlStrokeColorDefaultBrush"); borderThickness = inset(1.0); child = glyph(entry.character, 48.0) })
            if (entry.fluentOnly) children.add(label("Only supported in Segoe Fluent Icons", 12.0).apply { maxWidth = 210.0; foreground = GalleryTheme.brush("SystemFillColorCautionBrush") })
        })
        listOf("Icon name" to entry.name, "Text glyph" to "&#x${entry.code};", "Code glyph" to "\\u${entry.code}").forEach { (title, value) ->
            details.children.add(label(title, 12.0).apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") })
            details.children.add(stack(8.0, true) { children.add(label(value).apply { fontFamily = FontFamily("Consolas"); isTextSelectionEnabled = true }); children.add(copyButton(value)) })
        }
        val fontIconSource = "FontIcon().apply {\n    glyph = \"\\u${entry.code}\"\n}"
        details.children.add(label("FontIcon Kotlin", 12.0).apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") })
        details.children.add(stack(8.0, true) {
            children.add(label(fontIconSource).apply { fontFamily = FontFamily("Consolas"); isTextSelectionEnabled = true })
            children.add(copyButton(fontIconSource))
        })
        GallerySymbols.find(entry.name)?.let { symbol ->
            details.children.add(label("Symbol name", 12.0).apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") })
            details.children.add(stack(8.0, true) {
                children.add(SymbolIcon(symbol))
                children.add(label(entry.name).apply { fontFamily = FontFamily("Consolas"); isTextSelectionEnabled = true })
                children.add(copyButton(entry.name))
            })
            val symbolIconSource = "SymbolIcon(Symbol.${entry.name})"
            details.children.add(label("SymbolIcon Kotlin", 12.0).apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") })
            details.children.add(stack(8.0, true) {
                children.add(label(symbolIconSource).apply { fontFamily = FontFamily("Consolas"); isTextSelectionEnabled = true })
                children.add(copyButton(symbolIconSource))
            })
        }
        details.children.add(label("Tags", 12.0).apply { margin = Thickness(0.0, 4.0, 0.0, 0.0); foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") })
        if (entry.tags.isEmpty()) details.children.add(label("No tags available."))
        else details.children.add(stack(4.0) {
            margin = Thickness(0.0, 8.0, 0.0, 4.0)
            var row = stack(4.0, true)
            var rowWidth = 0.0
            entry.tags.forEach { tag ->
                val tagWidth = (tag.length * 7.0 + 24.0).coerceIn(48.0, 280.0)
                if (rowWidth > 0.0 && rowWidth + tagWidth > 280.0) {
                    children.add(row)
                    row = stack(4.0, true)
                    rowWidth = 0.0
                }
                row.children.add(Button(tag) { search.text = tag }.apply {
                    cornerRadius = corners(12.0); padding = Thickness(8.0, 2.0, 8.0, 2.0); minHeight = 24.0
                })
                rowWidth += tagWidth + 4.0
            }
            children.add(row)
        })
    }
    icons.selectionChanged.add { _, _ -> filtered.getOrNull(icons.currentItemIndex)?.let { detail(galleryIcons[it]) } }
    var filterJob: Job? = null
    var queryVersion = 0
    fun filter(query: String) {
        val version = ++queryVersion
        filterJob?.cancel()
        filterJob = tasks.launch {
            val matches = withContext(Dispatchers.Default) {
                val terms = query.split(' ').filter { it.isNotBlank() }
                galleryIcons.indices.filter { index -> val entry = galleryIcons[index]; terms.all { term -> entry.name.contains(term, true) || entry.code.contains(term, true) || entry.tags.any { it.contains(term, true) } } }
            }
            if (version == queryVersion) {
                filtered = matches; icons.itemsSource = matches
                noResults.visibility = if (matches.isEmpty()) Visibility.Visible else Visibility.Collapsed
                announce(search, "${matches.size} icons found", "IconSearchResults")
                if (matches.isNotEmpty()) { icons.select(0); detail(galleryIcons[matches[0]]) }
                else { side.visibility = Visibility.Collapsed; details.children.clear() }
            }
        }
    }
    search.textChanged.add { _, _ -> filter(search.text) }
    children.add(search)
    children.add(Grid().apply {
        Grid.setRow(this, 1); cornerRadius = corners(8.0); background = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush")
        columnDefinitions.add(column(1.0, GridUnitType.Star)); columnDefinitions.add(column(334.0, GridUnitType.Pixel))
        children.add(icons); children.add(noResults); children.add(side)
        val detailColumn = columnDefinitions[1]
        sizeChanged.add { _, args ->
            val narrow = args.newSize.width < 700f
            if (rowDefinitions.isEmpty()) { rowDefinitions.add(starRow()); rowDefinitions.add(autoRow()) }
            Grid.setColumn(side, if (narrow) 0 else 1); Grid.setRow(side, if (narrow) 1 else 0)
            detailColumn.width = GridLength(if (narrow) 0.0 else 334.0, GridUnitType.Pixel)
            side.maxHeight = if (narrow) 380.0 else Double.POSITIVE_INFINITY
        }
    })
    loaded.add { _, _ -> filter(search.text) }
}
