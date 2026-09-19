package io.github.composefluent.winrt.gallery.design

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.FontFamily
import microsoft.ui.xaml.media.imaging.BitmapImage
import windows.applicationmodel.datatransfer.Clipboard
import windows.applicationmodel.datatransfer.DataPackage
import windows.foundation.Uri
import windows.ui.text.FontWeight

@GalleryPage(route = "Geometry", title = "Geometry", group = "DesignItem", order = 1)
internal fun geometryPage() = ExamplePage {
    children.add(label("Geometry describes the shape, size and position of UI elements on screen. These fundamental design elements help experiences feel coherent across the entire design system. WinUI uses three levels of rounding depending on what UI component is being rounded and how that component is arranged relative to neighboring elements. You can reference built-in corner radii resources from a projected ResourceDictionary."))
    example("Geometry", geometryGeometrySample())
}

@GallerySample(route = "Geometry", title = "Geometry")
internal fun geometryGeometrySample() = StackPanel().apply { this.spacing = 0.0; children.add(horizontalScroll(Canvas().apply {
            width = 505.0; height = 271.0; horizontalAlignment = HorizontalAlignment.Left
            children.add(themedDesignImage("Geometry", 271.0))
            annotation(this, 16.0, 16.0, "8px", "OverlayCornerRadius")
            annotation(this, 16.0, 148.0, "0px")
            annotation(this, 240.0, 168.0, "4px", "ControlCornerRadius")
        }))
        children.add(horizontalScroll(StackPanel().apply { this.spacing = 0.0; children.add(designRow(listOf(148.0, 400.0, 160.0), false, TextBlock().apply { this.text = "Corner radius"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }, TextBlock().apply { this.text = "Usage"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }, TextBlock().apply { this.text = "Style"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }).apply { margin = Thickness(16.0, 48.0, 0.0, 24.0) })
            val radii = listOf(8.0, 4.0, 0.0)
            val usages = listOf("Top-level containers such as app windows, flyouts, cards and dialogs.", "In-page elements such as controls and list backplates.", "Straight edges that intersect with other straight edges.")
            val resources = listOf("OverlayCornerRadius", "ControlCornerRadius", "N/a")
            radii.forEachIndexed { index, radius ->
                val value = StackPanel().apply { this.spacing = 12.0; this.orientation = Orientation.Horizontal; verticalAlignment = VerticalAlignment.Center
                    children.add(Border().apply { width = 20.0; height = 20.0; margin = Thickness(16.0, 0.0, 0.0, 0.0); background = GalleryTheme.brush("AccentFillColorDefaultBrush"); cornerRadius = corners(radius) })
                    children.add(TextBlock().apply { this.text = "${radius.toInt()}px"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }
                children.add(designRow(listOf(148.0, 400.0, 160.0), index % 2 == 0, value,
                    TextBlock().apply { this.text = usages[index]; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { verticalAlignment = VerticalAlignment.Center },
                    TextBlock().apply { this.text = resources[index]; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { fontFamily = FontFamily("Consolas"); isTextSelectionEnabled = true; verticalAlignment = VerticalAlignment.Center },
                    if (index < 2) copyButton(resources[index]) else Border()))
            } })) }
