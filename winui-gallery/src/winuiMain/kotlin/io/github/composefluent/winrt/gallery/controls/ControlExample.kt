package io.github.composefluent.winrt.gallery.controls

import io.github.composefluent.winrt.gallery.GalleryCodeCatalog
import io.github.composefluent.winrt.gallery.GallerySampleCode
import io.github.composefluent.winrt.gallery.GalleryTheme
import io.github.composefluent.winrt.gallery.kotlinCodePreview
import io.github.composefluent.winrt.runtime.WinRTXamlContentProperty
import microsoft.ui.xaml.UIElement
import microsoft.ui.xaml.controls.UserControl

/** Shared sample presenter following WinUI Gallery's ControlExample structure. */
@WinRTXamlContentProperty("Example")
internal class ControlExample : UserControl() {
    private var ready = false
    private var sourceSample: GallerySampleCode? = null

    var HeaderText: String = ""
        set(value) {
            field = value
            if (ready) headerTextPresenter.text = value
        }

    var SampleDefinition: String = ""
        set(value) {
            field = value
            sourceSample = GalleryCodeCatalog.sampleDefinition(value)
            sourceSample?.let { HeaderText = it.header }
        }

    var Example: UIElement? = null
        set(value) {
            field = value
            if (ready) examplePresenter.content = value
        }

    var Output: UIElement? = null
        set(value) {
            field = value
            if (ready) outputPresenter.content = value
        }

    var Options: UIElement? = null
        set(value) {
            field = value
            if (ready) optionsPresenter.content = value
        }

    override fun initializeComponent() {
        super.initializeComponent()
        ready = true
        headerTextPresenter.text = HeaderText
        examplePresenter.content = Example
        outputPresenter.content = Output
        optionsPresenter.content = Options

        val state = GalleryTheme.sampleBeingConstructed
        val route = state?.sourceRoute
        val index = state?.sourceExampleIndex ?: 0
        if (state != null) {
            state.sourceExampleIndex++
            state.sampleBodies.add(examplePresenter)
        }
        var sourceReady = false
        sourcePresenter.expanding.add { _, _ ->
            if (!sourceReady) {
                val kotlin = sourceSample?.kotlin ?: route?.let { GalleryCodeCatalog.document(it, HeaderText, index) }
                val xaml = sourceSample?.xaml ?: route?.let { GalleryCodeCatalog.xamlDocument(it, HeaderText, index) }
                if (kotlin != null) sourcePresenter.content = kotlinCodePreview(kotlin, xaml)
                sourceReady = true
            }
        }
    }
}
