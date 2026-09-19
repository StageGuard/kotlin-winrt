package io.github.composefluent.winrt.gallery.accessibility

import io.github.composefluent.winrt.gallery.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Ellipse
import microsoft.ui.xaml.shapes.Rectangle
import windows.ui.Color
import windows.ui.text.FontWeight

@GalleryPage(route = "AccessibilityColorContrast", title = "Color Contrast", group = "AccessibilityItem", order = 0)
internal fun accessibilityContrastPage() = stack {
    children.add(label("Accessibility is about building experiences that make your Windows application usable by people of all abilities. For more information about designing accessible apps:"))
    children.add(HyperlinkButton().apply { content = "Accessibility overview"; navigateUri = windows.foundation.Uri("https://learn.microsoft.com/windows/apps/design/accessibility/accessibility-overview") })
    children.add(label("To ensure optimal accessibility and usability, apps should strive to use high-contrast and easy-to-read color combinations for text and its background. Not only will this benefit users with lower visual acuity, but this will also ensure visibility and legibility under a wide range of lighting conditions, screens, and device settings."))
    children.add(HyperlinkButton().apply { content = "Accessibility Insights"; navigateUri = windows.foundation.Uri("https://accessibilityinsights.io/") })
    children.add(label("Color Contrast Checker", 20.0).apply { margin = Thickness(0.0, 20.0, 0.0, 0.0) })
    children.add(label("Use this tool to calculate the contrast ratio of two colors and measure them against the Web Content Accessibility Guidelines (WCAG)."))
    val foregroundBrush = SolidColorBrush(rgb(0u)); val backgroundBrush = SolidColorBrush(rgb(0xFFFFFFu))
    val ratio = label("21:1", 20.0)
    val indicators = List(3) { Ellipse().apply { width = 30.0; height = 30.0; fill = brush(0x006400u) } }
    val icons = List(3) { glyph("\uE73E").apply { this.foreground = brush(0xFFFFFFu) } }
    val results = List(3) { label("Pass").apply { width = 40.0; verticalAlignment = VerticalAlignment.Center; fontWeight = FontWeight(600u) } }
    fun update() {
        val contrast = colorContrast(foregroundBrush.color, backgroundBrush.color)
        ratio.text = "${round(contrast * 100.0) / 100.0}:1"
        repeat(3) { index ->
            val passed = contrast >= if (index == 0) 4.5 else 3.0
            indicators[index].fill = brush(if (passed) 0x006400u else 0x8B0000u)
            icons[index].glyph = if (passed) "\uE73E" else "\uE711"
            results[index].text = if (passed) "Pass" else "Fail"
        }
    }
    children.add(Grid().apply {
        padding = inset(8.0); columnSpacing = 8.0; rowSpacing = 8.0; cornerRadius = corners(8.0); background = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush")
        rowDefinitions.add(autoRow()); rowDefinitions.add(autoRow()); columnDefinitions.add(column(1.0, GridUnitType.Star))
        children.add(stack(8.0, true) {
            children.add(stack(4.0) { children.add(label("Text Color")); children.add(colorSelector(foregroundBrush.color) { foregroundBrush.color = it; update() }) })
            children.add(stack(4.0) { children.add(label("Background Color")); children.add(colorSelector(backgroundBrush.color) { backgroundBrush.color = it; update() }) })
            children.add(stack(4.0) { margin = Thickness(12.0, 0.0, 0.0, 0.0); children.add(label("Contrast Ratio")); children.add(ratio) })
        })
        children.add(Grid().apply {
            Grid.setRow(this, 1); minHeight = 300.0; margin = Thickness(12.0, 0.0, 12.0, 12.0); cornerRadius = corners(4.0)
            repeat(2) { columnDefinitions.add(column(1.0, GridUnitType.Star)) }
            children.add(Grid().apply {
                padding = inset(8.0); background = GalleryTheme.brush("ControlFillColorDefaultBrush"); rowSpacing = 16.0; columnSpacing = 8.0
                repeat(3) { rowDefinitions.add(starRow()) }; repeat(2) { columnDefinitions.add(column(1.0, GridUnitType.Auto)) }; columnDefinitions.add(column(1.0, GridUnitType.Star))
                val descriptions = listOf("Regular text", "Large text (14 pt. bold or 18pt. regular)", "Graphical objects and UI components")
                repeat(3) { index ->
                    children.add(Grid().apply { Grid.setRow(this, index); margin = Thickness(12.0, 0.0, 0.0, 0.0); children.add(indicators[index]); children.add(icons[index]) })
                    Grid.setRow(results[index], index); Grid.setColumn(results[index], 1); children.add(results[index])
                    children.add(stack(0.0) {
                        Grid.setRow(this, index); Grid.setColumn(this, 2); verticalAlignment = VerticalAlignment.Center
                        children.add(label(descriptions[index]).apply { fontWeight = FontWeight(700u) })
                        children.add(label("Requires at least ${if (index == 0) "4.5" else "3"}:1"))
                    })
                }
            })
            children.add(Grid().apply {
                Grid.setColumn(this, 1); padding = inset(8.0); this.background = backgroundBrush
                repeat(3) { rowDefinitions.add(starRow()) }
                fun sentence(size: Double = 14.0) = label("The quick brown fox jumped over the lazy fox.", size).apply { this.foreground = foregroundBrush; verticalAlignment = VerticalAlignment.Center }
                children.add(sentence().apply { padding = Thickness(12.0, 0.0, 12.0, 0.0) })
                children.add(stack(0.0) { Grid.setRow(this, 1); padding = Thickness(12.0, 0.0, 12.0, 0.0); verticalAlignment = VerticalAlignment.Center; children.add(sentence().apply { fontWeight = FontWeight(700u) }); children.add(sentence(18.0)) })
                children.add(stack(8.0, true) {
                    Grid.setRow(this, 2); padding = Thickness(12.0, 0.0, 12.0, 0.0); verticalAlignment = VerticalAlignment.Center
                    children.add(Grid().apply { children.add(Rectangle().apply { width = 30.0; height = 30.0; radiusX = 4.0; radiusY = 4.0; fill = foregroundBrush }); children.add(glyph("\uE73E").apply { this.foreground = brush(0xFFFFFFu) }) })
                    children.add(Grid().apply {
                        children.add(Rectangle().apply { width = 50.0; height = 30.0; radiusX = 15.0; radiusY = 50.0; fill = foregroundBrush })
                        children.add(Ellipse().apply { width = 15.0; height = 15.0; margin = Thickness(0.0, 0.0, 5.0, 0.0); horizontalAlignment = HorizontalAlignment.Right; fill = brush(0xFFFFFFu) })
                    })
                    children.add(glyph("\uE735", 20.0).apply { this.foreground = foregroundBrush })
                })
            })
        })
    })
}

// Ported from WinUI Gallery Samples/AccessibilityColorContrast (MIT).


internal fun colorContrast(first: Color, second: Color): Double {
    fun luminance(color: Color): Double {
        fun channel(value: UByte): Double { val srgb = value.toDouble() / 255.0; return if (srgb <= 0.04045) srgb / 12.92 else ((srgb + 0.055) / 1.055).pow(2.4) }
        return 0.2126 * channel(color.r) + 0.7152 * channel(color.g) + 0.0722 * channel(color.b)
    }
    val a = luminance(first); val b = luminance(second)
    return (max(a, b) + 0.05) / (min(a, b) + 0.05)
}
