package io.github.composefluent.winrt.gallery.motion

import io.github.composefluent.winrt.gallery.*
import kotlin.time.Duration.Companion.milliseconds
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.TranslateTransform
import microsoft.ui.xaml.media.animation.*
import microsoft.ui.xaml.shapes.Rectangle

@GalleryPage(route = "EasingFunction", title = "Easing Functions", group = "Motion", order = 2)
internal fun easingFunctionPage() = ExamplePage {
    children.add(label("- Use the Standard easing function for animating general property changes.\n- Use the Accelerate easing function to animate objects that are exiting the scene.\n- Use the Decelerate easing function to animate objects that are entering the scene."))
    val standard = easingStandardSample()
    example("Standard easing function", standard.first, standard.second)
    val accelerate = easingAccelerateSample()
    example("Accelerate easing function", accelerate.first, accelerate.second)
    val decelerate = easingDecelerateSample()
    example("Decelerate easing function", decelerate.first, decelerate.second)
    val other = easingOtherFunctionsSample()
    example("Other easing functions", other.first, other.second)
}

@GallerySample(route = "EasingFunction", title = "Standard easing function")
internal fun easingStandardSample() = run {
    val translation = TranslateTransform()
    val animation = DoubleAnimation().apply {
        duration = Duration(500.milliseconds, DurationType.TimeSpan)
        easingFunction = CircleEase().apply { easingMode = EasingMode.EaseInOut }
        Storyboard.setTarget(this, translation); Storyboard.setTargetProperty(this, "X")
    }
    val storyboard = Storyboard().apply { children.add(animation) }
    val sample = Grid().apply {
        columnDefinitions.add(column(1.0, GridUnitType.Auto)); columnDefinitions.add(column(1.0, GridUnitType.Star).apply { minWidth = 300.0 })
        children.add(Button().apply { this.content = "Animate" }.also { galleryButton -> galleryButton.click.add { _, _ -> animation.from = translation.x; animation.to = if (translation.x > 0) 0.0 else 200.0; storyboard.begin() } }.apply { named(this, "Animate rectangle using Standard easing function") })
        children.add(Rectangle().apply { Grid.setColumn(this, 1); width = 50.0; height = 50.0; fill = GalleryTheme.brush("AccentFillColorDefaultBrush"); renderTransform = translation })
        unloaded.add { _, _ -> storyboard.stop() }
    }
    sample to null
}

@GallerySample(route = "EasingFunction", title = "Accelerate easing function")
internal fun easingAccelerateSample() = run {
    val exponent = NumberBox().apply { header = "Exponent"; value = 4.5; named(this, "Accelerate easing exponent") }
    val translation = TranslateTransform()
    val animation = DoubleAnimation().apply {
        duration = Duration(150.milliseconds, DurationType.TimeSpan)
        Storyboard.setTarget(this, translation); Storyboard.setTargetProperty(this, "X")
    }
    val storyboard = Storyboard().apply { children.add(animation) }
    val sample = Grid().apply {
        columnDefinitions.add(column(1.0, GridUnitType.Auto)); columnDefinitions.add(column(1.0, GridUnitType.Star).apply { minWidth = 300.0 })
        children.add(Button().apply { this.content = "Animate" }.also { galleryButton -> galleryButton.click.add { _, _ -> animation.from = translation.x; animation.to = if (translation.x > 0) 0.0 else 200.0
            animation.easingFunction = ExponentialEase().apply { this.exponent = exponent.value; easingMode = EasingMode.EaseIn }
            storyboard.begin() } }.apply { named(this, "Animate rectangle using Accelerate easing function") })
        children.add(Rectangle().apply { Grid.setColumn(this, 1); width = 50.0; height = 50.0; fill = GalleryTheme.brush("AccentFillColorDefaultBrush"); renderTransform = translation })
        unloaded.add { _, _ -> storyboard.stop() }
    }
    sample to exponent
}

