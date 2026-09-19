package io.github.composefluent.winrt.gallery.statusandinfo

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.PlacementMode
import windows.foundation.Rect
import windows.foundation.Uri

@GalleryPage(route = "ToolTip", title = "ToolTip", group = "StatusAndInfo", order = 4)
internal fun toolTipPage() = ExamplePage {
    example("A Button with a simple ToolTip.", toolTipButtonWithASimpleToolTipSample())
    example("A TextBlock with an offset ToolTip.", toolTipTextBlockWithAnOffsetToolTipSample1())
    example("An Image with a ToolTip placement rectangle.", toolTipImageWithAToolTipPlacementRectangleSample2())
}

@GallerySample(route = "ToolTip", title = "A Button with a simple ToolTip.")
internal fun toolTipButtonWithASimpleToolTipSample() = Button().apply {
        content = "Button with a simple ToolTip."
        ToolTipService.setToolTip(this, "Simple ToolTip")
    }

@GallerySample(route = "ToolTip", title = "A TextBlock with an offset ToolTip.")
internal fun toolTipTextBlockWithAnOffsetToolTipSample1() = TextBlock().apply { this.text = "TextBlock with an offset ToolTip."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply {
        ToolTipService.setToolTip(this, ToolTip().apply { content = "Offset ToolTip."; verticalOffset = -80.0 })
    }

@GallerySample(route = "ToolTip", title = "An Image with a ToolTip placement rectangle.")
internal fun toolTipImageWithAToolTipPlacementRectangleSample2() = Image().apply { this.width = 400.0; this.height = 400.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/cliff.jpg") ) }.apply {
        height = 266.0
        ToolTipService.setToolTip(this, ToolTip().apply {
            content = "Non-occluding ToolTip."; placement = PlacementMode.Right
            placementRect = Rect(0f, 0f, 400f, 266f)
        })
    }
