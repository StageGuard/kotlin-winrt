package io.github.composefluent.winrt.gallery.motion

import io.github.composefluent.winrt.gallery.*
import kotlin.time.Duration.Companion.milliseconds
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.Popup
import microsoft.ui.xaml.media.CompositionTarget
import microsoft.ui.xaml.shapes.*
import windows.foundation.numerics.Vector3

@GalleryPage(route = "XamlCompInterop", title = "Animation interop", group = "Motion", order = 0)
internal fun compositionInteropPage() = ExamplePage {
    var damping = 0.6f
    var period = 50.0
    example("Natural motion composition animation.", xamlCompInteropNaturalMotionCompositionAnimationSample({ damping }, { period }), stack {
        children.add(choices("Damping Ratio", listOf("0.2", "0.4", "0.6", "0.8"), 2) { damping = (it + 1) * 0.2f })
        children.add(range("Period (in ms)", 50.0, 25.0, 200.0) { period = it }.apply { stepFrequency = 25.0; tickFrequency = 25.0 })
    })
    example("ExpressionAnimation: animate an ellipse relative to another element.", xamlCompInteropExpressionEllipseSample())
    example("Driving several related animations.", xamlCompInteropRelatedAnimationsSample())
    val responsive = xamlCompInteropResponsiveLayoutSample()
    example("Reference ActualSize in ExpressionAnimations to make a responsive layout.", responsive.first, responsive.second)
    val positionedPopup = xamlCompInteropPopupPositionSample()
    example("Reference ActualOffset and ActualSize in ExpressionAnimations.", positionedPopup.first, positionedPopup.second)
}

