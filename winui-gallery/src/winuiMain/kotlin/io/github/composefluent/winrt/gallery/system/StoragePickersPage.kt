package io.github.composefluent.winrt.gallery.system

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.await
import microsoft.ui.WindowId
import windows.storage.fileproperties.StorageItemThumbnail
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.media.imaging.BitmapImage
import microsoft.windows.storage.pickers.*
import windows.storage.CreationCollisionOption
import windows.storage.FileIO
import windows.storage.StorageFile
import windows.storage.StorageFolder
import windows.storage.fileproperties.ThumbnailMode
import windows.storage.fileproperties.ThumbnailOptions

@GalleryPage(route = "StoragePickers", title = "Storage pickers", group = "System", order = 2)
internal fun storagePickersPage() = ExamplePage {
    val owner = this
    val tasks = GalleryPageTasks(owner)
    fun input(title: String, initial: String, placeholder: String = "") = TextBox().apply {
        header = title
        text = initial
        placeholderText = placeholder
    }
    children.add(InfoBar().apply {
        isClosable = false
        margin = Thickness(0.0, 8.0, 0.0, 0.0)
        content = label("The picker reopens in the last selected location and view. The SuggestedStartLocation and ViewMode are only applied the first time the picker is opened (for example, right after app installation or when no previous selection exists).").apply {
            margin = Thickness(0.0, 12.0, 12.0, 12.0)
        }
        isOpen = true
    })
    repeat(2) { index ->
        val multiple = index == 1
        var filter = 0; var location = 0; var view = 0
        val commit = input("Commit button text", if (multiple) "Pick Files" else "Pick File", "Open")
        val output = label(if (multiple) "No files picked" else "No file picked")
        val pick = if (multiple) storagePickerMultipleFilesDemo(owner, tasks, { filter }, { location }, { view }, commit, output)
        else storagePickerSingleFileDemo(owner, tasks, { filter }, { location }, { view }, commit, output)
        val visual = stack(8.0) { children.add(pick); children.add(output) }
        val options = stack(8.0) {
            children.add(select("File type", listOf("All Files (*)", "Text Files (*.txt)", "Images (*.jpg, *.png)")) { filter = it }.apply { width = 200.0 })
            children.add(commit)
            children.add(select("Suggested start location", pickerLocationNames) { location = it }.apply { width = 200.0 })
            children.add(select("View mode", listOf("List", "Thumbnail")) { view = it }.apply { width = 200.0 })
        }
        if (multiple) example("Pick multiple files.", visual, options)
        else example("Pick a single file.", visual, options)
    }
    val fileContent = input("File content", "Hello, WinUI!").apply {
        width = 500.0; height = 200.0; acceptsReturn = true
        textWrapping = TextWrapping.Wrap; isSpellCheckEnabled = false
        horizontalAlignment = HorizontalAlignment.Left
    }
    val extensions = listOf(".txt", ".json", ".xml")
    val typeNames = listOf("Text Files", "JSON Files", "XML Files")
    val enabledTypes = BooleanArray(3)
    var defaultExtension = 0; var saveLocation = 0
    val fileName = input("Suggested file name", "NewDocument")
    val saveCommit = input("Commit button text", "Save File", "Save")
    val folder = input("Suggested folder", "", "Optional").apply {
        width = 148.0; isReadOnly = true
        foreground = GalleryTheme.brush("AccentTextFillColorPrimaryBrush")
    }
    val saved = label("No file saved")
    val saveSample = storageSaveFileDemo(owner, tasks, fileContent, enabledTypes, { defaultExtension }, { saveLocation }, fileName, saveCommit, folder, saved)
    example("Save a file.", saveSample, stack(8.0) {
        children.add(label("File types:"))
        typeNames.forEachIndexed { index, title -> children.add(option("$title (*${extensions[index]})") { enabledTypes[index] = it }) }
        children.add(select("Default extension", extensions) { defaultExtension = it })
        children.add(fileName); children.add(saveCommit)
        children.add(select("Suggested start location", pickerLocationNames) { saveLocation = it })
        children.add(stack(8.0, true) {
            children.add(folder)
            children.add(Button().apply {
                content = glyph("\uF89A")
                verticalAlignment = VerticalAlignment.Bottom
                named(this, "Select folder")
                click.add { _, _ ->
                    tasks.launch {
                        val selected = FolderPicker(checkNotNull(checkNotNull(owner.xamlRoot).contentIslandEnvironment).appWindowId)
                            .apply { commitButtonText = "Select folder" }
                            .pickSingleFolderAsync().await()
                        if (selected != null) folder.text = selected.path
                    }
                }
            })
        })
    })
    var folderLocation = 0; var folderView = 0
    val folderCommit = input("Commit button text", "Pick Folder", "Select Folder")
    val pickedFolder = label("No folder picked")

    val folderSample = storagePickFolderDemo(owner, tasks, folderCommit, { folderLocation }, { folderView }, pickedFolder)
    example("Pick a folder.", folderSample, stack(8.0) {
        children.add(folderCommit)
        children.add(select("Suggested start location", pickerLocationNames) { folderLocation = it })
        children.add(select("View mode", listOf("List", "Thumbnail")) { folderView = it })
    })
    val modes = listOf(ThumbnailMode.PicturesView, ThumbnailMode.VideosView, ThumbnailMode.MusicView, ThumbnailMode.DocumentsView, ThumbnailMode.ListView, ThumbnailMode.SingleItem)
    val names = listOf("PicturesView", "VideosView", "MusicView", "DocumentsView", "ListView", "SingleItem")
    var thumbnailMode = 0
    val size = NumberBox().apply {
        width = 200.0
        header = "Requested size (px)"; value = 200.0; minimum = 16.0; maximum = 1024.0
        spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline
    }
    val image = Image().apply { stretch = Stretch.Uniform; named(this, "File thumbnail") }
    val details = label("No file picked")

    val thumbnailSample = storageThumbnailDemo(owner, tasks, modes, names, { thumbnailMode }, size, image, details)
    example("File thumbnail.", thumbnailSample, stack(8.0) { children.add(select("Thumbnail mode", names) { thumbnailMode = it }.apply { width = 200.0 }); children.add(size) })



}

