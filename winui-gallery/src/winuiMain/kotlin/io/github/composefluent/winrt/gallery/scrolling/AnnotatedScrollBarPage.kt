package io.github.composefluent.winrt.gallery.scrolling

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "AnnotatedScrollBar", title = "AnnotatedScrollBar", group = "Scrolling", order = 0)
internal fun annotatedScrollBarPage() = ExamplePage {
    val repeater = ItemsRepeater().apply {
        margin = inset(2.0); layout = UniformGridLayout()
        itemsSource = annotatedScrollCounts.flatMapIndexed { index, count -> List(count) { index } }
        itemTemplate = GalleryElementFactory { data -> Grid().apply {
            width = 112.0; height = 82.0; margin = inset(4.0); cornerRadius = corners(4.0)
            background = brush(annotatedScrollColors[data.toString().toInt()])
        } }
    }
    val bar = annotatedScrollBarSample(repeater)
    val view = ScrollView().apply {
        maxWidth = 800.0; maxHeight = 500.0; background = brush(0xD3D3D3u); verticalScrollBarVisibility = ScrollingScrollBarVisibility.Hidden; content = repeater
        loaded.add { _, _ -> checkNotNull(scrollPresenter).verticalScrollController = bar.scrollController }
    }

    example("AnnotatedScrollBar linked to a ScrollView.", Grid().apply {
        columnDefinitions.add(column(1.0, GridUnitType.Star)); columnDefinitions.add(column(1.0, GridUnitType.Auto))
        children.add(view); Grid.setColumn(bar, 1); children.add(bar)
    }, stack {
        minWidth = 200.0
        children.add(label("Changing the AnnotatedScrollBar height refreshes its Labels layout."))
        children.add(range("AnnotatedScrollBar maximum height:", 500.0, 100.0, 500.0) { bar.maxHeight = it })
    })
}

private val annotatedScrollColors = listOf(0xF0FFFFu, 0xDC143Cu, 0x00FFFFu, 0xFF00FFu, 0xFFD700u)
private val annotatedScrollCounts = listOf(32, 50, 8, 70, 90)

@GallerySample(route = "AnnotatedScrollBar", title = "AnnotatedScrollBar linked to a ScrollView.")
internal fun annotatedScrollBarSample(repeater: ItemsRepeater) = AnnotatedScrollBar().apply {
    val names = listOf("Azure", "Crimson", "Cyan", "Fuchsia", "Gold")
    val counts = listOf(32, 50, 8, 70, 90)
    val starts = listOf(0, 32, 82, 90, 160)
    fun offset(index: Int): Double =
        (90 * (index / (repeater.actualWidth / 120.0).toInt().coerceAtLeast(1))).toDouble()
    maxHeight = 500.0
    margin = Thickness(4.0, 0.0, 48.0, 0.0)
    horizontalAlignment = HorizontalAlignment.Right
    repeater.sizeChanged.add { _, _ ->
        labels.clear()
        names.forEachIndexed { index, name ->
            labels.add(AnnotatedScrollBarLabel(name, offset(starts[index])))
        }
    }
    detailLabelRequested.add { _, args ->
        args.content = names[(0..3).firstOrNull {
            args.scrollOffset <= offset(starts[it] + counts[it] - 1)
        } ?: 4]
    }
}
