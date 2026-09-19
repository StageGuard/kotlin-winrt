package io.github.composefluent.winrt.gallery.text

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "AutoSuggestBox", title = "AutoSuggestBox", group = "Text", order = 0)
internal fun autoSuggestBoxPage() = ExamplePage {
    val output = label("")
    example("A basic AutoSuggestBox.", autoSuggestBasicSample(output), output = output)
    example("An AutoSuggestBox providing a search experience.", autoSuggestSearchSample())
}

@GallerySample(route = "AutoSuggestBox", title = "A basic AutoSuggestBox.")
internal fun autoSuggestBasicSample(output: TextBlock) = AutoSuggestBox().apply {
    width = 300.0
    named(this, "Basic AutoSuggestBox")
    textChanged.add { _, args ->
        if (args.reason == AutoSuggestionBoxTextChangeReason.UserInput) {
            val words = text.split(' ')
            itemsSource = galleryCats.filter { cat -> words.all { cat.contains(it, true) } }
                .ifEmpty { listOf("No results found") }
        }
    }
    suggestionChosen.add { _, args -> output.text = args.selectedItem?.toString().orEmpty() }
}

@GallerySample(route = "AutoSuggestBox", title = "An AutoSuggestBox providing a search experience.")
internal fun autoSuggestSearchSample() = stack {
    val details = ContentControl()
    val search = AutoSuggestBox().apply {
        width = 300.0
        placeholderText = "Type a control name"
        queryIcon = SymbolIcon(Symbol.Find)
    }
    fun results() = GalleryCatalog.pages.filter { page ->
        search.text.split(' ').all { page.title.contains(it, true) }
    }.sortedWith(compareBy({ !it.title.startsWith(search.text, true) }, { it.title }))
    search.textChanged.add { _, args ->
        if (args.reason == AutoSuggestionBoxTextChangeReason.UserInput) {
            search.itemsSource = results().map { it.title }.ifEmpty { listOf("No results found") }
        }
    }
    search.querySubmitted.add { _, args ->
        val selected = GalleryCatalog.pages.firstOrNull { it.title == args.chosenSuggestion?.toString() }
            ?: results().firstOrNull()
        details.content = selected?.let { page -> StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; if (page.image.isNotBlank()) children.add(Image().apply { this.width = 75.0; this.height = 75.0; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri(page.image) ) })
            children.add(StackPanel().apply { this.spacing = 4.0; children.add(TextBlock().apply { this.text = page.title; this.fontSize = 18.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
                children.add(TextBlock().apply { this.text = page.subtitle; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }) } }
    }
    children.add(search)
    children.add(details)
}
