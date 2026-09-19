package io.github.composefluent.winrt.gallery.scrolling

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import microsoft.ui.composition.Vector3KeyFrameAnimation
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.Stretch
import windows.foundation.numerics.Vector2
import windows.foundation.numerics.Vector3
import windows.globalization.numberformatting.*

@GalleryPage(route = "ScrollView", title = "ScrollView", group = "Scrolling", order = 2)
internal fun scrollViewPage() = ExamplePage {
    val basic = scrollViewContentSample()
    val modes = listOf(ScrollingScrollMode.Enabled, ScrollingScrollMode.Disabled, ScrollingScrollMode.Auto)
    val bars = listOf(ScrollingScrollBarVisibility.Auto, ScrollingScrollBarVisibility.Visible, ScrollingScrollBarVisibility.Hidden)

    example("Content inside a ScrollView.", stack(16.0) {
        children.add(label("This ScrollView allows horizontal and vertical scrolling, as well as zooming. Change the settings on the right to alter those capabilities or the built-in scrollbars' visibility."))
        children.add(basic)
    }, stack {
        children.add(select("ZoomMode", listOf("Enabled", "Disabled")) { basic.zoomMode = if (it == 0) ScrollingZoomMode.Enabled else ScrollingZoomMode.Disabled })
        children.add(NumberBox().apply {
            header = "ZoomFactor"; minimum = 0.1; maximum = 10.0; smallChange = 1.0; largeChange = 10.0; value = 4.0
            spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
            numberFormatter = DecimalFormatter().apply {
                integerDigits = 2; fractionDigits = 1
                numberRounder = IncrementNumberRounder().apply { increment = 0.1; roundingAlgorithm = RoundingAlgorithm.RoundHalfUp }
            }
            valueChanged.add { _, _ -> if (!value.isNaN()) basic.zoomTo(value.toFloat(), null) }
        })
        children.add(label("ScrollMode"))
        children.add(select("Horizontal", listOf("Enabled", "Disabled", "Auto"), 2) { basic.horizontalScrollMode = modes[it] })
        children.add(select("Vertical", listOf("Enabled", "Disabled", "Auto"), 2) { basic.verticalScrollMode = modes[it] })
        children.add(label("ScrollbarVisibility"))
        children.add(select("Horizontal", listOf("Auto", "Visible", "Hidden")) { basic.horizontalScrollBarVisibility = bars[it] })
        children.add(select("Vertical", listOf("Auto", "Visible", "Hidden")) { basic.verticalScrollBarVisibility = bars[it] })
    })
    val constant = scrollViewVelocitySample()
    val velocity = NumberBox().apply {
        header = "Vertical velocity"; minimum = -200.0; maximum = 200.0; smallChange = 10.0; largeChange = 30.0; value = 30.0
        spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
    }
    var correctingVelocity = false
    velocity.valueChanged.add { _, args ->
        if (!correctingVelocity && !args.oldValue.isNaN() && !args.newValue.isNaN()) {
            constant.scrollBy(0.0, 0.0, ScrollingScrollOptions(ScrollingAnimationMode.Disabled, ScrollingSnapPointsMode.Ignore))
            var speed = args.newValue.toFloat()
            if (speed in -30.0f..30.0f) {
                speed = if (args.newValue < args.oldValue) {
                    if (constant.verticalOffset == 0.0) 30.0f else -30.0f
                } else {
                    if (constant.verticalOffset == constant.scrollableHeight) -30.0f else 30.0f
                }
            } else if (speed < 30.0f && constant.verticalOffset == 0.0) speed = 30.0f
            else if (speed > 30.0f && constant.verticalOffset == constant.scrollableHeight) speed = -30.0f
            correctingVelocity = true
            try { velocity.value = speed.toDouble() } finally { correctingVelocity = false }
            constant.addScrollVelocity(Vector2(0.0f, speed), Vector2(0.0f, 0.0f))
        }
    }

    example("Constant velocity scrolling.", stack(16.0) {
        children.add(label("Set the vertical velocity to a value greater than 30 to scroll down, or a value smaller than -30 to scroll up at a constant speed."))
        children.add(constant)
    }, velocity)

    var mode = 0
    val duration = NumberBox().apply {
        header = "Animation duration (msec)"; minimum = 1000.0; maximum = 5000.0; value = 1500.0; smallChange = 500.0; largeChange = 1000.0
        spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
    }
    val animated = scrollViewAnimationSample({ mode }, duration)

    example("Programmatic scroll with a custom animation.", stack(16.0) {
        children.add(label("Pick an animation type and its duration and then click the button on the right to launch a programmatic scroll."))
        children.add(animated)
    }, stack {
        children.add(select("Scroll with animation", listOf("Default", "Accordion", "Teleportation")) { mode = it })
        children.add(duration)
        children.add(Button("Scroll with animation") {
            val target = animated.scrollableHeight * if (animated.verticalOffset > animated.scrollableHeight / 2.0) 0.2 else 0.8
            animated.scrollTo(animated.horizontalOffset, target, ScrollingScrollOptions(ScrollingAnimationMode.Enabled, ScrollingSnapPointsMode.Ignore))
        })
    })
}

