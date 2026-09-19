package io.github.composefluent.winrt.gallery.navigation

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.animation.SlideNavigationTransitionInfo
import microsoft.ui.xaml.media.animation.SlideNavigationTransitionEffect

@GalleryPage(route = "SelectorBar", title = "SelectorBar", group = "Navigation", order = 3)
internal fun selectorBarPage() = ExamplePage {
    example("A basic SelectorBar.", basicSelectorBarSample())
    example("A SelectorBar that switches between pages.", switchingSelectorBarSample())
    example("A SelectorBar displaying different collections.", selectorBarCollectionsSample())
}

@GallerySample(route = "SelectorBar", title = "A basic SelectorBar.")
internal fun basicSelectorBarSample() = SelectorBar().apply {
    listOf("Recent" to Symbol.Clock, "Shared" to Symbol.Share, "Favorites" to Symbol.Favorite).forEach { (title, symbol) ->
        items.add(SelectorBarItem().apply { text = title; icon = SymbolIcon(symbol) })
    }
}

@GallerySample(route = "SelectorBar", title = "A SelectorBar that switches between pages.")
internal fun switchingSelectorBarSample() = stack {
    val host = sampleFrame().apply { isNavigationStackEnabled = false; navigate(Page::class, 1) }
    var previous = 0
    children.add(SelectorBar().apply {
        (1..5).forEach { number -> items.add(SelectorBarItem().apply { text = "Page$number"; isSelected = number == 1 }) }
        selectionChanged.add { _, _ -> selectedItem?.let { selected ->
            val index = items.indexOf(selected)
            host.navigate(Page::class, index + 1, SlideNavigationTransitionInfo().apply {
                effect = if (index > previous) SlideNavigationTransitionEffect.FromRight else SlideNavigationTransitionEffect.FromLeft
            })
            previous = index
        } }
    })
    children.add(host)
}

@GallerySample(route = "SelectorBar", title = "A SelectorBar displaying different collections.")
internal fun selectorBarCollectionsSample() = stack {
    val colors = ItemsView().apply {
        layout = StackLayout().apply { orientation = Orientation.Horizontal }
        itemTemplate = GalleryRepeaterFactory(create = { ItemContainer() }, bind = { element, value ->
            element.asWinRT<ItemContainer>().apply {
                width = 112.0; height = 82.0; margin = inset(4.0)
                background = value!!.asWinRT<microsoft.ui.xaml.media.Brush>()
            }
        })
    }
    fun update(index: Int) {
        colors.itemsSource = List(listOf(5, 7, 4)[index]) { brush(listOf(0xFFC0CBu, 0xDDA0DDu, 0xB0E0E6u)[index]) }
    }
    update(0)
    val names = listOf("Pink", "Plum", "PowderBlue")
    children.add(SelectorBar().apply {
        names.forEachIndexed { index, title -> items.add(SelectorBarItem().apply { text = title; isSelected = index == 0 }) }
        selectionChanged.add { _, _ -> names.indexOf(selectedItem?.text).takeIf { it >= 0 }?.let(::update) }
    })
    children.add(colors)
}
