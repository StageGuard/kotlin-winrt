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

@GalleryPage(route = "Spacing", title = "Spacing", group = "DesignItem", order = 3)
internal fun spacingPage() = stack(0.0) {
    children.add(label("The use of consistently sized spacing and gutters semantically groups an experience into separate components. These values map to our rounded corner logic and together help create a cohesive and usable layout. A best practice in design is to use a 4px grid. This means that any spacing or sizing should be a multiple of 4. This helps to create a consistent and harmonious layout and these values are easy to scale.\nBelow, you can find a few examples of common layout types with highlighted spacing values (in epx)."))
    children.add(ScrollView().apply {
        margin = Thickness(0.0, 24.0, 0.0, 0.0)
        content = stack(36.0, true) {
            margin = Thickness(0.0, 0.0, 0.0, 16.0)
            listOf("Page with cards layout" to "Cards", "Dialog layout" to "Dialog").forEach { (title, asset) ->
                children.add(stack { verticalAlignment = VerticalAlignment.Top; children.add(label(title, 20.0).apply { horizontalAlignment = HorizontalAlignment.Center }); children.add(themedDesignImage(asset)) })
            }
        }
    })
    children.add(horizontalScroll(stack(0.0) {
        padding = Thickness(12.0, 24.0, 12.0, 12.0)
        children.add(designRow(listOf(206.0, 400.0), false, label("Value", 12.0), label("Usage", 12.0)).apply { margin = Thickness(16.0, 0.0, 0.0, 24.0) })
        val values = listOf(4, 8, 12, 16, 24, 36, 48)
        val usages = listOf("Spacing used for compact sizing.", "Spacing between UI controls, control + label.", "Spacing between control + header, surface and edge text, text sections.", "Padding used in list styles, cards.", "Spacing between content sections.", "Padding on pages.", "Spacing between page sections with title.")
        values.forEachIndexed { index, value -> children.add(designRow(listOf(86.0, 136.0, 400.0), index % 2 == 0,
            label("${value}epx").apply { margin = Thickness(16.0, 0.0, 0.0, 0.0); verticalAlignment = VerticalAlignment.Center },
            Border().apply { width = value.toDouble(); height = 20.0; cornerRadius = corners(4.0); horizontalAlignment = HorizontalAlignment.Left; background = GalleryTheme.brush("AccentFillColorDefaultBrush") },
            label(usages[index], 12.0).apply { verticalAlignment = VerticalAlignment.Center })) }
    }))
}