private fun storagePickerSingleFileDemo(
    owner: FrameworkElement,
    tasks: GalleryPageTasks,
    filter: () -> Int,
    location: () -> Int,
    view: () -> Int,
    commit: TextBox,
    output: TextBlock,
) = Button().apply {
    content = "Pick a single file"
    click.add { _, _ ->
        isEnabled = false
        tasks.launch {
            try {
                output.text = storagePickerSingleFileSample(
                    checkNotNull(checkNotNull(owner.xamlRoot).contentIslandEnvironment).appWindowId,
                    listOf(listOf("*"), listOf(".txt"), listOf(".jpg", ".png"))[filter()],
                    commit.text, pickerLocations[location()], pickerViews[view()],
                ).invoke()?.let { "Picked: ${it.path}" } ?: "No file selected."
            } finally {
                isEnabled = true
            }
        }
    }
}

private fun storagePickerMultipleFilesDemo(
    owner: FrameworkElement,
    tasks: GalleryPageTasks,
    filter: () -> Int,
    location: () -> Int,
    view: () -> Int,
    commit: TextBox,
    output: TextBlock,
) = Button().apply {
    content = "Pick multiple files"
    click.add { _, _ ->
        isEnabled = false
        tasks.launch {
            try {
                val files = storagePickerMultipleFilesSample(
                    checkNotNull(checkNotNull(owner.xamlRoot).contentIslandEnvironment).appWindowId,
                    listOf(listOf("*"), listOf(".txt"), listOf(".jpg", ".png"))[filter()],
                    commit.text, pickerLocations[location()], pickerViews[view()],
                ).invoke()
                output.text = if (files.isEmpty()) "No files selected." else files.joinToString("\n") { "- Picked: ${it.path}" }
            } finally {
                isEnabled = true
            }
        }
    }
}

