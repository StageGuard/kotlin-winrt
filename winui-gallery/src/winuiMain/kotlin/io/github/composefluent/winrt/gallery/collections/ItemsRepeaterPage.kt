package io.github.composefluent.winrt.gallery.collections

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.hosting.ElementCompositionPreview
import microsoft.ui.xaml.shapes.Ellipse
import microsoft.ui.xaml.shapes.Rectangle
import windows.system.VirtualKey
import kotlin.random.Random


@GalleryPage(route = "ItemsRepeater", title = "ItemsRepeater", group = "Collections", order = 2)
internal fun itemsRepeaterPage() = ExamplePage {
    val basicState = BasicItemsRepeaterState()
    val remove = Button("Remove Item")
    val basic = itemsRepeaterBasicSample(basicState, remove)
    example("Basic, non-interactive ItemsRepeater", repeaterScroll(basic, 200.0).apply {
        height = Double.NaN; maxHeight = 500.0
        horizontalScrollMode = ScrollMode.Auto; horizontalScrollBarVisibility = ScrollBarVisibility.Auto
    }, stack {
        children.add(Button("Add Item") { basicState.addRandomItem() })
        children.add(remove)
        children.add(choices("Layout", listOf("StackLayout - Vertical", "StackLayout - Horizontal", "UniformGridLayout")) {
            basicState.layoutMode = it; basicState.updateLayout()
        })
    })

    val feedState = ActivityFeedRepeaterState()
    val feed = itemsRepeaterActivityFeedSample(feedState)
    example("Virtualizing, scrollable list of items laid out by ItemsRepeater", repeaterScroll(feed, 400.0),
        choices("Layout", listOf("Uniform grid", "Custom virtualizing layout"), 1) {
            feed.layout = if (it == 0) UniformGridLayout().apply {
                minItemWidth = 108.0; minItemHeight = 108.0; minRowSpacing = 12.0; minColumnSpacing = 12.0
            } else feedState.activityLayout
        })

    val mixed = itemsRepeaterMixedTypeSample()
    example("An ItemsRepeater with a mixed-type collection", stack {
        children.add(label("This ItemsRepeater displays integer and string items. Its element factory chooses the layout for each item's type."))
        children.add(mixed)
    })

    example("Laying out nested ItemsRepeaters", itemsRepeaterNestedSample())
    example("Animated scrolling ItemsRepeater with content display", itemsRepeaterAnimatedSample())
    val recipeState = RecipeRepeaterState()
    val recipeFilter = TextBox().apply { header = "Filter by ingredient..."; width = 200.0 }
    val recipes = itemsRepeaterRecipeSample(recipeState, recipeFilter)
    example("Virtualized content-heavy layout with filtering and sorting", repeaterScroll(recipes, 600.0), stack {
        children.add(recipeFilter)
        children.add(label("Sort by number of ingredients"))
        children.add(Button("Least to most") { recipeState.sortDescending(false) })
        children.add(Button("Most to least") { recipeState.sortDescending(true) })
    })
}

// Ported from WinUI Gallery Samples/ItemsRepeater (MIT), with code-defined element factories.


internal val foodGroups = listOf(
    "Fruits" to listOf("Apricots", "Bananas", "Grapes", "Strawberries", "Watermelon", "Plums", "Blueberries"),
    "Vegetables" to listOf("Broccoli", "Spinach", "Sweet potato", "Cauliflower", "Onion", "Brussels sprouts", "Carrots"),
    "Grains" to listOf("Rice", "Quinoa", "Pasta", "Bread", "Farro", "Oats", "Barley"),
    "Proteins" to listOf("Steak", "Chicken", "Tofu", "Salmon", "Pork", "Chickpeas", "Eggs"),
)
internal val repeaterColors = listOf(
    "Blue" to 0xFF0000FFu, "BlueViolet" to 0xFF8A2BE2u, "Crimson" to 0xFFDC143Cu,
    "DarkCyan" to 0xFF008B8Bu, "DarkGoldenrod" to 0xFFB8860Bu, "DarkMagenta" to 0xFF8B008Bu,
    "DarkOliveGreen" to 0xFF556B2Fu, "DarkRed" to 0xFF8B0000u, "DarkSlateBlue" to 0xFF483D8Bu,
    "DeepPink" to 0xFFFF1493u, "IndianRed" to 0xFFCD5C5Cu, "MediumSlateBlue" to 0xFF7B68EEu,
    "Maroon" to 0xFF800000u, "MidnightBlue" to 0xFF191970u, "Peru" to 0xFFCD853Fu,
    "SaddleBrown" to 0xFF8B4513u, "SteelBlue" to 0xFF4682B4u, "OrangeRed" to 0xFFFF4500u,
    "Firebrick" to 0xFFB22222u, "DarkKhaki" to 0xFFBDB76Bu,
)

