package io.github.composefluent.winrt.gallery.system

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.await
import io.github.composefluent.winrt.runtime.WinRTOut
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import microsoft.ui.text.TextGetOptions
import microsoft.ui.text.TextSetOptions
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.media.imaging.BitmapImage
import microsoft.windows.storage.pickers.FileOpenPicker
import windows.applicationmodel.datatransfer.*
import windows.foundation.Uri
import windows.storage.IStorageItem
import windows.storage.StorageFile
import windows.storage.streams.RandomAccessStreamReference

@GalleryPage(route = "Clipboard", title = "Clipboard", group = "System", order = 0)
internal fun clipboardPage() = ExamplePage {
    val owner = this
    val tasks = GalleryPageTasks(owner)
    val editor = RichEditBox().apply {
        width = 800.0
        height = 100.0
        document!!.setText(TextSetOptions.None, "This text will be copied to the clipboard.")
        named(this, "editor with custom menu")
    }
    val confirmation = label("Text copied to clipboard!").apply {
        visibility = Visibility.Collapsed
        padding = Thickness(20.0, 5.0, 0.0, 0.0)
    }
    val confirmationJob = arrayOfNulls<Job>(1)
    example("Copy text to the clipboard.", clipboardCopyTextDemo(tasks, editor, confirmation, confirmationJob))

    val pastedText = label("Click the button!").apply { padding = Thickness(5.0, 5.0, 0.0, 0.0) }
    example("Paste text from the clipboard.", clipboardPasteTextDemo(tasks, pastedText))

    val copyImageStatus = label("").apply { visibility = Visibility.Collapsed; foreground = GalleryTheme.brush("SystemFillColorSuccessBrush") }
    example("Copy an image to the clipboard.", clipboardCopyImageDemo(copyImageStatus))
    val pasteImageStatus = label("").apply { visibility = Visibility.Collapsed; foreground = GalleryTheme.brush("SystemFillColorSuccessBrush") }
    val pastedImage = Image().apply {
        width = 200.0; height = 150.0
        horizontalAlignment = HorizontalAlignment.Left
        stretch = Stretch.UniformToFill
        visibility = Visibility.Collapsed
        named(this, "Pasted image from clipboard")
    }
    example("Paste an image from the clipboard.", clipboardPasteImageDemo(tasks, pasteImageStatus, pastedImage))
    example("Copy files to the clipboard.", clipboardCopyFilesDemo(owner, tasks, label("")))
    example("Paste files from the clipboard.", clipboardPasteFilesDemo(tasks, label("")))

    val text = TextBox().apply {
        width = 400.0
        horizontalAlignment = HorizontalAlignment.Left
        placeholderText = "Enter text to copy with options"
        text = "Text with clipboard options"
        named(this, "Text to copy with options")
    }
    val history = ToggleSwitch().apply { header = "Allow in History"; isOn = true }
    val roaming = ToggleSwitch().apply { header = "Allow Roaming"; isOn = true }
    val optionsStatus = label("")
    example("Clipboard history and roaming options.", clipboardHistoryDemo(text, history, roaming, optionsStatus))
    example("Show clipboard formats.", clipboardFormatsDemo(label("")))
    example("Clear the clipboard.", clipboardClearDemo(label("")))
    example("Monitor clipboard changes.", clipboardMonitorDemo(owner, label("")))
}

private fun clipboardCopyTextDemo(
    tasks: GalleryPageTasks,
    editor: RichEditBox,
    confirmation: TextBlock,
    confirmationJob: Array<Job?>,
) = stack(0.0) {
    children.add(stack(0.0, true) {
        children.add(Button("Copy Text to the Clipboard") {
            val text = WinRTOut<String>()
            editor.document!!.getText(TextGetOptions.None, text)
            clipboardCopyTextSample(text.value)
            confirmation.visibility = Visibility.Visible
            confirmationJob[0]?.cancel()
            confirmationJob[0] = tasks.launch {
                delay(2000)
                confirmation.visibility = Visibility.Collapsed
            }
        }.apply { margin = Thickness(0.0, 0.0, 0.0, 10.0) })
        children.add(confirmation)
    })
    children.add(editor)
}

