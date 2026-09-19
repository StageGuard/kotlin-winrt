package io.github.composefluent.winrt.gallery.motion

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.Popup
import microsoft.ui.xaml.media.animation.*
import microsoft.ui.xaml.shapes.*

@GalleryPage(route = "ThemeTransition", title = "Theme Transitions", group = "Motion", order = 5)
internal fun themeTransitionPage() = ExamplePage {
    val entrance = themeTransitionEntranceThemeTransitionAddingItemsToYourUISample()
    example("EntranceThemeTransition: Adding items to your UI.", entrance.first, entrance.second)

    val reposition = themeTransitionRepositionSample()
    example("RepositionThemeTransition: React to layout changes.", reposition.first, reposition.second)

    val refresh = themeTransitionContentRefreshSample()
    example("ContentThemeTransition: Animate content refreshes.", refresh.first, refresh.second)

    val addDelete = themeTransitionAddDeleteSample()
    example("AddDeleteThemeTransition: Animate adding and removing items.", addDelete.first, addDelete.second)

    example("PopupThemeTransition: Animate opening and closing a popup.", themeTransitionPopupSample())
}

@GallerySample(route = "ThemeTransition", title = "EntranceThemeTransition: Adding items to your UI.")
internal fun themeTransitionEntranceThemeTransitionAddingItemsToYourUISample() = run {
    val entrance = StackPanel().apply { this.spacing = 0.0; this.orientation = Orientation.Horizontal; childrenTransitions = TransitionCollection().apply { add(EntranceThemeTransition().apply { isStaggeringEnabled = true }) }
        repeat(5) { children.add(Rectangle().apply { width = 50.0; height = 50.0; margin = inset(5.0); fill = brush(0xADD8E6u) }) } }
    val actions = stack {
        listOf(1, 5).forEach { count -> children.add(Button().apply { this.content = if (count == 1) "Add one" else "Add five" }.also { galleryButton -> galleryButton.click.add { _, _ -> repeat(count) { entrance.children.add(Rectangle().apply { width = 50.0; height = 50.0; margin = inset(5.0); fill = brush(0xADD8E6u) }) }
            announce(entrance, "Added $count rectangles.", "EntranceAddNotificationId") } }) }
        children.add(Button().apply { this.content = "Clear all" }.also { galleryButton -> galleryButton.click.add { _, _ -> entrance.children.clear(); announce(entrance, "All rectangles cleared.", "EntranceClearNotificationId") } })
    }
    entrance to actions
}

@GallerySample(route = "ThemeTransition", title = "RepositionThemeTransition: React to layout changes.")
internal fun themeTransitionRepositionSample() = run {
    val middle = Rectangle().apply { width = 75.0; height = 75.0; margin = inset(5.0); fill = brush(0x008000u) }
    val grid = Grid().apply {
        repeat(3) { columnDefinitions.add(column(1.0, GridUnitType.Auto)) }
        children.add(Rectangle().apply { width = 75.0; height = 75.0; margin = inset(5.0); fill = brush(0xFF0000u) })
        Grid.setColumn(middle, 1); children.add(middle)
        children.add(Rectangle().apply { width = 75.0; height = 75.0; margin = inset(5.0); fill = brush(0x0000FFu); Grid.setColumn(this, 2); transitions = TransitionCollection().apply { add(RepositionThemeTransition()) } })
    }
    val reposition = Button().apply { this.content = "Reposition" }.also { galleryButton -> galleryButton.click.add { _, _ -> middle.visibility = if (middle.visibility == Visibility.Visible) Visibility.Collapsed else Visibility.Visible
        announce(middle, if (middle.visibility == Visibility.Visible) "Element restored." else "Element repositioned.", "RepositionNotificationId") } }
    grid to reposition
}

@GallerySample(route = "ThemeTransition", title = "ContentThemeTransition: Animate content refreshes.")
internal fun themeTransitionContentRefreshSample() = run {
    val list = ListView().apply {
        itemContainerTransitions = TransitionCollection().apply { add(ContentThemeTransition()) }
        itemsSource = List(5) { "Item $it" }
    }
    val refresh = Button().apply { this.content = "Refresh data" }.also { galleryButton -> galleryButton.click.add { _, _ -> list.itemsSource = List(5) { "Updated content $it" }
        announce(list, "Data refreshed.", "ContentRefreshNotificationId") } }
    list to refresh
}

@GallerySample(route = "ThemeTransition", title = "AddDeleteThemeTransition: Animate adding and removing items.")
internal fun themeTransitionAddDeleteSample() = run {
    var count = 10
    val list = ListView().apply {
        itemContainerTransitions = TransitionCollection().apply { add(AddDeleteThemeTransition()) }
        repeat(10) { items.add(ListViewItem().apply { content = "Item $it" }) }
    }
    fun addItem() { list.items.add(ListViewItem().apply { content = "New Item ${count++}" }) }
    fun deleteItem() { if (list.items.isNotEmpty()) list.items.removeAt(0) }
    val actions = stack {
        children.add(Button().apply { this.content = "Add" }.also { galleryButton -> galleryButton.click.add { _, _ -> addItem(); announce(list, "Item added.", "AddDeleteItemAddedNotificationId") } })
        children.add(Button().apply { this.content = "Delete" }.also { galleryButton -> galleryButton.click.add { _, _ -> deleteItem(); announce(list, "Item deleted.", "AddDeleteItemDeletedNotificationId") } })
        children.add(Button().apply { this.content = "Add and Del" }.also { galleryButton -> galleryButton.click.add { _, _ -> addItem(); deleteItem(); announce(list, "Item added and item deleted.", "AddDeleteBothNotificationId") } })
    }
    list to actions
}

@GallerySample(route = "ThemeTransition", title = "PopupThemeTransition: Animate opening and closing a popup.")
internal fun themeTransitionPopupSample() = run {
    val popup = Popup().apply { childTransitions = TransitionCollection().apply { add(PopupThemeTransition()) }; margin = inset(-75.0) }
    val show = Button().apply { content = "Show Popup" }
    val close = Button().apply { this.content = "Close Popup" }.also { galleryButton -> galleryButton.click.add { _, _ -> popup.isOpen = false; show.focus(FocusState.Programmatic) } }.apply { horizontalAlignment = HorizontalAlignment.Center }
    popup.child = Grid().apply {
        children.add(Ellipse().apply { width = 200.0; height = 200.0; fill = GalleryTheme.brush("FlyoutBackgroundThemeBrush"); stroke = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush"); strokeThickness = 2.0 })
        children.add(StackPanel().apply { this.spacing = 0.0; width = 152.0; margin = inset(24.0); verticalAlignment = VerticalAlignment.Center
            children.add(TextBlock().apply { this.text = "This is a popup using PopupThemeTransition"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(12.0); textAlignment = TextAlignment.Center })
            children.add(close) })
    }
    show.click.add { _, _ -> popup.xamlRoot = show.xamlRoot; popup.isOpen = true; close.focus(FocusState.Programmatic) }
    Grid().apply {
        children.add(show); children.add(popup)
        unloaded.add { _, _ -> popup.isOpen = false }
    }
}
