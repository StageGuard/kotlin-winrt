package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import microsoft.ui.text.MarkerType
import windows.foundation.Uri

@GalleryPage(route = "DropDownButton", title = "DropDownButton", group = "BasicInput", order = 1)
internal fun dropDownButtonPage() = ExamplePage {
    example("A simple DropDownButton.", dropDownButtonBasicSample())
    example("A DropDownButton with icons.", dropDownButtonIconSample())
}

@GallerySample(route = "DropDownButton", title = "A simple DropDownButton.")
internal fun dropDownButtonBasicSample() = DropDownButton().apply {
    content = "Email"
    named(this, "Email")
    flyout = MenuFlyout().apply {
        placement = FlyoutPlacementMode.BottomEdgeAlignedLeft
        listOf("Send", "Reply", "Reply All").forEach { title ->
            items.add(MenuFlyoutItem().apply { text = title })
        }
    }
}

@GallerySample(route = "DropDownButton", title = "A DropDownButton with icons.")
internal fun dropDownButtonIconSample() = DropDownButton().apply {
    content = FontIcon().apply { this.glyph = "\uE715"; this.fontSize = 16.0 }
    named(this, "Email")
    flyout = MenuFlyout().apply {
        placement = FlyoutPlacementMode.BottomEdgeAlignedLeft
        listOf("Send" to "\uE725", "Reply" to "\uE8CA", "Reply All" to "\uE8C2").forEach { (title, code) ->
            items.add(MenuFlyoutItem().apply { text = title; icon = FontIcon().apply { this.glyph = code; this.fontSize = 16.0 } })
        }
    }
}
