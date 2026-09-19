// TokenViewSelectorBarItemStyle from WinUI Gallery Styles/SelectorBar.xaml (MIT).
package io.github.composefluent.winrt.gallery

import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*

/** Retains SelectorBar selection and automation while applying its Gallery token visuals. */
class GalleryTokenSelectorItem : SelectorBarItem() {
    init {
        padding = Thickness(23.0, 5.0, 23.0, 6.0)
        cornerRadius = corners(16.0)
        borderThickness = inset(1.0)
        horizontalAlignment = HorizontalAlignment.Left
        verticalAlignment = VerticalAlignment.Center
        background = GalleryTheme.brush("ControlFillColorDefaultBrush")
        foreground = GalleryTheme.brush("TextFillColorPrimaryBrush")
        borderBrush = GalleryTheme.brush("ControlStrokeColorDefaultBrush")
    }

    /** Applies the Gallery token colors without relying on a XAML resource dictionary. */
    internal fun applyTokenState(selected: Boolean) {
        background = GalleryTheme.brush(if (selected) "AccentFillColorDefaultBrush" else "ControlFillColorDefaultBrush")
        foreground = GalleryTheme.brush(if (selected) "TextOnAccentFillColorPrimaryBrush" else "TextFillColorPrimaryBrush")
        borderBrush = GalleryTheme.brush(if (selected) "AccentFillColorDefaultBrush" else "ControlStrokeColorDefaultBrush")
    }
}
