package io.github.composefluent.winrt.gallery.menusandtoolbars

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.input.*
import microsoft.ui.xaml.media.*
import windows.foundation.Point
import windows.foundation.Uri
import windows.system.VirtualKey
import windows.system.VirtualKeyModifiers

@GalleryPage(route = "SwipeControl", title = "SwipeControl", group = "MenusAndToolbars", order = 7)
internal fun swipeControlPage() = ExamplePage {
    var accepted = false
    var flagged = false
    val reveal = swipeControlSwipeRightToRevealActionsSample()
    val output = label("Swipe Right")
    reveal.content = output
    fun update() { output.text = "Swipe Right" + when { accepted && flagged -> " - Accepted & Flagged"; accepted -> " - Accepted"; flagged -> " - Flagged"; else -> "" } }
    val accept = swipeItem("Accept", "\uE8FB")
    accept.invoked.add { _, _ -> accepted = !accepted; accept.text = if (accepted) "Cancel" else "Accept"; accept.iconSource = FontIconSource().apply { glyph = if (accepted) "\uE711" else "\uE10B" }; update() }
    val flag = swipeItem("Flag", "\uE7C1")
    flag.invoked.add { _, _ -> flagged = !flagged; flag.text = if (flagged) "Unmark" else "Flag"; update() }
    reveal.leftItems = SwipeItems().apply { mode = SwipeMode.Reveal; add(accept); add(flag) }
    example("Swipe right to reveal actions.", reveal)
    val archiveOutput = label("Swipe Left")
    example("Swipe left to execute an action.", swipeControlSwipeLeftToExecuteSample(archiveOutput))
    val list = swipeControlCustomSwipeActionsInAListViewSample2()
    repeat(4) { index ->
        val item = ListViewItem().apply { padding = inset(0.0); horizontalContentAlignment = HorizontalAlignment.Stretch }
        item.content = swipe("Swipe Item ${index + 1}").apply {
            leftItems = SwipeItems().apply { mode = SwipeMode.Reveal; add(swipeItem("Reply All", "\uE8C2").apply { background = brush(0x3E6FA7u) }); add(swipeItem("Open", "\uE8C3").apply { background = brush(0xFF9501u) }) }
            rightItems = SwipeItems().apply { mode = SwipeMode.Execute; add(swipeItem("Delete", "\uE74D") { list.items.remove(item) }.apply { background = brush(0xFF0000u) }) }
        }
        list.items.add(item)
    }
    example("Custom swipe actions in a ListView.", list)
    example("A SwipeControl with a gradient background.", swipeControlSwipeControlWithAGradientBackgroundSample3())
    example("A SwipeControl with a custom icon.", swipeControlSwipeControlWithACustomIconSample4())

}

@GallerySample(route = "SwipeControl", title = "A SwipeControl with a gradient background.")
internal fun swipeControlSwipeControlWithAGradientBackgroundSample3() = SwipeControl().apply {
        width = 500.0; height = 68.0; margin = inset(12.0); borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ButtonBackground")
        content = TextBlock().apply { this.text = "Swipe Left"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(12.0); horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
        rightItems = SwipeItems().apply { mode = SwipeMode.Execute; add(swipeItem("Lock", "\uE72E").apply {
            background = LinearGradientBrush().apply {
                startPoint = Point(0f, 0.5f); endPoint = Point(1f, 0.5f)
                listOf(0x8990F9u, 0x5B66FBu, 0x5C1DF4u).forEachIndexed { index, value -> gradientStops.add(GradientStop().apply { color = rgb(value); offset = index / 2.0 }) }
            }
        }) }
    }

@GallerySample(route = "SwipeControl", title = "A SwipeControl with a custom icon.")
internal fun swipeControlSwipeControlWithACustomIconSample4() = SwipeControl().apply {
        width = 500.0; height = 68.0; margin = inset(12.0); borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ButtonBackground")
        content = TextBlock().apply { this.text = "Swipe Right"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(12.0); horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
        leftItems = SwipeItems().apply { mode = SwipeMode.Reveal; add(SwipeItem().apply { text = "Coffee"; iconSource = BitmapIconSource().apply { uriSource = Uri("ms-appx:///Assets/SampleMedia/CoffeeCup.png") } }) }
    }

// Ported from WinUI Gallery Samples/{CommandBar,CommandBarFlyout,StandardUICommand,XamlUICommand,SwipeControl} (MIT).


internal fun swipeItem(title: String, code: String, action: () -> Unit = {}) = SwipeItem().apply {
    text = title; iconSource = FontIconSource().apply { glyph = code }; invoked.add { _, _ -> action() }
}
internal fun swipe(contentText: String) = SwipeControl().apply {
    width = 500.0; height = 68.0; margin = inset(12.0); borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ButtonBackground")
    content = label(contentText).apply { margin = inset(12.0); horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
}

@GallerySample(route = "SwipeControl", title = "Swipe right to reveal actions.")
internal fun swipeControlSwipeRightToRevealActionsSample() = SwipeControl().apply {
    width = 500.0; height = 68.0; margin = inset(12.0); borderThickness = inset(1.0); borderBrush = GalleryTheme.brush("ButtonBackground")
    content = TextBlock().apply { this.text = "Swipe Right"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(12.0); horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center }
}

@GallerySample(route = "SwipeControl", title = "Swipe left to execute an action.")
internal fun swipeControlSwipeLeftToExecuteSample(output: TextBlock) = SwipeControl().apply {
    width = 500.0
    height = 68.0
    margin = inset(12.0)
    borderThickness = inset(1.0)
    borderBrush = GalleryTheme.brush("ButtonBackground")
    content = output.apply {
        text = "Swipe Left"
        margin = inset(12.0)
        horizontalAlignment = HorizontalAlignment.Center
        verticalAlignment = VerticalAlignment.Center
    }
    var archived = false
    rightItems = SwipeItems().apply {
        mode = SwipeMode.Execute
        add(swipeItem("Archive", "\uE7B8") {
            archived = !archived
            output.text = if (archived) "Archived - Swipe Left" else "Swipe Left"
        }.apply { behaviorOnInvoked = SwipeBehaviorOnInvoked.Close })
    }
}

@GallerySample(route = "SwipeControl", title = "Custom swipe actions in a ListView.")
internal fun swipeControlCustomSwipeActionsInAListViewSample2() = ListView().apply { width = 800.0; height = 300.0; minWidth = 200.0 }
