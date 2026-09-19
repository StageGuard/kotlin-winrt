package io.github.composefluent.winrt.gallery.motion

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.*
import microsoft.ui.xaml.shapes.Rectangle
import windows.foundation.Point
import windows.foundation.numerics.Vector3
import windows.system.VirtualKey

@GalleryPage(route = "ImplicitTransition", title = "Implicit Transitions", group = "Motion", order = 3)
internal fun implicitTransitionPage() = ExamplePage {
    fun number(header: String, value: Double, max: Double) = NumberBox().apply { this.header = header; this.value = value; minimum = 0.0; maximum = max; margin = inset(5.0) }
    fun value(number: NumberBox): Float { if (number.value.isNaN()) number.value = 0.0; return number.value.toFloat() }
    val opacity = implicitTransitionAutomaticallyAnimateChangesToOpacitySample()
    val opacityInput = number("Opacity (0.0 to 1.0)", 0.5, 1.0)
    fun setOpacity() { opacity.opacity = value(opacityInput).toDouble() }
    opacityInput.keyDown.add { _, args -> if (args.key == VirtualKey.Enter) setOpacity() }
    example("Automatically animate changes to opacity.", opacity, stack(0.0) {
        children.add(opacityInput); children.add(Button("Set Opacity", ::setOpacity).apply { margin = inset(5.0); horizontalAlignment = HorizontalAlignment.Stretch })
    })
    val rotation = implicitTransitionAutomaticallyAnimateChangesToRotationSample1()
    val rotationInput = number("Rotation (0.0 to 360.0)", 45.0, 360.0)
    fun setRotation() { rotation.centerPoint = Vector3((rotation.actualWidth / 2).toFloat(), (rotation.actualHeight / 2).toFloat(), 0f); rotation.rotation = value(rotationInput) }
    rotationInput.keyDown.add { _, args -> if (args.key == VirtualKey.Enter) setRotation() }
    example("Automatically animate changes to rotation.", rotation, stack(0.0) { children.add(rotationInput); children.add(Button("Set Rotation", ::setRotation).apply { margin = inset(5.0); horizontalAlignment = HorizontalAlignment.Stretch }) })
    val scaleSample = implicitTransitionScaleSample()
    example("Automatically animate changes to scale.", scaleSample.first, scaleSample.second)
    val translationSample = implicitTransitionTranslationSample()
    example("Automatically animate changes to translation.", translationSample.first, translationSample.second)
    val background = implicitTransitionImplicitlyAnimateBackgroundChangesSample3()
    var blue = true
    example("Implicitly animate background changes.", background, Button("Change Background Color") { blue = !blue; background.background = brush(if (blue) 0x0000FFu else 0xFFFF00u) })

    val theme = implicitTransitionGridThemeChangeSample()
    example("Implicitly animate a Grid theme change.", theme.first, theme.second)

}

@GallerySample(route = "ImplicitTransition", title = "Automatically animate changes to opacity.")
internal fun implicitTransitionAutomaticallyAnimateChangesToOpacitySample() = Rectangle().apply {
    width = 50.0; height = 50.0; margin = Thickness(45.0, 5.0, 5.0, 5.0)
    verticalAlignment = VerticalAlignment.Top; fill = GalleryTheme.brush("AccentFillColorDefaultBrush")
    opacity = 0.5
    opacityTransition = ScalarTransition()
}

@GallerySample(route = "ImplicitTransition", title = "Automatically animate changes to rotation.")
internal fun implicitTransitionAutomaticallyAnimateChangesToRotationSample1() = Rectangle().apply {
        width = 50.0; height = 50.0; margin = Thickness(45.0, 5.0, 5.0, 5.0)
        verticalAlignment = VerticalAlignment.Top; fill = GalleryTheme.brush("AccentFillColorDefaultBrush")
        rotationTransition = ScalarTransition()
        fill = LinearGradientBrush().apply {
            startPoint = Point(0.5f, 0f); endPoint = Point(0.5f, 1f)
            gradientStops.add(GradientStop().apply { color = rgb(0xD3D3D3u); offset = 0.0 })
            gradientStops.add(GradientStop().apply { color = windows.ui.viewmanagement.UISettings().getColorValue(windows.ui.viewmanagement.UIColorType.Accent); offset = 1.0 })
        }
    }

@GallerySample(route = "ImplicitTransition", title = "Implicitly animate background changes.")
internal fun implicitTransitionImplicitlyAnimateBackgroundChangesSample3() = ContentPresenter().apply { width = 50.0; height = 50.0; margin = Thickness(45.0, 5.0, 5.0, 5.0); verticalAlignment = VerticalAlignment.Top; this.background = brush(0x0000FFu); backgroundTransition = BrushTransition() }

