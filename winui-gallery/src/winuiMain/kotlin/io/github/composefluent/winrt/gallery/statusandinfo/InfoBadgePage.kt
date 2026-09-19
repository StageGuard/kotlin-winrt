package io.github.composefluent.winrt.gallery.statusandinfo

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.PlacementMode
import windows.foundation.Rect
import windows.foundation.Uri

@GalleryPage(route = "InfoBadge", title = "InfoBadge", group = "StatusAndInfo", order = 0)
internal fun infoBadgePage() = ExamplePage {
    val badge = InfoBadge().apply { value = 5 }

    val navigation = infoBadgeNavigationSample(badge)
    example("An InfoBadge embedded in a NavigationView.", navigation, stack {
        children.add(ToggleSwitch().apply { header = "InfoBadge Opacity"; isOn = true; toggled.add { _, _ -> badge.opacity = if (isOn) 1.0 else 0.0 } })
        children.add(select("Display Mode", listOf("LeftExpanded", "LeftCompact", "Top")) {
            navigation.paneDisplayMode = listOf(NavigationViewPaneDisplayMode.Left, NavigationViewPaneDisplayMode.LeftCompact, NavigationViewPaneDisplayMode.Top)[it]
            navigation.isPaneOpen = it != 1
        })
    })
    val icon = InfoBadge()
    val value = InfoBadge().apply { this.value = 10 }
    val dot = InfoBadge()
    fun applyStyles(prefix: String) {
        icon.style = controlStyle("${prefix}IconInfoBadgeStyle")
        value.style = controlStyle("${prefix}ValueInfoBadgeStyle")
        dot.style = controlStyle("${prefix}DotInfoBadgeStyle")
    }
    applyStyles("Attention")
    val styles = listOf("Attention", "Informational", "Success", "Critical")

    example("Different InfoBadge styles.", infoBadgeStylesSample(icon, value, dot),
        select("Styles", styles) { applyStyles(styles[it]) })
    example("Placing an InfoBadge inside another control.", infoBadgePlacingAnInfoBadgeInsideAnotherControlSample2())

    val dynamic = infoBadgeDynamicSample()
    example("An InfoBadge with a dynamic value.", dynamic, NumberBox().apply {
        header = "InfoBadge Value"; minimum = -1.0; this.value = 1.0; spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
        valueChanged.add { _, _ -> if (this.value.isFinite()) dynamic.value = this.value.toInt() }
    })



}

@GallerySample(route = "InfoBadge", title = "An InfoBadge embedded in a NavigationView.")
internal fun infoBadgeNavigationSample(badge: InfoBadge) = NavigationView().apply {
    height = 300.0; paneDisplayMode = NavigationViewPaneDisplayMode.Left
    listOf("Home" to Symbol.Home, "Account" to Symbol.Contact, "Inbox" to Symbol.Mail).forEach { (title, symbol) ->
        menuItems.add(NavigationViewItem().apply {
            content = title; icon = SymbolIcon(symbol)
            if (title == "Inbox") { infoBadge = badge; named(this, "Inbox, 5 notifications") }
        })
    }
}

@GallerySample(route = "InfoBadge", title = "Different InfoBadge styles.")
internal fun infoBadgeStylesSample(icon: InfoBadge, value: InfoBadge, dot: InfoBadge) = StackPanel().apply { this.spacing = 20.0; this.orientation = Orientation.Horizontal; children.add(icon); children.add(value); children.add(dot) }

@GallerySample(route = "InfoBadge", title = "An InfoBadge with a dynamic value.")
internal fun infoBadgeDynamicSample() = InfoBadge().apply { value = 1 }

@GallerySample(route = "InfoBadge", title = "Placing an InfoBadge inside another control.")
internal fun infoBadgePlacingAnInfoBadgeInsideAnotherControlSample2() = Button().apply {
        width = 200.0; height = 60.0; padding = inset(0.0)
        horizontalContentAlignment = HorizontalAlignment.Stretch; verticalContentAlignment = VerticalAlignment.Stretch
        ToolTipService.setToolTip(this, "Refresh required")
        content = Grid().apply {
            children.add(SymbolIcon(Symbol.Sync))
            children.add(InfoBadge().apply {
                horizontalAlignment = HorizontalAlignment.Right; verticalAlignment = VerticalAlignment.Top
                background = brush(0xC42B1Cu); iconSource = FontIconSource().apply { glyph = "\uF13C" }
            })
        }
    }
