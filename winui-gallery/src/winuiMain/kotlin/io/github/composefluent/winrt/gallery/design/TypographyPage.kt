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

@GalleryPage(route = "Typography", title = "Typography", group = "DesignItem", order = 4)
internal fun typographyPage() = ExamplePage {
    children.add(label("Typography helps provide structure and hierarchy to UI. The default font for Windows is Segoe UI Variable. Best practice is to use Regular weight for most text, use Semibold for titles. The minimum values should be 12px Regular, 14px Semibold."))
    example("Type ramp", typographyTypeRampSample())
}

@GallerySample(route = "Typography", title = "Type ramp")
internal fun typographyTypeRampSample() = StackPanel().apply { this.spacing = 0.0; children.add(horizontalScroll(Canvas().apply {
            width = 750.0; height = 450.0; horizontalAlignment = HorizontalAlignment.Left
            children.add(themedDesignImage("Typography", 450.0))
            annotation(this, 650.0, 60.0, "Caption"); annotation(this, 190.0, 280.0, "Body"); annotation(this, 83.0, 245.0, "Body Strong")
            annotation(this, 320.0, 20.0, "Title"); annotation(this, 160.0, 110.0, "Display")
        }))
        children.add(horizontalScroll(StackPanel().apply { this.spacing = 0.0; val widths = listOf(272.0, 136.0, 112.0, 244.0)
            children.add(designRow(widths, false, TextBlock().apply { this.text = "Example"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }, TextBlock().apply { this.text = "Variable Font"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }, TextBlock().apply { this.text = "Size/Line height"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }, TextBlock().apply { this.text = "Style"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }).apply { margin = Thickness(16.0, 48.0, 0.0, 24.0) })
            val names = listOf("Caption", "Body", "Body Strong", "Body Large", "Body Large Strong", "Subtitle", "Title", "Title Large", "Display")
            val sizes = listOf(12, 14, 14, 18, 18, 20, 28, 40, 68)
            val lines = listOf(16, 20, 20, 24, 24, 28, 36, 52, 92)
            names.forEachIndexed { index, name ->
                val strong = index == 2 || index == 4 || index >= 5
                val resource = name.replace(" ", "") + "TextBlockStyle"
                children.add(designRow(widths, index % 2 == 0,
                    TextBlock().apply { this.text = name; this.fontSize = sizes[index].toDouble(); this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(16.0, 0.0, 0.0, 0.0); fontWeight = FontWeight(if (strong) 600u else 400u); lineHeight = lines[index].toDouble(); verticalAlignment = VerticalAlignment.Center },
                    TextBlock().apply { this.text = "${if (index == 0) "Small" else if (index < 5) "Text" else "Display"}, ${if (strong) "SemiBold" else "Regular"}"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { verticalAlignment = VerticalAlignment.Center },
                    TextBlock().apply { this.text = "${sizes[index]}/${lines[index]} epx"; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { verticalAlignment = VerticalAlignment.Center },
                    TextBlock().apply { this.text = resource; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { fontFamily = FontFamily("Consolas"); verticalAlignment = VerticalAlignment.Center; isTextSelectionEnabled = true }, copyButton(resource)))
            } })) }
