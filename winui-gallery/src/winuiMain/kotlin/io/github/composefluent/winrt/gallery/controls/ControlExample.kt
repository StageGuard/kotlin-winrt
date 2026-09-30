package io.github.composefluent.winrt.gallery.controls

import io.github.composefluent.winrt.gallery.GalleryCodeCatalog
import io.github.composefluent.winrt.gallery.GallerySampleCode
import io.github.composefluent.winrt.gallery.GalleryTheme
import io.github.composefluent.winrt.gallery.kotlinCodePreview
import io.github.composefluent.winrt.gallery.code.KotlinCodeDocument
import io.github.composefluent.winrt.gallery.code.KotlinCodeSpan
import io.github.composefluent.winrt.gallery.code.KotlinCodeKind
import io.github.composefluent.winrt.runtime.WinRTXamlContentProperty
import microsoft.ui.xaml.UIElement
import microsoft.ui.xaml.Visibility
import microsoft.ui.xaml.GridLength
import microsoft.ui.xaml.GridUnitType
import microsoft.ui.xaml.controls.UserControl

/** Shared sample presenter following WinUI Gallery's ControlExample structure. */
@WinRTXamlContentProperty("Example")
internal class ControlExample : UserControl() {
    private var ready = false
    private var sourceSample: GallerySampleCode? = null
    private var refreshSource: (() -> Unit)? = null
    var Xaml: String = ""
        set(value) { field = value; refreshSource?.invoke() }
    var XamlSource: String = ""
        set(value) { field = value; refreshSource?.invoke() }
    val Substitutions: MutableList<ControlExampleSubstitution> = mutableListOf()
    var ExampleHeight: GridLength = GridLength(1.0, GridUnitType.Star)
    var WebViewHeight: Int = 400
    var WebViewWidth: Int = 800

    var HeaderText: String = ""
        set(value) {
            field = value
            if (ready) {
                headerTextPresenter.text = value
                headerTextPresenter.visibility = if (value.isBlank()) Visibility.Collapsed else Visibility.Visible
            }
        }

    var SampleDefinition: String = ""
        set(value) {
            field = value
            sourceSample = GalleryCodeCatalog.sampleDefinition(value)
            sourceSample?.header?.takeIf(String::isNotBlank)?.let { HeaderText = it }
        }

    var Example: UIElement? = null
        set(value) {
            field = value
            if (ready) examplePresenter.content = value
        }

    var Output: UIElement? = null
        set(value) {
            field = value
            if (ready) {
                outputPresenter.content = value
                outputContainer.visibility = if (value == null) Visibility.Collapsed else Visibility.Visible
            }
        }

    var Options: UIElement? = null
        set(value) {
            field = value
            if (ready) {
                optionsPresenter.content = value
                optionsPresenter.visibility = if (value == null) Visibility.Collapsed else Visibility.Visible
            }
        }

    override fun initializeComponent() {
        super.initializeComponent()
        ready = true
        headerTextPresenter.text = HeaderText
        headerTextPresenter.visibility = if (HeaderText.isBlank()) Visibility.Collapsed else Visibility.Visible
        examplePresenter.content = Example
        outputPresenter.content = Output
        outputContainer.visibility = if (Output == null) Visibility.Collapsed else Visibility.Visible
        optionsPresenter.content = Options
        optionsPresenter.visibility = if (Options == null) Visibility.Collapsed else Visibility.Visible

        val state = GalleryTheme.sampleBeingConstructed
        val route = state?.sourceRoute
        val index = state?.sourceExampleIndex ?: 0
        if (state != null) {
            state.sourceExampleIndex++
            state.sampleBodies.add(examplePresenter)
        }
        var sourceReady = false
        fun updateSource() {
            val kotlin = sourceSample?.kotlin ?: route?.let { GalleryCodeCatalog.document(it, HeaderText, index) }
            val xaml = if (Xaml.isNotEmpty()) KotlinCodeDocument("sample.xaml", Xaml,
                listOf(KotlinCodeSpan(Xaml.length, KotlinCodeKind.Plain))) else
                GalleryCodeCatalog.sourceDocument(XamlSource) ?: sourceSample?.xaml ?: route?.let { GalleryCodeCatalog.xamlDocument(it, HeaderText, index) }
            if (kotlin != null) sourcePresenter.content = kotlinCodePreview(substitute(kotlin), xaml?.let(::substitute))
        }
        refreshSource = { if (sourceReady) updateSource() }
        sourcePresenter.expanding.add { _, _ ->
            if (!sourceReady) {
                updateSource()
                Substitutions.forEach { substitution -> substitution.addValueChanged { updateSource() } }
                sourceReady = true
            }
        }
    }

    private fun substitute(document: KotlinCodeDocument): KotlinCodeDocument {
        val replacements = Regex("""\$\(([^)]+)\)""").findAll(document.source).map { match ->
            match to requireNotNull(Substitutions.firstOrNull { it.Key == match.groupValues[1] }) {
                "Unknown sample substitution ${match.groupValues[1]} in ${document.fileName}"
            }.ValueAsString()
        }.toList()
        if (replacements.isEmpty()) return document
        val source = buildString {
            var start = 0
            replacements.forEach { (match, value) ->
                append(document.source, start, match.range.first); append(value)
                start = match.range.last + 1
            }
            append(document.source, start, document.source.length)
        }
        fun mapOffset(offset: Int): Int {
            var delta = 0
            for ((match, value) in replacements) {
                if (offset <= match.range.first) break
                if (offset <= match.range.last + 1) return match.range.first + delta + value.length
                delta += value.length - match.value.length
            }
            return offset + delta
        }
        var end = 0
        val spans = document.spans.mapNotNull { span ->
            val mapped = mapOffset(span.end)
            if (mapped <= end) null else KotlinCodeSpan(mapped, span.kind).also { end = mapped }
        }
        return document.copy(source = source, spans = spans)
    }
}
