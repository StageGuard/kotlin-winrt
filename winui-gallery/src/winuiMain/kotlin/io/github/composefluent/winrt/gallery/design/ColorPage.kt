package io.github.composefluent.winrt.gallery.design

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.*
import windows.ui.Color

@GalleryPage(route = "Color", title = "Color", group = "DesignItem", order = 0)
internal fun colorPage() = stack(0.0) {
    children.add(label("The brushes below are part of WinUI 3. Select a group and copy a brush resource key from any tile."))
    val sections = listOf("Text", "Fill", "Stroke", "Background", "Signal", "High Contrast")
    val host = ContentControl().apply { horizontalContentAlignment = HorizontalAlignment.Stretch }
    val selector = SelectorBar().apply { sections.forEach { items.add(SelectorBarItem().apply { text = it }) } }
    fun show(index: Int) {
        host.content = stack(8.0) {
            galleryPalettes[index].forEach { block ->
                if (block.tiles.isNotEmpty()) {
                    children.add(Grid().apply {
                        repeat(block.columns) { columnDefinitions.add(column(1.0, GridUnitType.Star)) }
                        repeat((block.tiles.maxOf { it.row } + 1)) { rowDefinitions.add(autoRow()) }
                        block.tiles.forEach { tile -> children.add(paletteTile(tile)) }
                    })
                } else if (index == 5) {
                    children.add(label(block.title, if (block.title.length > 20) 14.0 else 20.0).apply { margin = Thickness(0.0, 24.0, 0.0, 0.0) })
                } else {
                    children.add(Border().apply {
                        margin = Thickness(0.0, 36.0, 0.0, 8.0); padding = inset(12.0); cornerRadius = corners(8.0)
                        background = paletteBrush(block.background); borderBrush = GalleryTheme.brush("CardStrokeColorDefaultBrush"); borderThickness = inset(1.0)
                        child = stack(8.0) {
                            children.add(label(block.title, 20.0).apply { foreground = paletteBrush(block.foreground) })
                            children.add(label(block.description, 12.0).apply { foreground = paletteBrush(block.foreground); opacity = 0.8 })
                            children.add(ContentControl().apply {
                                horizontalContentAlignment = HorizontalAlignment.Center; margin = Thickness(0.0, 8.0, 0.0, 8.0)
                                content = palettePreview(block)
                            })
                        }
                    })
                }
            }
        }
    }
    selector.selectionChanged.add { _, _ -> show(selector.items.indexOf(selector.selectedItem).coerceAtLeast(0)) }
    selector.selectedItem = selector.items[0]
    children.add(selector); children.add(host)
    actualThemeChanged.add { _, _ -> show(selector.items.indexOf(selector.selectedItem).coerceAtLeast(0)) }
    show(0)
}

// Ported from WinUI Gallery ColorPage and ColorSections (MIT).


internal fun paletteBrush(value: String): Brush = when {
    value.isEmpty() -> GalleryTheme.brush("TextFillColorPrimaryBrush")
    value.startsWith("#") -> {
        val bits = value.drop(1).toUInt(16)
        SolidColorBrush(Color(if (value.length == 9) (bits shr 24).toUByte() else 255u,
            (bits shr 16).toUByte(), (bits shr 8).toUByte(), bits.toUByte()))
    }
    value == "Transparent" -> SolidColorBrush(Color(0u, 0u, 0u, 0u))
    value == "Black" -> brush(0x000000u)
    value == "White" -> brush(0xFFFFFFu)
    else -> GalleryTheme.brush(value)
}

internal fun paletteTile(tile: PaletteTile) = Grid().apply {
    Grid.setRow(this, tile.row); Grid.setColumn(this, tile.column)
    columnDefinitions.add(column(1.0, GridUnitType.Star)); columnDefinitions.add(column(1.0, GridUnitType.Auto))
    if (tile.backdrop.isNotEmpty()) children.add(paletteBackdrop(tile.backdrop).apply { Grid.setColumnSpan(this, 2) })
    val body = Grid().apply {
    background = if (tile.background.isEmpty()) paletteBrush("Transparent") else paletteBrush(tile.background)
    padding = inset(12.0); rowSpacing = 6.0
    rowDefinitions.add(autoRow()); rowDefinitions.add(autoRow()); rowDefinitions.add(RowDefinition().apply { minHeight = 30.0 }); rowDefinitions.add(autoRow())
    columnDefinitions.add(column(1.0, GridUnitType.Star)); columnDefinitions.add(column(1.0, GridUnitType.Auto))
    val ink = paletteBrush(tile.foreground)
    children.add(label(tile.name).apply { foreground = ink; isTextSelectionEnabled = true; fontWeight = windows.ui.text.FontWeight(600u) })
    children.add(label(tile.explanation, 12.0).apply { foreground = ink; opacity = 0.8; Grid.setRow(this, 1) })
    children.add(label(tile.key, 12.0).apply { foreground = ink; isTextSelectionEnabled = true; Grid.setRow(this, 3); Grid.setColumnSpan(this, 2) })
    children.add(copyButton(tile.key).apply { foreground = ink; Grid.setColumn(this, 1); verticalAlignment = VerticalAlignment.Top })
    if (tile.comment.isNotEmpty()) {
        rowDefinitions.add(autoRow())
        children.add(label(tile.comment, 12.0).apply { foreground = ink; Grid.setRow(this, 4); Grid.setColumnSpan(this, 2) })
    }
    }
    children.add(body)
    if (tile.separator) children.add(Border().apply {
        width = 1.0; horizontalAlignment = HorizontalAlignment.Right; background = GalleryTheme.brush("CardStrokeColorDefaultBrush")
        Grid.setColumn(this, 1); Grid.setRowSpan(this, 4); isHitTestVisible = false
    })
}