@GallerySample(route = "XamlCompInterop", title = "Natural motion composition animation.")
internal fun xamlCompInteropNaturalMotionCompositionAnimationSample(dampingRatio: () -> Float, periodMilliseconds: () -> Double) = stack {
    val compositor = CompositionTarget.getCompositorForCurrentThread()
    fun hover(element: UIElement) {
        fun animate(scale: Float) {
            element.startAnimation(compositor.createSpringVector3Animation().apply {
                target = "Scale"; finalValue = Vector3(scale, scale, scale); this.dampingRatio = dampingRatio(); period = periodMilliseconds().milliseconds
            })
        }
        element.pointerEntered.add { _, _ -> animate(1.5f) }
        element.pointerExited.add { _, _ -> animate(1f) }
    }
    children.add(TextBlock().apply { this.text = "Hover over the button to animate its scale."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    children.add(Button().apply { width = 100.0; height = 50.0; content = "Item"; hover(this) })
}

@GallerySample(route = "XamlCompInterop", title = "ExpressionAnimation: animate an ellipse relative to another element.")
internal fun xamlCompInteropExpressionEllipseSample() = run {
    val compositor = CompositionTarget.getCompositorForCurrentThread()
    val rectangle = Rectangle().apply {
        width = 50.0; height = 50.0; fill = GalleryTheme.brush("AccentFillColorDefaultBrush")
        fun animate(scale: Float) {
            startAnimation(compositor.createSpringVector3Animation().apply {
                target = "Scale"; finalValue = Vector3(scale, scale, scale); dampingRatio = 0.6f; period = 50.0.milliseconds
            })
        }
        pointerEntered.add { _, _ -> animate(1.5f) }; pointerExited.add { _, _ -> animate(1f) }
    }
    val ellipse = Ellipse().apply {
        width = 50.0; height = 50.0; margin = Thickness(55.0, 0.0, 55.0, 0.0); fill = GalleryTheme.brush("AccentFillColorDefaultBrush")
        loaded.add { _, _ -> startAnimation(compositor.createExpressionAnimation().apply {
            expression = "Vector3(1/scaleElement.Scale.X, 1/scaleElement.Scale.Y, 1)"; target = "Scale"; setExpressionReferenceParameter("scaleElement", rectangle)
        }) }
        unloaded.add { _, _ -> stopAnimation(compositor.createExpressionAnimation().apply { target = "Scale" }) }
    }
    stack {
        height = 200.0
        children.add(TextBlock().apply { this.text = "Hover over the square to animate its scale. Notice that the ellipse also animates."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(TextBlock().apply { this.text = "The scale of the circle is inversely related to the scale of the square."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(Grid().apply {
            repeat(2) { columnDefinitions.add(column(1.0, GridUnitType.Star)) }
            children.add(rectangle); Grid.setColumn(ellipse, 1); children.add(ellipse)
        })
    }
}

@GallerySample(route = "XamlCompInterop", title = "Driving several related animations.")
internal fun xamlCompInteropRelatedAnimationsSample() = run {
    val compositor = CompositionTarget.getCompositorForCurrentThread()
    fun hover(element: UIElement) {
        fun animate(scale: Float) {
            element.startAnimation(compositor.createSpringVector3Animation().apply {
                target = "Scale"; finalValue = Vector3(scale, scale, scale); dampingRatio = 0.6f; period = 50.0.milliseconds
            })
        }
        element.pointerEntered.add { _, _ -> animate(1.5f) }; element.pointerExited.add { _, _ -> animate(1f) }
    }
    val buttons = List(4) { index -> Button().apply { width = 100.0; height = 50.0; margin = inset(5.0); content = "Item ${index + 1}"; hover(this) } }
    StackPanel().apply { this.spacing = 0.0; margin = Thickness(0.0, 0.0, 0.0, 50.0)
        children.add(TextBlock().apply { this.text = "Hover over any button to animate its scale. Notice that the other buttons move out of the way."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(TextBlock().apply { this.text = "Each button animates as a function of the previous button's scale and translation."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(0.0, 0.0, 0.0, 12.0) })
        buttons.forEach { children.add(it) }
        loaded.add { _, _ -> (1..3).forEach { index -> buttons[index].startAnimation(compositor.createExpressionAnimation().apply {
            expression = "(above.Scale.Y - 1) * 50 + above.Translation.Y % (50 * index)"; target = "Translation.Y"
            setExpressionReferenceParameter("above", buttons[index - 1]); setScalarParameter("index", index.toFloat())
        }) } } }
}

@GallerySample(route = "XamlCompInterop", title = "Reference ActualSize in ExpressionAnimations to make a responsive layout.")
internal fun xamlCompInteropResponsiveLayoutSample() = run {
    val compositor = CompositionTarget.getCompositorForCurrentThread()
    val circle = Grid().apply { width = 200.0; height = 200.0; margin = inset(12.0) }
    repeat(8) { index -> circle.children.add(Button().apply {
        content = "Button"; named(this, "Button $index"); horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Center
        loaded.add { _, _ -> startAnimation(compositor.createExpressionAnimation().apply {
            expression = "Vector3((source.ActualSize.X/2)*cos(.02*(source.ActualSize.X/2)+((2*Pi)/total)*index)+(source.ActualSize.X/2), (source.ActualSize.X/2)*sin(.02*(source.ActualSize.X/2)+((2*Pi)/total)*index),0)"
            target = "Translation"; setScalarParameter("index", (index + 1).toFloat()); setScalarParameter("total", 8f); setExpressionReferenceParameter("source", circle)
        }) }
    }) }
    val radius = Slider().apply { this.width = 196.0; this.header = "Change radius"; this.value = 200.0; this.minimum = 200.0; this.maximum = 400.0 }.also { gallerySlider -> gallerySlider.valueChanged.add { _, _ -> val it = gallerySlider.value; circle.width = it; circle.height = it } }
    circle to radius
}

@GallerySample(route = "XamlCompInterop", title = "Reference ActualOffset and ActualSize in ExpressionAnimations.")
internal fun xamlCompInteropPopupPositionSample() = run {
    val compositor = CompositionTarget.getCompositorForCurrentThread()
    val text = TextBlock().apply { this.text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { width = 300.0 }
    val popup = Popup().apply {
        margin = inset(5.0)
        child = Border().apply {
            minWidth = 50.0; minHeight = 50.0; maxWidth = 200.0; background = GalleryTheme.brush("FlyoutBackgroundThemeBrush")
            borderBrush = GalleryTheme.brush("ControlStrongStrokeColorDefaultBrush"); borderThickness = inset(2.0)
            child = TextBlock().apply { this.text = "I am always right aligned center to the target."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = inset(6.0); verticalAlignment = VerticalAlignment.Center }
        }
    }
    val sample = stack {
        children.add(TextBlock().apply { this.text = "This sample positions a popup relative to a block of text that has variable layout size based on font size. Use the sliders to move and resize the text."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(Grid().apply {
            horizontalAlignment = HorizontalAlignment.Left; children.add(text); children.add(popup)
            loaded.add { _, _ ->
                popup.xamlRoot = xamlRoot
                popup.startAnimation(compositor.createExpressionAnimation().apply {
                    expression = "Vector3(source.ActualOffset.X + source.ActualSize.X, source.ActualOffset.Y + source.ActualSize.Y / 2 - 25, 0)"
                    target = "Translation"; setExpressionReferenceParameter("source", text)
                }); popup.isOpen = true
            }
            unloaded.add { _, _ -> popup.isOpen = false }
        })
    }
    val options = stack {
        children.add(Slider().apply { this.width = 196.0; this.header = "Change font size"; this.value = 12.0; this.minimum = 12.0; this.maximum = 24.0 }.also { gallerySlider -> gallerySlider.valueChanged.add { _, _ -> val it = gallerySlider.value; text.fontSize = it } })
        children.add(Slider().apply { this.width = 196.0; this.header = "Change text margin"; this.value = 0.0; this.minimum = 0.0; this.maximum = 100.0 }.also { gallerySlider -> gallerySlider.valueChanged.add { _, _ -> val it = gallerySlider.value; text.margin = inset(it) } })
    }
    sample to options
}