internal fun repeaterFactory(render: (Any?) -> UIElement) = GalleryRepeaterFactory(
    create = { Border() },
    bind = { container, value -> container.asWinRT<Border>().child = render(value) },
)

internal fun repeaterScroll(repeater: ItemsRepeater, viewportHeight: Double) = ScrollViewer().apply {
    content = repeater; height = viewportHeight
    isVerticalScrollChainingEnabled = false
    horizontalScrollBarVisibility = ScrollBarVisibility.Disabled
    horizontalScrollMode = ScrollMode.Disabled
}

internal fun repeaterTile(value: Any?, accent: Boolean, size: Double = 14.0) = Border().apply {
    background = GalleryTheme.brush(if (accent) "SystemControlBackgroundAccentBrush" else "SystemControlBackgroundChromeMediumBrush")
    child = label(value.toString(), size).apply {
        horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center
        if (accent) foreground = GalleryTheme.brush("SystemControlForegroundChromeWhiteBrush")
        textWrapping = TextWrapping.Wrap
    }
}

@GallerySample(route = "ItemsRepeater", title = "Animated scrolling ItemsRepeater with content display")
internal fun itemsRepeaterAnimatedSample() = run {
    val repeater = ItemsRepeater()
    val scroll = repeaterScroll(repeater, 175.0).apply { width = 250.0 }
    val preview = Rectangle().apply {
        width = 150.0; height = 150.0; margin = Thickness(10.0, 0.0, 0.0, 0.0)
        stroke = GalleryTheme.brush("SystemControlForegroundBaseHighBrush"); named(this, "ColorRectangle")
    }
    var previousFocus = -1
    var selectedIndex = -1
    var selectedButton: Button? = null
    repeater.apply {
        layout = StackLayout()
        itemTemplate = GalleryRepeaterFactory(create = {
            Button().apply {
            horizontalAlignment = HorizontalAlignment.Stretch
            foreground = GalleryTheme.brush("TextFillColorInverseBrush")
            click.add { _, _ ->
                val index = dataContext.toString().toInt()
                selectedButton?.let { named(it, it.content.toString()) }
                selectedIndex = index; selectedButton = this
                preview.fill = background; named(this, "${content}, selected")
                announce(this, "Rectangle color set to $content", "RectangleChangedNotificationActivityId")
            }
            gotFocus.add { _, _ ->
                previousFocus = repeater.getElementIndex(this)
                startBringIntoView(BringIntoViewOptions().apply { verticalAlignmentRatio = 0.5; animationDesired = true })
            }
            }
        }, bind = { element, value ->
            val index = value.toString().toInt()
            element.asWinRT<Button>().apply {
                content = repeaterColors[index].first; background = brush(repeaterColors[index].second)
                named(this, content.toString() + if (index == selectedIndex) ", selected" else "")
            }
        })
        itemsSource = repeaterColors.indices.toList()
    }
    repeater.elementPrepared.add { _, args ->
        val visual = ElementCompositionPreview.getElementVisual(checkNotNull(args.element))
        val scrollVisual = ElementCompositionPreview.getElementVisual(scroll)
        val properties = ElementCompositionPreview.getScrollViewerManipulationPropertySet(scroll)
        val scale = checkNotNull(visual.compositor).createExpressionAnimation("1 - abs((svVisual.Size.Y/2 - scrollProperties.Translation.Y) - (item.Offset.Y + item.Size.Y/2))*(.25/(svVisual.Size.Y/2))").apply {
            setReferenceParameter("svVisual", scrollVisual); setReferenceParameter("scrollProperties", properties); setReferenceParameter("item", visual)
        }
        visual.startAnimation("Scale.X", scale); visual.startAnimation("Scale.Y", scale)
        visual.startAnimation("CenterPoint", checkNotNull(visual.compositor).createExpressionAnimation("Vector3(item.Size.X/2, item.Size.Y/2, 0)").apply { setReferenceParameter("item", visual) })
    }
    repeater.elementClearing.add { _, args ->
        val visual = ElementCompositionPreview.getElementVisual(checkNotNull(args.element))
        visual.stopAnimation("Scale.X"); visual.stopAnimation("Scale.Y"); visual.stopAnimation("CenterPoint")
        if (args.element == selectedButton) selectedButton = null
    }
    repeater.gettingFocus.add { _, args ->
        val old = args.oldFocusedElement
        if (previousFocus >= 0 && old != null && repeater.getElementIndex(old.asWinRT<UIElement>()) == -1)
            repeater.tryGetElement(previousFocus)?.let { args.newFocusedElement = it }
    }
    repeater.keyDown.add { _, args ->
        val index = when (args.key) { VirtualKey.Home -> 0; VirtualKey.End -> repeaterColors.lastIndex; else -> -1 }
        if (!args.handled && index >= 0) {
            repeater.getOrCreateElement(index).asWinRT<Control>().apply { startBringIntoView(); focus(FocusState.Programmatic) }
            args.handled = true
        }
    }
    stack {
        orientation = Orientation.Horizontal; children.add(scroll); children.add(preview)
    }
}