@GallerySample(route = "EasingFunction", title = "Decelerate easing function")
internal fun easingDecelerateSample() = run {
    val exponent = NumberBox().apply { header = "Exponent"; value = 7.0; named(this, "Decelerate easing exponent") }
    val translation = TranslateTransform()
    val animation = DoubleAnimation().apply {
        duration = Duration(300.milliseconds, DurationType.TimeSpan)
        Storyboard.setTarget(this, translation); Storyboard.setTargetProperty(this, "X")
    }
    val storyboard = Storyboard().apply { children.add(animation) }
    val sample = Grid().apply {
        columnDefinitions.add(column(1.0, GridUnitType.Auto)); columnDefinitions.add(column(1.0, GridUnitType.Star).apply { minWidth = 300.0 })
        children.add(Button().apply { this.content = "Animate" }.also { galleryButton -> galleryButton.click.add { _, _ -> animation.from = translation.x; animation.to = if (translation.x > 0) 0.0 else 200.0
            animation.easingFunction = ExponentialEase().apply { this.exponent = exponent.value; easingMode = EasingMode.EaseOut }
            storyboard.begin() } }.apply { named(this, "Animate rectangle using Decelerate easing function") })
        children.add(Rectangle().apply { Grid.setColumn(this, 1); width = 50.0; height = 50.0; fill = GalleryTheme.brush("AccentFillColorDefaultBrush"); renderTransform = translation })
        unloaded.add { _, _ -> storyboard.stop() }
    }
    sample to exponent
}

@GallerySample(route = "EasingFunction", title = "Other easing functions")
internal fun easingOtherFunctionsSample() = run {
    val functions: List<Pair<String, () -> EasingFunctionBase>> = listOf(
        "BackEase" to { BackEase() }, "BounceEase" to { BounceEase() }, "CircleEase" to { CircleEase() },
        "CubicEase" to { CubicEase() }, "ElasticEase" to { ElasticEase() }, "ExponentialEase" to { ExponentialEase() },
        "PowerEase" to { PowerEase() }, "QuadraticEase" to { QuadraticEase() }, "QuarticEase" to { QuarticEase() },
        "QuinticEase" to { QuinticEase() }, "SineEase" to { SineEase() },
    )
    var selected = 0
    var mode = EasingMode.EaseOut
    val translation = TranslateTransform()
    val animation = DoubleAnimation().apply {
        duration = Duration(500.milliseconds, DurationType.TimeSpan)
        Storyboard.setTarget(this, translation); Storyboard.setTargetProperty(this, "X")
    }
    val storyboard = Storyboard().apply { children.add(animation) }
    val sample = Grid().apply {
        columnDefinitions.add(column(1.0, GridUnitType.Auto)); columnDefinitions.add(column(1.0, GridUnitType.Star).apply { minWidth = 300.0 })
        children.add(Button().apply { this.content = "Animate" }.also { galleryButton -> galleryButton.click.add { _, _ -> animation.from = translation.x; animation.to = if (translation.x > 0) 0.0 else 200.0
            animation.easingFunction = functions[selected].second().apply { easingMode = mode }
            storyboard.begin() } }.apply { named(this, "Animate rectangle using another easing function") })
        children.add(Rectangle().apply { Grid.setColumn(this, 1); width = 50.0; height = 50.0; fill = GalleryTheme.brush("AccentFillColorDefaultBrush"); renderTransform = translation })
        unloaded.add { _, _ -> storyboard.stop() }
    }
    val options = stack {
        children.add(ComboBox().apply { this.header = "Easing type"; functions.map { it.first }.forEach { this.items.add(it) }; this.selectedIndex = 0 }.also { galleryComboBox -> galleryComboBox.selectionChanged.add { _, _ -> val gallerySelectedIndex = galleryComboBox.selectedIndex; if (gallerySelectedIndex >= 0) { val it = gallerySelectedIndex; selected = it } } })
        children.add(RadioButtons().apply { this.header = "Easing mode"; listOf("EaseOut", "EaseIn", "EaseInOut").forEach { this.items.add(it) }; this.selectedIndex = 0 }.also { galleryRadioButtons -> galleryRadioButtons.selectionChanged.add { _, _ -> val gallerySelectedIndex = galleryRadioButtons.selectedIndex; if (gallerySelectedIndex in 0 until galleryRadioButtons.items.size) { val it = gallerySelectedIndex; mode = listOf(EasingMode.EaseOut, EasingMode.EaseIn, EasingMode.EaseInOut)[it] } } })
    }
    sample to options
}
