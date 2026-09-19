package io.github.composefluent.winrt.gallery.media

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.media.imaging.BitmapImage
import microsoft.ui.xaml.media.imaging.SvgImageSource
import windows.foundation.Uri

@GalleryPage(route = "Image", title = "Image", group = "Media", order = 2)
internal fun imagePage() = ExamplePage {
    example("A basic Image with a local file.", imageBasicImageWithALocalFileSample())
    example("An Image decoded to its rendering size.", imageImageDecodedToItsRenderingSizeSample1())
    val stretchImage = imageImageStretchingSample2()
    example("Image stretching.", stretchImage, choices("Image stretch mode", listOf("None", "Fill", "Uniform", "UniformToFill")) {
        stretchImage.stretch = listOf(Stretch.None, Stretch.Fill, Stretch.Uniform, Stretch.UniformToFill)[it]
    })
    example("Nine-grid images.", imageNineGridDemo())
    example("An SVG image.", imageSVGImageSample4())
    example("An animated GIF.", imageAnimatedGifDemo())

}

private fun imageAnimatedGifDemo() = stack {
    val manual = imageAnimatedGifSourceSample(false)
    val playback = stack(8.0) {
        visibility = Visibility.Collapsed
        children.add(Button("Play") { imageGifPlaySample(manual) })
        children.add(Button("Stop") { imageGifStopSample(manual) })
    }
    manual.imageOpened.add { _, _ ->
        playback.visibility = if (manual.isAnimatedBitmap) Visibility.Visible else Visibility.Collapsed
    }
    children.add(label("An Image element automatically plays an animated GIF source."))
    children.add(Image().apply {
        height = 40.0; horizontalAlignment = HorizontalAlignment.Left
        source = imageAnimatedGifSourceSample(true)
    })
    children.add(label("Set AutoPlay to False to prevent the GIF from playing automatically."))
    children.add(Image().apply {
        height = 40.0; horizontalAlignment = HorizontalAlignment.Left
        source = imageAnimatedGifSourceSample(false)
    })
    children.add(label("Control playback manually using BitmapImage.Play() and Stop()."))
    children.add(Image().apply { height = 40.0; horizontalAlignment = HorizontalAlignment.Left; source = manual })
    children.add(playback)
}

@GallerySample(route = "Image", title = "A basic Image with a local file.")
internal fun imageBasicImageWithALocalFileSample() = Image().apply {
    height = 100.0
    source = BitmapImage().apply { uriSource = Uri("ms-appx:///Assets/SampleMedia/treetops.jpg") }
}

@GallerySample(route = "Image", title = "An Image decoded to its rendering size.")
internal fun imageImageDecodedToItsRenderingSizeSample1() = Image().apply {
        height = 100.0
        source = BitmapImage().apply { decodePixelHeight = 100; uriSource = Uri("ms-appx:///Assets/SampleMedia/treetops.jpg") }
    }

private fun imageNineGridDemo() = stack(0.0) {
    children.add(label("The normal image"))
    children.add(imageNineGridSample(82.0, Thickness(0.0, 0.0, 0.0, 0.0)))
    children.add(label("Image stretched evenly"))
    children.add(imageNineGridSample(164.0, Thickness(3.0, 3.0, 3.0, 3.0)))
    children.add(label("Image stretched using nine grid"))
    children.add(imageNineGridSample(164.0, Thickness(30.0, 20.0, 30.0, 20.0)))
}

@GallerySample(route = "Image", title = "Nine-grid images.")
internal fun imageNineGridSample(height: Double, grid: Thickness) = Image().apply {
    this.height = height
    nineGrid = grid
    source = BitmapImage().apply { uriSource = Uri("ms-appx:///Assets/SampleMedia/ninegrid.gif") }
}

@GallerySample(route = "Image", title = "An SVG image.")
internal fun imageSVGImageSample4() = Image().apply { height = 100.0; source = SvgImageSource(Uri("ms-appx:///Assets/SampleMedia/MirrorPCConsent.svg")) }

@GallerySample(route = "Image", title = "Image stretching.")
internal fun imageImageStretchingSample2() = Image().apply {
    width = 100.0; height = 100.0; stretch = Stretch.None
    source = BitmapImage().apply { uriSource = Uri("ms-appx:///Assets/SampleMedia/valley.jpg") }
}

@GallerySample(route = "Image", title = "An animated GIF.")
internal fun imageAnimatedGifSourceSample(autoPlay: Boolean) = BitmapImage().apply {
    this.autoPlay = autoPlay
    uriSource = Uri("ms-appx:///Assets/SampleMedia/animated.gif")
}

internal fun imageGifPlaySample(image: BitmapImage) = image.play()

internal fun imageGifStopSample(image: BitmapImage) = image.stop()