private fun clipboardPasteTextDemo(tasks: GalleryPageTasks, pastedText: TextBlock) = stack(0.0) {
    children.add(Button("Paste Text from the Clipboard") { tasks.launch {
        clipboardPasteTextSample().invoke()?.let { pastedText.text = it }
    } }.apply { margin = Thickness(0.0, 0.0, 0.0, 10.0) })
    children.add(label("Clipboard:"))
    children.add(pastedText)
}

private fun clipboardCopyImageDemo(status: TextBlock) = stack(10.0) {
    children.add(picture("ms-appx:///Assets/SampleMedia/rainier.jpg", 200.0).apply {
        height = 150.0; horizontalAlignment = HorizontalAlignment.Left
        stretch = Stretch.UniformToFill
        named(this, "Source image to copy")
    })
    children.add(Button("Copy Image to Clipboard") {
        status.text = if (clipboardCopyImageSample(Uri("ms-appx:///Assets/SampleMedia/rainier.jpg")))
            "Image copied to clipboard." else "Error copying image to clipboard."
        status.visibility = Visibility.Visible
    })
    children.add(status)
}

private fun clipboardPasteImageDemo(tasks: GalleryPageTasks, status: TextBlock, image: Image) = stack(10.0) {
    children.add(Button("Paste Image from Clipboard") { tasks.launch {
        val bitmap = clipboardPasteImageSample().invoke()
        if (bitmap != null) {
            image.source = bitmap; image.visibility = Visibility.Visible
            status.text = "Image pasted from clipboard."
        } else {
            image.visibility = Visibility.Collapsed
            status.text = "Bitmap format is not available in the clipboard."
        }
        status.visibility = Visibility.Visible
    } })
    children.add(status)
    children.add(image)
}

private fun clipboardCopyFilesDemo(owner: FrameworkElement, tasks: GalleryPageTasks, status: TextBlock) = stack(10.0) {
    children.add(Button("Copy Files to Clipboard") { tasks.launch {
        val picker = FileOpenPicker(checkNotNull(checkNotNull(owner.xamlRoot).contentIslandEnvironment).appWindowId)
            .apply { fileTypeFilter.add("*") }
        val files = picker.pickMultipleFilesAsync().await()
        if (files.isNotEmpty()) {
            val items = mutableListOf<IStorageItem>()
            for (file in files) items.add(StorageFile.getFileFromPathAsync(file.path).await())
            status.text = if (clipboardCopyFilesSample(items))
                "${items.size} file(s) copied to clipboard." else "Error copying files to clipboard."
        }
    } })
    children.add(status)
}

private fun clipboardPasteFilesDemo(tasks: GalleryPageTasks, status: TextBlock) = stack(10.0) {
    children.add(Button("Paste Files from Clipboard") { tasks.launch {
        val data = Clipboard.getContent()
        if (data.contains(StandardDataFormats.storageItems)) {
            val files = clipboardPasteFilesSample().invoke()
            status.text = "Requested operation: ${data.requestedOperation}\nFile(s) on clipboard (${files.size}):\n" +
                files.joinToString("\n") { "  • ${it.name}" }
        } else status.text = "StorageItems format is not available in the clipboard."
    } })
    children.add(status)
}

private fun clipboardHistoryDemo(text: TextBox, history: ToggleSwitch, roaming: ToggleSwitch, optionsStatus: TextBlock) = stack(10.0) {
    children.add(text)
    children.add(stack(16.0, true) { children.add(history); children.add(roaming) })
    children.add(Button("Copy with Options") {
        optionsStatus.text = if (text.text.isEmpty()) "Please enter text to copy."
        else if (clipboardHistorySample(text.text, history.isOn, roaming.isOn)) {
            "Text copied to clipboard. History: ${if (history.isOn) "allowed" else "excluded"}. Roaming: ${if (roaming.isOn) "allowed" else "excluded"}."
        } else "Error copying content to clipboard."
    })
    children.add(optionsStatus)
    children.add(stack(16.0, true) {
        children.add(label("Clipboard history: ${if (Clipboard.isHistoryEnabled()) "enabled" else "disabled"}"))
        children.add(label("Clipboard roaming: ${if (Clipboard.isRoamingEnabled()) "enabled" else "disabled"}"))
    })
}

private fun clipboardFormatsDemo(status: TextBlock) = stack(8.0) {
    children.add(Button("Show Clipboard Formats") {
        val formats = clipboardFormatsSample()
        status.text = if (formats.isEmpty()) "The clipboard is empty."
        else "Available formats on the clipboard:\n" + formats.joinToString("\n") { "  • $it" }
    })
    children.add(status)
}

