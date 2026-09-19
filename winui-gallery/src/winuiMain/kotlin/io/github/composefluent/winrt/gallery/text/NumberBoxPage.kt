package io.github.composefluent.winrt.gallery.text

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.FontFamily
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import windows.globalization.numberformatting.DecimalFormatter
import windows.globalization.numberformatting.IncrementNumberRounder
import windows.globalization.numberformatting.RoundingAlgorithm
import windows.ui.Color

@GalleryPage(route = "NumberBox", title = "NumberBox", group = "Text", order = 1)
internal fun numberBoxPage(): UIElement = ExamplePage {
    example("A NumberBox that evaluates expressions.", numberBoxNumberBoxThatEvaluatesExpressionsSample())

    val spinBox = numberBoxNumberBoxWithASpinButtonSample1()
    val placement = RadioButtons().apply {
        header = "SpinButton placement"
        items.add("Inline")
        items.add("Compact")
        selectedIndex = 0
        selectionChanged.add { _, _ ->
            spinBox.spinButtonPlacementMode = if (selectedIndex == 0) {
                NumberBoxSpinButtonPlacementMode.Inline
            } else NumberBoxSpinButtonPlacementMode.Compact
        }
    }
    example("A NumberBox with a spin button.", spinBox, placement)

    example("A formatted NumberBox that rounds to the nearest 0.25.", numberBoxFormattedNumberBoxThatRoundsToTheNearest025Sample2())
}

@GallerySample(route = "NumberBox", title = "A NumberBox that evaluates expressions.")
internal fun numberBoxNumberBoxThatEvaluatesExpressionsSample() = NumberBox().apply {
        acceptsExpression = true
        header = "Enter an expression:"
        placeholderText = "1 + 2^2"
        value = Double.NaN
    }

@GallerySample(route = "NumberBox", title = "A formatted NumberBox that rounds to the nearest 0.25.")
internal fun numberBoxFormattedNumberBoxThatRoundsToTheNearest025Sample2() = NumberBox().apply {
        header = "Enter a dollar amount:"
        placeholderText = "0.00"
        numberFormatter = DecimalFormatter().apply {
            integerDigits = 1
            fractionDigits = 2
            numberRounder = IncrementNumberRounder().apply {
                increment = 0.25
                roundingAlgorithm = RoundingAlgorithm.RoundHalfUp
            }
        }
    }

@GallerySample(route = "NumberBox", title = "A NumberBox with a spin button.")
internal fun numberBoxNumberBoxWithASpinButtonSample1() = NumberBox().apply {
        header = "Enter an integer:"
        verticalAlignment = VerticalAlignment.Top
        value = 10.0
        smallChange = 10.0
        largeChange = 100.0
        spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
        named(this, "NumberBox with spin button")
    }