private data class RepeaterRecipe(val number: Int, val color: UInt, val ingredients: List<String>)

internal class BasicItemsRepeaterState {
    val bars = mutableListOf(300, 25, 175)
    var layoutMode = 0
    var updateLayout: () -> Unit = {}
    var addRandomItem: () -> Unit = {}
}

internal class ActivityFeedRepeaterState {
    val activityLayout = GalleryActivityFeedLayout()
}

internal class RecipeRepeaterState {
    var sortDescending: (Boolean) -> Unit = {}
}

@GallerySample(route = "ItemsRepeater", title = "Basic, non-interactive ItemsRepeater")
internal fun itemsRepeaterBasicSample(state: BasicItemsRepeaterState, removeButton: Button) = ItemsRepeater().apply {
    fun updateLayout() {
        layout = when (state.layoutMode) {
            0 -> StackLayout().apply { spacing = 8.0 }
            1 -> StackLayout().apply { orientation = Orientation.Horizontal; spacing = 8.0 }
            else -> UniformGridLayout().apply { minRowSpacing = 8.0; minColumnSpacing = 8.0 }
        }
        maxWidth = when (state.layoutMode) { 0 -> 437.0; 1 -> 6000.0; else -> 540.0 }
        itemTemplate = repeaterFactory { value ->
            val length = value.toString().toDouble()
            if (state.layoutMode == 2) Grid().apply {
                children.add(Ellipse().apply {
                    width = 425.0 / 6; height = width
                    fill = GalleryTheme.brush("SystemControlBackgroundChromeLowBrush")
                })
                children.add(Ellipse().apply {
                    width = length / 6; height = width
                    fill = GalleryTheme.brush("SystemControlBackgroundAccentBrush")
                })
            } else Border().apply {
                background = GalleryTheme.brush("SystemControlBackgroundChromeLowBrush")
                if (state.layoutMode == 0) width = 425.0 else height = 425.0 / 4
                child = Rectangle().apply {
                    fill = GalleryTheme.brush("SystemControlBackgroundAccentBrush")
                    width = if (state.layoutMode == 0) length else 48.0
                    height = if (state.layoutMode == 0) 24.0 else length / 4
                    horizontalAlignment = HorizontalAlignment.Left
                    verticalAlignment = VerticalAlignment.Top
                }
            }
        }
        itemsSource = state.bars.toList()
    }
    state.updateLayout = ::updateLayout
    state.addRandomItem = {
        state.bars.add(Random.nextInt(425))
        updateLayout()
        removeButton.isEnabled = true
    }
    removeButton.click.add { _, _ ->
        if (state.bars.isNotEmpty()) state.bars.removeAt(0)
        updateLayout()
        removeButton.isEnabled = state.bars.isNotEmpty()
    }
    updateLayout()
}