private fun clipboardClearDemo(status: TextBlock) = stack(8.0) {
    children.add(Button("Clear Clipboard") {
        clipboardClearSample()
        status.text = "Clipboard has been cleared."
    })
    children.add(status)
}

private fun clipboardMonitorDemo(owner: FrameworkElement, status: TextBlock) = stack(8.0) {
    val monitor = ToggleSwitch().apply { header = "Monitor ContentChanged" }
    clipboardMonitorSample(owner) {
        if (monitor.isOn) owner.dispatcherQueue?.tryEnqueue {
            if (monitor.isOn && owner.isLoaded)
                status.text = "Clipboard content changed. Formats: " + Clipboard.getContent().availableFormats.joinToString(", ")
        }
    }
    monitor.toggled.add { _, _ ->
        status.text = if (monitor.isOn) "Monitoring clipboard changes..." else "Stopped monitoring clipboard changes."
    }
    owner.unloaded.add { _, _ -> monitor.isOn = false }
    children.add(monitor)
    children.add(status)
}

@GallerySample(route = "Clipboard", title = "Copy text to the clipboard.")
internal fun clipboardCopyTextSample(text: String) = run {
    val content = DataPackage().apply { setText(text) }
    Clipboard.setContent(content)
}

@GallerySample(route = "Clipboard", title = "Paste text from the clipboard.")
internal fun clipboardPasteTextSample() = suspend {
    val content = Clipboard.getContent()
    if (content.contains(StandardDataFormats.text)) content.getTextAsync().await() else null
}

@GallerySample(route = "Clipboard", title = "Copy an image to the clipboard.")
internal fun clipboardCopyImageSample(uri: Uri): Boolean = run {
    val content = DataPackage().apply {
        setBitmap(RandomAccessStreamReference.createFromUri(uri))
    }
    return@run Clipboard.setContentWithOptions(content, ClipboardContentOptions())
}

@GallerySample(route = "Clipboard", title = "Paste an image from the clipboard.")
internal fun clipboardPasteImageSample() = suspend {
    val content = Clipboard.getContent()
    if (!content.contains(StandardDataFormats.bitmap)) null
    else {
        val stream = content.getBitmapAsync().await().openReadAsync().await()
        try {
            BitmapImage().apply { setSourceAsync(stream).await() }
        } finally {
            stream.close()
        }
    }
}

@GallerySample(route = "Clipboard", title = "Copy files to the clipboard.")
internal fun clipboardCopyFilesSample(files: List<IStorageItem>): Boolean = run {
    val content = DataPackage().apply {
        setStorageItems(files)
        requestedOperation = DataPackageOperation.Copy
    }
    return@run Clipboard.setContentWithOptions(content, ClipboardContentOptions())
}

@GallerySample(route = "Clipboard", title = "Paste files from the clipboard.")
internal fun clipboardPasteFilesSample() = suspend {
    val content = Clipboard.getContent()
    if (content.contains(StandardDataFormats.storageItems)) content.getStorageItemsAsync().await() else emptyList()
}

@GallerySample(route = "Clipboard", title = "Clipboard history and roaming options.")
internal fun clipboardHistorySample(text: String, allowHistory: Boolean, allowRoaming: Boolean): Boolean = run {
    val content = DataPackage().apply { setText(text) }
    val options = ClipboardContentOptions().apply {
        isAllowedInHistory = allowHistory
        isRoamable = allowRoaming
    }
    return@run Clipboard.setContentWithOptions(content, options)
}

@GallerySample(route = "Clipboard", title = "Show clipboard formats.")
internal fun clipboardFormatsSample(): List<String> = Clipboard.getContent().availableFormats

@GallerySample(route = "Clipboard", title = "Clear the clipboard.")
internal fun clipboardClearSample() = Clipboard.clear()

@GallerySample(route = "Clipboard", title = "Monitor clipboard changes.")
internal fun clipboardMonitorSample(owner: FrameworkElement, onChanged: () -> Unit) = run {
    val subscription = Clipboard.contentChanged.add { _, _ -> onChanged() }
    owner.unloaded.add { _, _ -> Clipboard.contentChanged.remove(subscription) }
}