internal fun paletteBackdrop(kind: String) = SystemBackdropElement().apply {
    cornerRadius = corners(8.0)
    systemBackdrop = if (kind == "Acrylic") DesktopAcrylicBackdrop() else MicaBackdrop().apply {
        this.kind = if (kind == "MicaAlt") microsoft.ui.composition.systembackdrops.MicaKind.BaseAlt else microsoft.ui.composition.systembackdrops.MicaKind.Base
    }
}

internal fun paletteSurface(fill: String, stroke: String = "CardStrokeColorDefaultBrush", width: Double = 120.0, height: Double = 40.0) = Grid().apply {
    this.width = width; this.height = height; cornerRadius = corners(8.0)
    if (fill.isNotEmpty()) background = GalleryTheme.brush(fill)
    borderBrush = GalleryTheme.brush(stroke); borderThickness = inset(1.0)
}

internal fun palettePreview(block: PaletteBlock): UIElement = when (block.title) {
    "Text", "Accent Text", "Text On Accent" -> label("Aa", 42.0).apply {
        foreground = GalleryTheme.brush(when (block.title) { "Accent Text" -> "AccentTextFillColorPrimaryBrush"; "Text On Accent" -> "TextOnAccentFillColorPrimaryBrush"; else -> "TextFillColorPrimaryBrush" })
    }
    "Control Alt Fill", "Control Strong Stroke" -> ToggleSwitch().apply { onContent = ""; offContent = ""; minWidth = 40.0; maxWidth = 40.0 }
    "Neutral Solid" -> Slider().apply { width = 200.0; value = 50.0 }
    "Neutral Strong" -> CheckBox().apply { content = "Text" }
    "Subtle Fill" -> ListView().apply { width = 200.0; items.add("Text"); items.add("Text") }
    "Accent Fill" -> Button().apply { content = "Text"; style = controlStyle("AccentButtonStyle") }
    "Control Fill", "Control Elevation (gradient strokes)", "Control Stroke" -> Button().apply { content = "Text" }
    "System" -> InfoBar().apply { title = "Title"; message = "This is body text. Windows 11 is faster and more intuitive."; isOpen = true; isClosable = false; severity = InfoBarSeverity.Error }
    "Card Background" -> paletteSurface("CardBackgroundFillColorDefaultBrush", width = 60.0, height = 30.0)
    "Card Stroke" -> paletteSurface("CardBackgroundFillColorDefaultBrush", width = 60.0, height = 48.0)
    "Smoke Background" -> paletteSurface("CardBackgroundFillColorDefaultBrush")
    "Surface Stroke" -> paletteSurface("AcrylicBackgroundFillColorBaseBrush", "SurfaceStrokeColorDefaultBrush")
    "Solid Background" -> paletteSurface("SolidBackgroundFillColorBaseBrush")
    "Acrylic Background" -> paletteSurface("AcrylicBackgroundFillColorBaseBrush")
    "Accent Acrylic Background" -> paletteSurface("AccentAcrylicBackgroundFillColorBaseBrush")
    "Mica Background" -> paletteSurface("AcrylicBackgroundFillColorBaseBrush").apply { children.add(paletteBackdrop("Mica")) }
    "Layer", "Layer on Acrylic" -> paletteSurface(if (block.title == "Layer") "AcrylicBackgroundFillColorBaseBrush" else "").apply {
        if (block.title == "Layer on Acrylic") children.add(paletteBackdrop("Acrylic"))
        children.add(Grid().apply {
            width = 90.0; horizontalAlignment = HorizontalAlignment.Right
            background = GalleryTheme.brush(if (block.title == "Layer") "LayerFillColorDefaultBrush" else "LayerOnAcrylicFillColorDefaultBrush")
            borderBrush = GalleryTheme.brush("CardStrokeColorDefaultBrush"); borderThickness = Thickness(1.0, 0.0, 0.0, 0.0)
        })
    }
    "Layer on Mica Base Alt" -> Grid().apply {
        children.add(paletteBackdrop("MicaAlt"))
        children.add(TabViewItem().apply {
            width = 150.0; height = 30.0; margin = inset(8.0); header = "Text"
            borderBrush = GalleryTheme.brush("ControlStrokeColorSecondaryBrush"); borderThickness = inset(1.0)
        })
    }
    "Divider Stroke" -> paletteSurface("AcrylicBackgroundFillColorBaseBrush", "SurfaceStrokeColorDefaultBrush").apply {
        children.add(Border().apply { width = 1.0; background = GalleryTheme.brush("DividerStrokeColorDefaultBrush") })
    }
    "Focus Stroke" -> Border().apply {
        borderBrush = GalleryTheme.brush("FocusStrokeColorOuterBrush"); borderThickness = inset(2.0); cornerRadius = corners(10.0)
        child = Border().apply {
            borderBrush = GalleryTheme.brush("FocusStrokeColorInnerBrush"); borderThickness = inset(2.0); cornerRadius = corners(9.0)
            child = paletteSurface("", "SurfaceStrokeColorDefaultBrush").apply {
                children.add(label("Text").apply { horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center })
            }
        }
    }
    "Control On Image Fill" -> Grid().apply {
        cornerRadius = corners(4.0)
        children.add(picture("ms-appx:///Assets/SampleMedia/valley.jpg", Double.NaN).apply { maxHeight = 150.0 })
        children.add(paletteSurface("ControlOnImageFillColorDefaultBrush", "ControlStrongStrokeColorDefaultBrush", 20.0, 20.0).apply {
            margin = inset(8.0); horizontalAlignment = HorizontalAlignment.Right; verticalAlignment = VerticalAlignment.Top
        })
    }
    else -> error("Unknown palette preview: ${block.title}")
}