@GallerySample(route = "ImplicitTransition", title = "Implicitly animate a Grid theme change.")
internal fun implicitTransitionGridThemeChangeSample() = run {
    val grid = Grid().apply {
        width = 300.0; minHeight = 200.0; verticalAlignment = VerticalAlignment.Top; requestedTheme = ElementTheme.Light
        borderBrush = brush(0x696969u); borderThickness = inset(1.0); backgroundTransition = BrushTransition(); background = brush(0xF3F3F3u)
        children.add(StackPanel().apply { this.spacing = 6.0; margin = inset(12.0); children.add(TextBlock().apply { this.text = "Lorem Ipsum"; this.fontSize = 20.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(TextBlock().apply { this.text = "The background of this grid animates when the theme changes."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(Button().apply { content = "Button" }); children.add(CheckBox().apply { content = "CheckBox" }) })
    }
    val changeTheme = Button().apply { this.content = "Change Theme" }.also { galleryButton -> galleryButton.click.add { _, _ -> grid.requestedTheme = if (grid.requestedTheme == ElementTheme.Dark) ElementTheme.Light else ElementTheme.Dark
        grid.background = brush(if (grid.requestedTheme == ElementTheme.Dark) 0x202020u else 0xF3F3F3u) } }
    grid to changeTheme
}

@GallerySample(route = "ImplicitTransition", title = "Automatically animate changes to scale.")
internal fun implicitTransitionScaleSample() = run {
    val transition = Vector3Transition()
    val target = Rectangle().apply {
        width = 50.0; height = 50.0; margin = Thickness(45.0, 5.0, 5.0, 5.0)
        verticalAlignment = VerticalAlignment.Top; fill = GalleryTheme.brush("AccentFillColorDefaultBrush"); scaleTransition = transition
    }
    val axes = BooleanArray(3) { true }
    val input = NumberBox().apply { header = "Scale (0.0 to 5.0)"; value = 1.0; minimum = 0.0; maximum = 5.0; margin = inset(5.0) }
    fun setScale(amount: Float) {
        transition.components = (if (axes[0]) Vector3TransitionComponents.X else Vector3TransitionComponents(0u)) or
            (if (axes[1]) Vector3TransitionComponents.Y else Vector3TransitionComponents(0u)) or
            (if (axes[2]) Vector3TransitionComponents.Z else Vector3TransitionComponents(0u))
        target.scale = Vector3(amount, amount, amount)
    }
    input.keyDown.add { _, args -> if (args.key == VirtualKey.Enter) { if (input.value.isNaN()) input.value = 0.0; setScale(input.value.toFloat()) } }
    val options = StackPanel().apply { this.spacing = 0.0; listOf(0.5f, 1f, 2f).forEach { amount -> children.add(Button().apply { this.content = "Set Scale to ($amount, $amount, $amount)" }.also { galleryButton -> galleryButton.click.add { _, _ -> setScale(amount) } }.apply { margin = inset(5.0); horizontalAlignment = HorizontalAlignment.Stretch }) }
        children.add(TextBlock().apply { this.text = "Components"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        listOf("X", "Y", "Z").forEachIndexed { index, axis -> children.add(CheckBox().apply { this.content = "Animate $axis"; this.isChecked = true }.also { galleryCheckBox -> galleryCheckBox.click.add { _, _ -> val it = galleryCheckBox.isChecked == true; axes[index] = it } }) }
        children.add(input); children.add(Button().apply { this.content = "Set custom scale" }.also { galleryButton -> galleryButton.click.add { _, _ -> if (input.value.isNaN()) input.value = 0.0; setScale(input.value.toFloat()) } }.apply { margin = inset(5.0); horizontalAlignment = HorizontalAlignment.Stretch }) }
    target to options
}

@GallerySample(route = "ImplicitTransition", title = "Automatically animate changes to translation.")
internal fun implicitTransitionTranslationSample() = run {
    val transition = Vector3Transition()
    val target = Rectangle().apply {
        width = 50.0; height = 50.0; margin = Thickness(45.0, 5.0, 5.0, 5.0)
        verticalAlignment = VerticalAlignment.Top; fill = GalleryTheme.brush("AccentFillColorDefaultBrush"); translationTransition = transition
    }
    val axes = BooleanArray(3) { true }
    val input = NumberBox().apply { header = "Translation (0.0 to 200.0)"; value = 1.0; minimum = 0.0; maximum = 200.0; margin = inset(5.0) }
    fun setTranslation(amount: Float) {
        transition.components = (if (axes[0]) Vector3TransitionComponents.X else Vector3TransitionComponents(0u)) or
            (if (axes[1]) Vector3TransitionComponents.Y else Vector3TransitionComponents(0u)) or
            (if (axes[2]) Vector3TransitionComponents.Z else Vector3TransitionComponents(0u))
        target.translation = Vector3(amount, amount, amount)
    }
    input.keyDown.add { _, args -> if (args.key == VirtualKey.Enter) { if (input.value.isNaN()) input.value = 0.0; setTranslation(input.value.toFloat()) } }
    val options = StackPanel().apply { this.spacing = 0.0; listOf(0f, 100f, 200f).forEach { amount -> children.add(Button().apply { this.content = "Set Translation to ($amount, $amount, $amount)" }.also { galleryButton -> galleryButton.click.add { _, _ -> setTranslation(amount) } }.apply { margin = inset(5.0); horizontalAlignment = HorizontalAlignment.Stretch }) }
        children.add(TextBlock().apply { this.text = "Components"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        listOf("X", "Y", "Z").forEachIndexed { index, axis -> children.add(CheckBox().apply { this.content = "Animate $axis"; this.isChecked = true }.also { galleryCheckBox -> galleryCheckBox.click.add { _, _ -> val it = galleryCheckBox.isChecked == true; axes[index] = it } }) }
        children.add(input); children.add(Button().apply { this.content = "Set custom Translation" }.also { galleryButton -> galleryButton.click.add { _, _ -> if (input.value.isNaN()) input.value = 0.0; setTranslation(input.value.toFloat()) } }.apply { margin = inset(5.0); horizontalAlignment = HorizontalAlignment.Stretch }) }
    target to options
}