private fun storageSaveFileDemo(
    owner: FrameworkElement,
    tasks: GalleryPageTasks,
    fileContent: TextBox,
    enabledTypes: BooleanArray,
    defaultExtension: () -> Int,
    saveLocation: () -> Int,
    fileName: TextBox,
    saveCommit: TextBox,
    folder: TextBox,
    saved: TextBlock,
) = stack(8.0) {
    children.add(fileContent)
    children.add(Button().apply {
        content = "Save a file"
        click.add { _, _ ->
            isEnabled = false
            tasks.launch {
                try {
                    val extensions = listOf(".txt", ".json", ".xml")
                    val typeNames = listOf("Text Files", "JSON Files", "XML Files")
                    val choices = typeNames.indices.filter { enabledTypes[it] }.associate { typeNames[it] to listOf(extensions[it]) }
                    val path = storageSaveFileSample(
                        checkNotNull(checkNotNull(owner.xamlRoot).contentIslandEnvironment).appWindowId,
                        fileContent.text, choices, extensions[defaultExtension()], fileName.text,
                        saveCommit.text, pickerLocations[saveLocation()], folder.text,
                    ).invoke()
                    saved.text = path?.let { "File saved to: $it" } ?: "File save canceled."
                } finally {
                    isEnabled = true
                }
            }
        }
    })
    children.add(saved)
}

private fun storagePickFolderDemo(
    owner: FrameworkElement,
    tasks: GalleryPageTasks,
    commit: TextBox,
    location: () -> Int,
    view: () -> Int,
    pickedFolder: TextBlock,
) = stack(8.0) {
    children.add(Button().apply {
        content = "Pick a folder"
        click.add { _, _ ->
            isEnabled = false
            tasks.launch {
                try {
                    val result = storagePickFolderSample(
                        checkNotNull(checkNotNull(owner.xamlRoot).contentIslandEnvironment).appWindowId,
                        commit.text, pickerLocations[location()], pickerViews[view()],
                    ).invoke()
                    pickedFolder.text = result?.let { "Picked: ${it.path}" } ?: "No folder selected."
                } finally {
                    isEnabled = true
                }
            }
        }
    })
    children.add(pickedFolder)
}

private fun storageThumbnailDemo(
    owner: FrameworkElement,
    tasks: GalleryPageTasks,
    modes: List<ThumbnailMode>,
    names: List<String>,
    thumbnailMode: () -> Int,
    size: NumberBox,
    image: Image,
    details: TextBlock,
) = stack(8.0, true) {
    children.add(stack(8.0) {
        children.add(Button().apply {
            content = "Pick a file"
            click.add { _, _ ->
                isEnabled = false
                tasks.launch {
                    try {
                        val result = FileOpenPicker(checkNotNull(checkNotNull(owner.xamlRoot).contentIslandEnvironment).appWindowId)
                            .apply { fileTypeFilter.add("*") }
                            .pickSingleFileAsync().await()
                        if (result == null) {
                            details.text = "No file selected."
                            image.source = null
                        } else {
                            val file = StorageFile.getFileFromPathAsync(result.path).await()
                            val pixels = if (size.value.isNaN() || size.value <= 0.0) 200u else size.value.toUInt()
                            val thumbnail = storageThumbnailSample(file, modes[thumbnailMode()], pixels).invoke()
                            if (thumbnail == null) {
                                image.source = null
                                details.text = "No thumbnail available for the selected file."
                            } else try {
                                val bitmap = BitmapImage()
                                bitmap.setSourceAsync(thumbnail).await()
                                image.source = bitmap
                                details.text = "File: ${file.name}\nMode: ThumbnailMode.${names[thumbnailMode()]}\nRequested size: $pixels\nReturned size: ${thumbnail.originalWidth} x ${thumbnail.originalHeight}"
                            } finally {
                                thumbnail.close()
                            }
                        }
                    } finally {
                        isEnabled = true
                    }
                }
            }
        })
        children.add(details)
    })
    children.add(Border().apply {
        width = 160.0
        height = 160.0
        background = GalleryTheme.brush("SubtleFillColorTertiaryBrush")
        cornerRadius = corners(4.0)
        horizontalAlignment = HorizontalAlignment.Left
        child = image
    })
}

