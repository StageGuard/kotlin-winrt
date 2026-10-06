package io.github.composefluent.winrt.gallery.controls
import io.github.composefluent.winrt.gallery.GalleryTheme
import microsoft.ui.xaml.controls.UserControl
internal class SampleThemeListener : UserControl() {
    override fun initializeComponent() {
        super.initializeComponent()
        GalleryTheme.sampleBeingConstructed?.sampleBodies?.add(this)
    }
}
