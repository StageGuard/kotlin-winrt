package io.github.composefluent.winrt.gallery.system

import io.github.composefluent.winrt.gallery.*
import kotlin.time.Duration.Companion.seconds
import microsoft.ui.composition.*
import microsoft.ui.content.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.hosting.ElementCompositionPreview
import microsoft.ui.xaml.shapes.Rectangle
import windows.foundation.Point
import windows.foundation.numerics.*

@GalleryPage(route = "ContentIsland", title = "ContentIsland", group = "System", order = 1)
internal fun contentIslandPage() = ExamplePage {
    val tasks = GalleryPageTasks(this)
    example("Basic ContentIsland content.", contentIslandSample(this, tasks))
}

@GallerySample(route = "ContentIsland", title = "Basic ContentIsland content.")
internal fun contentIslandSample(owner: FrameworkElement, tasks: GalleryPageTasks) = StackPanel().apply { this.spacing = 8.0; val cleanup = mutableListOf<() -> Unit>()
    val cards = List(6) { Rectangle().apply {
        width = 400.0; height = 400.0; margin = inset(8.0); radiusX = 8.0; radiusY = 8.0
        fill = GalleryTheme.brush("CardBackgroundFillColorDefaultBrush"); stroke = GalleryTheme.brush("CardStrokeColorDefaultBrush"); strokeThickness = 1.0
    } }
    var next = 0
    val load = Button().apply {
        content = "Load model"
        style = controlStyle("AccentButtonStyle")
    }
    load.click.add { _, _ ->
        tasks.launch {
            if (next >= cards.size) return@launch
            val rectangle = cards[next]
            val xamlRoot = checkNotNull(owner.xamlRoot)
            // Follow the ContentIsland hosting guide: the link owns a dedicated
            // placement visual, attached beneath the placeholder so its scrolling
            // and clipping still follow the XAML element.
            // https://learn.microsoft.com/windows/apps/develop/composition/content-island
            val compositor = checkNotNull(ElementCompositionPreview.getElementVisual(rectangle).compositor)
            val placement = compositor.createContainerVisual()
            ElementCompositionPreview.setElementChildVisual(rectangle, placement)
            val link = ChildSiteLink.create(checkNotNull(xamlRoot.contentIsland), placement)
            var model: ContainerVisual? = null
            var island: ContentIsland? = null
            var attached = false
            try {
                val createdModel = proceduralHelmet(compositor)
                model = createdModel
                val createdIsland = ContentIsland.create(createdModel)
                island = createdIsland
                fun updatePlacement() {
                    val offset = rectangle.transformToVisual(checkNotNull(xamlRoot.content)).transformPoint(Point(0f, 0f))
                    link.localToParentTransformMatrix = Matrix4x4(
                        1f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f,
                        0f, 0f, 1f, 0f,
                        offset.x, offset.y, 0f, 1f,
                    )
                    placement.size = rectangle.actualSize
                    link.actualSize = rectangle.actualSize
                }
                updatePlacement()
                link.connect(createdIsland)
                val token = rectangle.layoutUpdated.add { _, _ -> updatePlacement() }
                cleanup.add {
                    rectangle.layoutUpdated.remove(token)
                    try { link.close() } finally { try { createdIsland.close() } finally { createdModel.close() } }
                }
                attached = true
                next++; load.isEnabled = next < cards.size
            } finally {
                if (!attached) {
                    try { link.close() } finally { try { island?.close() } finally { model?.close() } }
                }
            }
        }
    }

    children.add(TextBlock().apply { this.text = "Here's a ContentIsland in a ScrollViewer. Notice how the content scrolls and clips correctly."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    children.add(load)
    children.add(VariableSizedWrapGrid().apply {
        orientation = Orientation.Horizontal
        itemWidth = 416.0; itemHeight = 416.0
        maximumRowsOrColumns = 1
        horizontalAlignment = HorizontalAlignment.Stretch
        cards.forEach { children.add(it) }
        fun updateColumns() {
            maximumRowsOrColumns = (actualWidth / itemWidth).toInt().coerceAtLeast(1)
        }
        sizeChanged.add { _, _ -> updateColumns() }
        loaded.add { _, _ -> updateColumns() }
    })
    unloaded.add { _, _ -> cleanup.toList().forEach { it() }; cleanup.clear(); next = 0; load.isEnabled = true } }

// Host layout and ChildSiteLink lifecycle ported from WinUI Gallery (MIT).
// The helmet below is original procedural geometry. The upstream DamagedHelmet
// asset has a CC BY-NC predecessor, so none of its model or texture data is used.


internal fun proceduralHelmet(compositor: Compositor): ContainerVisual {
    val root = compositor.createContainerVisual().apply { size = Vector2(400f, 400f) }
    val helmet = compositor.createContainerVisual().apply {
        size = Vector2(280f, 300f); offset = Vector3(60f, 50f, 0f); centerPoint = Vector3(140f, 150f, 0f); rotationAxis = Vector3(0f, 1f, 0f)
    }
    fun plate(x: Float, y: Float, width: Float, height: Float, radius: Float, color: UInt, z: Float) {
        val shape = compositor.createRoundedRectangleGeometry().apply { size = Vector2(width, height); cornerRadius = Vector2(radius, radius) }
        checkNotNull(helmet.children).insertAtTop(compositor.createShapeVisual().apply {
            size = Vector2(width, height); offset = Vector3(x, y, z)
            checkNotNull(shapes).add(compositor.createSpriteShape(shape).apply { fillBrush = compositor.createColorBrush(rgb(color)) })
        })
    }
    // Layered shell gives the original procedural model depth under rotation.
    for (layer in -15..15) {
        val depth = layer.toFloat() * 2f
        val inset = (layer * layer).toFloat() / 15f
        val shade = (92 + layer * 2).coerceIn(0, 255).toUInt()
        plate(30f + inset, 18f + inset / 2, 220f - inset * 2, 264f - inset, 76f, (shade shl 16) or ((shade + 8u) shl 8) or (shade + 14u), depth)
    }
    plate(47f, 80f, 186f, 100f, 32f, 0x172A35u, 33f)
    plate(55f, 87f, 170f, 79f, 27f, 0x46758Au, 35f)
    plate(63f, 92f, 145f, 18f, 9f, 0x91BBC6u, 36f)
    plate(85f, 187f, 110f, 65f, 18f, 0x343D43u, 34f)
    repeat(4) { index -> plate(97f, 198f + index * 11, 86f, 5f, 2f, 0x172229u, 36f) }
    plate(18f, 110f, 35f, 74f, 15f, 0x323C44u, 3f)
    plate(227f, 110f, 35f, 74f, 15f, 0x323C44u, 3f)
    checkNotNull(root.children).insertAtTop(helmet)
    helmet.startAnimation("RotationAngleInDegrees", compositor.createScalarKeyFrameAnimation().apply {
        insertKeyFrame(0f, 0f); insertKeyFrame(0.5f, 360f); insertKeyFrame(1f, 0f); duration = 15.seconds; iterationBehavior = AnimationIterationBehavior.Forever
    })
    return root
}
