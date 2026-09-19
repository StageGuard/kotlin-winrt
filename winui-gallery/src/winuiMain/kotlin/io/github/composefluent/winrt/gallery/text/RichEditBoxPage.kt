package io.github.composefluent.winrt.gallery.text

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.await
import io.github.composefluent.winrt.runtime.asWinRT
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import microsoft.ui.text.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.input.StandardUICommand
import microsoft.ui.xaml.input.StandardUICommandKind
import microsoft.ui.xaml.media.FontFamily
import microsoft.windows.storage.pickers.FileOpenPicker
import microsoft.windows.storage.pickers.FileSavePicker
import microsoft.windows.storage.pickers.PickerLocationId
import windows.storage.FileAccessMode
import windows.storage.StorageFile
import windows.storage.CachedFileManager
import windows.storage.provider.FileUpdateStatus

@GalleryPage(route = "RichEditBox", title = "RichEditBox", group = "Text", order = 3)
internal fun richEditBoxPage() = ExamplePage {
    val tasks = GalleryPageTasks(this)
    example("A simple text editor with RichEditBox.", richEditBoxSimpleTextEditorWithRichEditBoxSample())
    val custom = richEditBoxCustomizingARichEditBoxCommandBarFlyoutSample1()
    var customMenuInitialized = false
    custom.loaded.add { _, _ ->
        if (customMenuInitialized) return@add
        customMenuInitialized = true
        listOfNotNull(custom.selectionFlyout, custom.contextFlyout).forEach { flyout ->
            val commands = flyout.asWinRT<CommandBarFlyout>()
            commands.primaryCommands.add(AppBarButton().apply { command = StandardUICommand(StandardUICommandKind.Share).asWinRT<microsoft.ui.xaml.input.ICommand>() })
        }
    }
    example("Customizing a RichEditBox CommandBarFlyout.", custom)
    example("A custom editor using RichEditBox.", richEditBoxCustomEditorSample(tasks))
    example("Math mode in RichEditBox.", richEditBoxMathModeInRichEditBoxSample3())
    val math = richEditBoxMathMlSample()
    val mathMlSource = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>x</mi><mo>∈</mo><mi>P</mi><mfenced><mi>A</mi></mfenced><mo>↔</mo><mi>x</mi><mo>⊆</mo><mi>A</mi></math>"

    example("Working with MathML in RichEditBox.", stack {
        children.add(label("SetMathML restores an equation. GetMathML retrieves its MathML representation when the equation occupies a single line."))
        children.add(math)
        children.add(label("MathML Code"))
        children.add(stack(8.0, true) {
            children.add(label(mathMlSource, 12.0).apply {
                fontFamily = FontFamily("Consolas")
                textWrapping = TextWrapping.Wrap
                isTextSelectionEnabled = true
            })
            children.add(copyButton(mathMlSource))
        })
    }, Button("Set sample formula") {
        // MathML is mathematical document data, not XAML UI markup.
        math.document!!.setMathML(mathMlSource)
    })


}

@GallerySample(route = "RichEditBox", title = "A simple text editor with RichEditBox.")
internal fun richEditBoxSimpleTextEditorWithRichEditBoxSample() = RichEditBox().apply { named(this, "simple text editor") }

