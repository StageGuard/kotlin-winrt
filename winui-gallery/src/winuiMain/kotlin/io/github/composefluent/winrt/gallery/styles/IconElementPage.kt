package io.github.composefluent.winrt.gallery.styles

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.*
import microsoft.ui.xaml.media.imaging.BitmapImage
import microsoft.ui.xaml.media.imaging.SvgImageSource
import windows.foundation.Point
import windows.foundation.Uri

@GalleryPage(route = "IconElement", title = "IconElement", group = "Styles", order = 3)
internal fun iconElementPage() = ExamplePage {
    val bitmap = iconElementBitmapIconSample()
    example("BitmapIcon with a multicolor bitmap image.", stack {
        children.add(label("The ShowAsMonochrome property (true by default) will result in a solid block of the foreground color if the property is set to true and the icon is more than one color. This behavior can be ignored by setting the ShowAsMonochrome property to false."))
        children.add(bitmap)
    }, option("Monochrome") { bitmap.showAsMonochrome = it; bitmap.uriSource = Uri("ms-appx:///Assets/SampleMedia/Slices.png") })
    example("FontIcon with a glyph from a specific font.", stack {
        children.add(label("Use FontIcon as the icon for a control if you want to specify a Glyph value from a FontFamily. Windows 10 uses the Segoe MDL2 Assets FontFamily and that is what this example is showing."))
        children.add(iconElementFontIconWithAGlyphFromASpecificFontSample1().apply { named(this, "ExampleButton1") })
    })
    example("ImageIcon with a bitmap image in a Button.", stack {
        children.add(label("To use an ImageIcon as the icon for a control, you can set image that has a file format supported by the Image class. The two examples here show a PNG and SVG image as the icon."))
        children.add(iconElementImageIconWithABitmapImageInAButtonSample2().apply { named(this, "ImageExample1") })
    })
    example("ImageIcon with an SVG image in a Button.", iconElementImageIconWithAnSVGImageInAButtonSample3().apply { named(this, "ImageExample2") })
    example("PathIcon in a Button.", stack {
        children.add(label("To use a PathIcon as the icon for a control, you specify the path data of the image you are trying to display. The path data draws a series of connected lines and curves."))
        children.add(iconElementPathIconInAButtonSample4().apply { named(this, "Example1Button") })
    })
    example("SymbolIcon in a Button.", stack {
        children.add(label("To use a SymbolIcon as the icon for a control, you specify the enum value for the glyph you would like to display. SymbolIcon's enum is based off of icons from the Segoe MDL2 font used by Windows 10."))
        children.add(iconElementSymbolIconInAButtonSample5().apply { named(this, "AcceptButton") })
    })
}

@GallerySample(route = "IconElement", title = "BitmapIcon with a multicolor bitmap image.")
internal fun iconElementBitmapIconSample() = BitmapIcon().apply {
    width = 50.0
    horizontalAlignment = HorizontalAlignment.Left
    showAsMonochrome = false
    uriSource = Uri("ms-appx:///Assets/SampleMedia/Slices.png")
}

@GallerySample(route = "IconElement", title = "FontIcon with a glyph from a specific font.")
internal fun iconElementFontIconWithAGlyphFromASpecificFontSample1() = Button().apply {
    content = FontIcon().apply {
        fontFamily = FontFamily("Segoe MDL2 Assets")
        glyph = "\uE790"
    }
}

@GallerySample(route = "IconElement", title = "ImageIcon with a bitmap image in a Button.")
internal fun iconElementImageIconWithABitmapImageInAButtonSample2() = Button().apply {
    width = 100.0
    content = ImageIcon().apply {
        source = BitmapImage(Uri("ms-appx:///Assets/SampleMedia/Slices.png"))
    }
}

@GallerySample(route = "IconElement", title = "ImageIcon with an SVG image in a Button.")
internal fun iconElementImageIconWithAnSVGImageInAButtonSample3() = Button().apply {
    content = ImageIcon().apply {
        width = 50.0
        source = SvgImageSource(Uri("ms-appx:///Assets/SampleMedia/MirrorPCConsent.svg"))
    }
}

@GallerySample(route = "IconElement", title = "PathIcon in a Button.")
internal fun iconElementPathIconInAButtonSample4() = Button().apply {
    content = PathIcon().apply {
        horizontalAlignment = HorizontalAlignment.Center
        data = PathGeometry().apply {
            fillRule = FillRule.Nonzero
            figures.add(PathFigure().apply {
                startPoint = Point(20f, 20f)
                listOf(Point(24f, 10f), Point(24f, 24f), Point(5f, 24f)).forEach { next ->
                    segments.add(LineSegment().apply { point = next })
                }
            })
        }
    }
}

@GallerySample(route = "IconElement", title = "SymbolIcon in a Button.")
internal fun iconElementSymbolIconInAButtonSample5() = Button().apply {
    content = StackPanel().apply {
        children.add(SymbolIcon(Symbol.Accept))
        children.add(TextBlock().apply { text = "Accept"; textWrapping = TextWrapping.Wrap })
    }
}