// Ported from WinUI Gallery Samples/StoragePickers (MIT).


internal val pickerLocations = listOf(PickerLocationId.DocumentsLibrary, PickerLocationId.ComputerFolder, PickerLocationId.Desktop, PickerLocationId.Downloads, PickerLocationId.MusicLibrary, PickerLocationId.PicturesLibrary, PickerLocationId.VideosLibrary, PickerLocationId.Objects3D, PickerLocationId.Unspecified)
internal val pickerLocationNames = listOf("DocumentsLibrary", "ComputerFolder", "Desktop", "Downloads", "MusicLibrary", "PicturesLibrary", "VideosLibrary", "Objects3D", "Unspecified")
internal val pickerViews = listOf(PickerViewMode.List, PickerViewMode.Thumbnail)


@GallerySample(route = "StoragePickers", title = "Pick a single file.")
internal fun storagePickerSingleFileSample(
    windowId: WindowId,
    extensions: List<String>,
    commitText: String,
    startLocation: PickerLocationId,
    view: PickerViewMode,
) = suspend {
    val picker = FileOpenPicker(windowId).apply {
        extensions.forEach { fileTypeFilter.add(it) }
        commitButtonText = commitText
        suggestedStartLocation = startLocation
        viewMode = view
    }
    picker.pickSingleFileAsync().await()
}

@GallerySample(route = "StoragePickers", title = "Pick multiple files.")
internal fun storagePickerMultipleFilesSample(
    windowId: WindowId,
    extensions: List<String>,
    commitText: String,
    startLocation: PickerLocationId,
    view: PickerViewMode,
) = suspend {
    val picker = FileOpenPicker(windowId).apply {
        extensions.forEach { fileTypeFilter.add(it) }
        commitButtonText = commitText
        suggestedStartLocation = startLocation
        viewMode = view
    }
    picker.pickMultipleFilesAsync().await()
}

@GallerySample(route = "StoragePickers", title = "Save a file.")
internal fun storageSaveFileSample(
    windowId: WindowId,
    text: String,
    types: Map<String, List<String>>,
    extension: String,
    name: String,
    commitText: String,
    startLocation: PickerLocationId,
    folder: String,
) = suspend {
    val picker = FileSavePicker(windowId).apply {
        types.forEach { (title, extensions) -> fileTypeChoices[title] = extensions.toMutableList() }
        defaultFileExtension = extension
        suggestedFileName = name
        commitButtonText = commitText
        suggestedStartLocation = startLocation
        suggestedFolder = folder
    }
    val result = picker.pickSaveFileAsync().await()
    if (result == null) null else {
        val directory = StorageFolder.getFolderFromPathAsync(result.path.substringBeforeLast('\\')).await()
        val file = directory.createFileAsync(result.path.substringAfterLast('\\'), CreationCollisionOption.ReplaceExisting).await()
        FileIO.writeTextAsync(file, text).await()
        result.path
    }
}

@GallerySample(route = "StoragePickers", title = "Pick a folder.")
internal fun storagePickFolderSample(
    windowId: WindowId,
    commitText: String,
    startLocation: PickerLocationId,
    view: PickerViewMode,
) = suspend {
    val picker = FolderPicker(windowId).apply {
        commitButtonText = commitText
        suggestedStartLocation = startLocation
        viewMode = view
    }
    picker.pickSingleFolderAsync().await()
}

@GallerySample(route = "StoragePickers", title = "File thumbnail.")
internal fun storageThumbnailSample(
    file: StorageFile,
    mode: ThumbnailMode,
    requestedSize: UInt,
): suspend () -> StorageItemThumbnail? = suspend {
    // Dispose the returned thumbnail after reading its stream.
    file.getThumbnailAsync(mode, requestedSize, ThumbnailOptions.UseCurrentScale).await()
}