@GallerySample(route = "ScrollView", title = "Content inside a ScrollView.")
internal fun scrollViewContentSample() = ScrollView().apply {
    width = 400.0; height = 266.0
    isTabStop = true
    horizontalAlignment = HorizontalAlignment.Left
    verticalAlignment = VerticalAlignment.Top
    contentOrientation = ScrollingContentOrientation.None
    zoomMode = ScrollingZoomMode.Enabled
    content = Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/cliff.jpg") ) }.apply {
        stretch = Stretch.Uniform; named(this, "cliff")
    }
    loaded.add { _, _ ->
        zoomTo(4.0f, null, ScrollingZoomOptions(ScrollingAnimationMode.Enabled, ScrollingSnapPointsMode.Ignore))
    }
}

@GallerySample(route = "ScrollView", title = "Constant velocity scrolling.")
internal fun scrollViewVelocitySample() = ScrollView().apply {
    width = 400.0; height = 300.0; isTabStop = true
    horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top
    content = StackPanel().apply { this.spacing = 0.0; listOf("grapes", "rainier", "sunset", "treetops", "valley", "cliff").forEach { name ->
            children.add(Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/$name.jpg") ) }.apply { stretch = Stretch.Uniform; named(this, name) })
        } }
}

@GallerySample(route = "ScrollView", title = "Programmatic scroll with a custom animation.")
internal fun scrollViewAnimationSample(mode: () -> Int, duration: NumberBox) = ScrollView().apply {
    width = 400.0; height = 300.0; isTabStop = true
    horizontalAlignment = HorizontalAlignment.Left; verticalAlignment = VerticalAlignment.Top
    content = StackPanel().apply { this.spacing = 0.0; repeat(8) { index ->
            val name = "LandscapeImage${index + 1}"
            children.add(Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/$name.jpg") ) }.apply { stretch = Stretch.Uniform; named(this, name) })
        } }
    fun target() = scrollableHeight * if (verticalOffset > scrollableHeight / 2.0) 0.2 else 0.8
    scrollAnimationStarting.add { _, args ->
        val stock = checkNotNull(args.animation).asWinRT<Vector3KeyFrameAnimation>()
        if (mode() == 0) stock.duration = duration.value.milliseconds
        else {
            val compositor = checkNotNull(stock.compositor)
            val custom = compositor.createVector3KeyFrameAnimation()
            val y = target().toFloat()
            val x = horizontalOffset.toFloat()
            val delta = y - verticalOffset.toFloat()
            if (mode() == 1) {
                var bounce = 0.1f * delta
                repeat(3) { step ->
                    custom.insertKeyFrame(1.0f - 0.4f / 2.0.pow(step).toFloat(), Vector3(x, y + bounce, 0.0f))
                    bounce /= -2.0f
                }
                custom.insertKeyFrame(1.0f, Vector3(x, y, 0.0f))
            } else {
                val start = compositor.createCubicBezierEasingFunction(Vector2(1.0f, 0.0f), Vector2(1.0f, 0.0f))
                val step = compositor.createStepEasingFunction(1)
                val end = compositor.createCubicBezierEasingFunction(Vector2(0.0f, 1.0f), Vector2(0.0f, 1.0f))
                custom.insertKeyFrame(0.499999f, Vector3(x, y - 0.9f * delta, 0.0f), start)
                custom.insertKeyFrame(0.5f, Vector3(x, y - 0.1f * delta, 0.0f), step)
                custom.insertKeyFrame(1.0f, Vector3(x, y, 0.0f), end)
            }
            custom.duration = duration.value.milliseconds
            args.animation = custom
        }
    }
}