@GallerySample(route = "RichEditBox", title = "Math mode in RichEditBox.")
internal fun richEditBoxMathModeInRichEditBoxSample3() = stack {
        children.add(TextBlock().apply { this.text = "Math mode recognizes and converts mathematical expressions as you type. For example, 4^2 becomes 4² and \\pi becomes π. Enabling math mode switches the input font to Cambria Math and clears existing content and undo history."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(RichEditBox().apply { width = 724.0; height = 80.0; fontSize = 16.0; textDocument!!.setMathMode(RichEditMathMode.MathOnly) })
    }

@GallerySample(route = "RichEditBox", title = "Customizing a RichEditBox CommandBarFlyout.")
internal fun richEditBoxCustomizingARichEditBoxCommandBarFlyoutSample1() = RichEditBox().apply { width = 800.0; height = 200.0; named(this, "editor with custom menu") }

@GallerySample(route = "RichEditBox", title = "Working with MathML in RichEditBox.")
internal fun richEditBoxMathMlSample() = RichEditBox().apply {
    width = 724.0; height = 80.0; fontSize = 16.0
    textDocument!!.setMathMode(RichEditMathMode.MathOnly)
}

@GallerySample(route = "RichEditBox", title = "A custom editor using RichEditBox.")
internal fun richEditBoxCustomEditorSample(tasks: GalleryPageTasks) = stack {
    val editor = RichEditBox().apply { height = 200.0; minWidth = 300.0; named(this, "Custom editor") }
    val colors = Flyout()
    var currentColor = rgb(0x008000u)
    colors.content = VariableSizedWrapGrid().apply {
        maximumRowsOrColumns = 3; orientation = Orientation.Horizontal
        listOf(0xFF0000u, 0xFFA500u, 0xFFFF00u, 0x008000u, 0x0000FFu, 0x4B0082u, 0xEE82EEu, 0x808080u).forEach { color ->
            children.add(Button().apply {
                padding = inset(0.0); margin = inset(6.0); minWidth = 0.0; minHeight = 0.0; content = tile(color, 32.0)
                click.add { _, _ -> currentColor = rgb(color); editor.document!!.selection!!.characterFormat!!.foregroundColor = currentColor; colors.hide(); editor.focus(FocusState.Keyboard) }
            })
        }
    }
    val find = TextBox().apply { width = 224.0; placeholderText = "Enter search text" }
    fun clearHighlights() {
        val range = editor.document!!.getRange(0, TextConstants.maxUnitCount)
        range.characterFormat!!.backgroundColor = GalleryTheme.brush("TextControlBackgroundFocused").asWinRT<microsoft.ui.xaml.media.SolidColorBrush>().color
        range.characterFormat!!.foregroundColor = GalleryTheme.brush("TextFillColorPrimaryBrush").asWinRT<microsoft.ui.xaml.media.SolidColorBrush>().color
    }
    fun findMatches() {
        clearHighlights()
        if (find.text.isEmpty()) return
        val range = editor.document!!.getRange(0, 0)
        while (range.findText(find.text, TextConstants.maxUnitCount, FindOptions.None) > 0) {
            range.characterFormat!!.backgroundColor = GalleryTheme.resource("SystemColorHighlightColor") as windows.ui.Color
            range.characterFormat!!.foregroundColor = GalleryTheme.resource("SystemColorHighlightTextColor") as windows.ui.Color
        }
    }
    find.textChanged.add { _, _ -> findMatches() }
    find.gotFocus.add { _, _ -> findMatches() }
    find.lostFocus.add { _, _ -> clearHighlights() }
    editor.textChanged.add { _, _ -> editor.document!!.selection!!.characterFormat!!.foregroundColor = currentColor }

    children.add(CommandBar().apply {
        primaryCommands.add(appCommand("Open file", Symbol.OpenFile) { tasks.launch {
            val picker = FileOpenPicker(checkNotNull(checkNotNull(editor.xamlRoot).contentIslandEnvironment).appWindowId).apply {
                suggestedStartLocation = PickerLocationId.DocumentsLibrary; fileTypeFilter.add(".rtf")
            }
            val result = picker.pickSingleFileAsync().await() ?: return@launch
            val file = StorageFile.getFileFromPathAsync(result.path).await()
            file.openAsync(FileAccessMode.Read).await().use { stream -> editor.document!!.loadFromStream(TextSetOptions.FormatRtf, stream) }
        } })
        primaryCommands.add(appCommand("Save file", Symbol.Save) { tasks.launch {
            val picker = FileSavePicker(checkNotNull(checkNotNull(editor.xamlRoot).contentIslandEnvironment).appWindowId).apply {
                suggestedStartLocation = PickerLocationId.DocumentsLibrary; suggestedFileName = "New Document"
                fileTypeChoices["Rich Text"] = mutableListOf(".rtf")
            }
            val result = picker.pickSaveFileAsync().await() ?: return@launch
            val file = StorageFile.getFileFromPathAsync(result.path).await()
            CachedFileManager.deferUpdates(file)
            var updateStatus: FileUpdateStatus? = null
            try {
                file.openAsync(FileAccessMode.ReadWrite).await().use { stream ->
                    stream.size = 0uL
                    editor.document!!.saveToStream(TextGetOptions.FormatRtf, stream)
                }
            } finally {
                withContext(NonCancellable) { updateStatus = CachedFileManager.completeUpdatesAsync(file).await() }
            }
            check(updateStatus == FileUpdateStatus.Complete) { "File ${file.name} couldn't be saved." }
        } })
        primaryCommands.add(appCommand("Bold", Symbol.Bold) { editor.document!!.selection!!.characterFormat!!.bold = FormatEffect.Toggle })
        primaryCommands.add(appCommand("Italic", Symbol.Italic) { editor.document!!.selection!!.characterFormat!!.italic = FormatEffect.Toggle })
        primaryCommands.add(appCommand("Font color", Symbol.FontColor).apply { flyout = colors })
    })
    children.add(editor)
    children.add(StackPanel().apply { this.spacing = 10.0; this.orientation = Orientation.Horizontal; children.add(TextBlock().apply { this.text = "Find:"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(find) })
}