@GallerySample(route = "ItemsRepeater", title = "Virtualizing, scrollable list of items laid out by ItemsRepeater")
internal fun itemsRepeaterActivityFeedSample(state: ActivityFeedRepeaterState) = ItemsRepeater().apply {
    margin = Thickness(0.0, 0.0, 12.0, 0.0)
    layout = state.activityLayout
    itemTemplate = repeaterFactory { value -> repeaterTile(value, value.toString().toInt() % 2 != 0) }
    itemsSource = (0 until 500).toList()
}

@GallerySample(route = "ItemsRepeater", title = "An ItemsRepeater with a mixed-type collection")
internal fun itemsRepeaterMixedTypeSample() = ItemsRepeater().apply {
    layout = UniformGridLayout().apply { minItemWidth = 200.0; minItemHeight = 200.0 }
    itemTemplate = repeaterFactory { value ->
        repeaterTile(value, value is String, if (value is String) 14.0 else 46.0).apply {
            margin = inset(10.0); padding = inset(10.0)
        }
    }
    itemsSource = listOf(
        64,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        128,
        "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
        256,
        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
        512,
        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        1024,
    )
}

@GallerySample(route = "ItemsRepeater", title = "Laying out nested ItemsRepeaters")
internal fun itemsRepeaterNestedSample() = ScrollViewer().apply {
    horizontalScrollMode = ScrollMode.Auto
    horizontalScrollBarVisibility = ScrollBarVisibility.Auto
    content = ItemsRepeater().apply {
        layout = StackLayout()
        itemTemplate = repeaterFactory { value ->
            val (name, food) = foodGroups[value.toString().toInt()]
            StackPanel().apply { this.spacing = 0.0; children.add(TextBlock().apply { this.text = name; this.fontSize = 28.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { padding = inset(8.0) })
                children.add(ItemsRepeater().apply {
                    layout = StackLayout().apply { orientation = Orientation.Horizontal }
                    itemTemplate = repeaterFactory { repeaterTile(it, true).apply { margin = inset(10.0); padding = inset(10.0) } }
                    itemsSource = food
                }) }
        }
        itemsSource = foodGroups.indices.toList()
    }
}

@GallerySample(route = "ItemsRepeater", title = "Virtualized content-heavy layout with filtering and sorting")
internal fun itemsRepeaterRecipeSample(state: RecipeRepeaterState, filter: TextBox) = ItemsRepeater().apply {
    val extras = listOf("Garlic", "Lemon", "Butter", "Lime", "Feta Cheese", "Parmesan Cheese", "Breadcrumbs")
    val recipes = List(1000) { index ->
        RepeaterRecipe(index, repeaterColors.random().second,
            foodGroups.map { it.second.random() } + extras.shuffled().take(Random.nextInt(4)))
    }
    var visible = recipes
    var descending = false
    val masonry = GalleryRecipeLayout()
    layout = masonry
    itemTemplate = repeaterFactory { value ->
        val recipe = visible[value.toString().toInt()]
        StackPanel().apply { this.spacing = 0.0; margin = inset(5.0)
            background = GalleryTheme.brush("SystemControlBackgroundBaseLowBrush")
            borderThickness = inset(1.0)
            children.add(Border().apply {
                height = 75.0; margin = inset(8.0); background = brush(recipe.color); opacity = 0.8
                child = TextBlock().apply { this.text = recipe.number.toString(); this.fontSize = 35.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply {
                    padding = inset(12.0); textAlignment = TextAlignment.Center
                    foreground = GalleryTheme.brush("SystemControlForegroundAltHighBrush")
                }
            })
            children.add(TextBlock().apply { this.text = "Recipe ${recipe.number}"; this.fontSize = 28.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(15.0, 0.0, 10.0, 0.0) })
            children.add(TextBlock().apply { this.text = recipe.ingredients.joinToString("\n", prefix = "\n"); this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply {
                margin = Thickness(15.0, 0.0, 15.0, 15.0)
            }) }
    }
    fun update() {
        visible = recipes.filter { recipe -> recipe.ingredients.any { it.contains(filter.text, ignoreCase = true) } }
            .let { filtered -> if (descending) filtered.sortedByDescending { it.ingredients.size } else filtered.sortedBy { it.ingredients.size } }
        masonry.reset()
        itemsSource = visible.indices.toList()
        announce(this, "Filtered recipes, ${visible.size} results.", "RecipesFilteredNotificationActivityId")
    }
    filter.textChanged.add { _, _ -> update() }
    state.sortDescending = { descending = it; update() }
    update()
}
