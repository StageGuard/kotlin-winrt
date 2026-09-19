package io.github.composefluent.winrt.gallery.dialogsandflyouts

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.Popup

@GalleryPage(route = "Popup", title = "Popup", group = "DialogsAndFlyouts", order = 2)
internal fun popupPage() = ExamplePage {
    val lightDismiss = ToggleSwitch().apply {
        header = "IsLightDismissEnabled"; isOn = true; offContent = "False"; onContent = "True"
    }
    val popup = popupOffsetSample(lightDismiss)
    lightDismiss.toggled.add { _, _ -> popup.isLightDismissEnabled = lightDismiss.isOn }

    example("A Popup with offset positioning.", Grid().apply {
        children.add(Button("Show Popup (using Offset)") { popup.isOpen = true; lightDismiss.isEnabled = false })
        children.add(popup)
    }, stack {
        children.add(lightDismiss)
        fun offset(title: String, initial: Double, maximum: Double, changed: (Double) -> Unit) {
            children.add(NumberBox().apply {
                header = title; value = initial; minimum = -100.0; this.maximum = maximum; smallChange = 10.0; largeChange = 100.0
                spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
                valueChanged.add { _, _ -> if (value.isFinite()) changed(value) }
            })
        }
        offset("VerticalOffset", 0.0, 100.0) { popup.verticalOffset = it }
        offset("HorizontalOffset", 200.0, 500.0) { popup.horizontalOffset = it }
    })
}

@GallerySample(route = "Popup", title = "A Popup with offset positioning.")
internal fun popupOffsetSample(lightDismiss: ToggleSwitch) = Popup().apply {
    val popup = this
    horizontalOffset = 200.0
    isLightDismissEnabled = true
    closed.add { _, _ -> lightDismiss.isEnabled = true }
    child = Grid().apply {
        minWidth = 240.0; padding = inset(16.0); cornerRadius = corners(8.0)
        background = GalleryTheme.brush("AcrylicBackgroundFillColorDefaultBrush")
        borderBrush = GalleryTheme.brush("SurfaceStrokeColorDefaultBrush"); borderThickness = inset(1.0)
        children.add(StackPanel().apply { this.spacing = 8.0; children.add(TextBlock().apply { this.text = "Simple Popup"; this.fontSize = 16.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(Button().apply { this.content = "Close" }.also { galleryButton -> galleryButton.click.add { _, _ -> popup.isOpen = false } }) })
    }
}
