package io.github.composefluent.winrt.gallery.media

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.await
import microsoft.ui.xaml.controls.*
import microsoft.windows.storage.pickers.FileOpenPicker
import windows.foundation.Uri
import windows.media.core.MediaSource
import windows.storage.StorageFile

@GalleryPage(route = "MediaPlayerElement", title = "MediaPlayerElement", group = "Media", order = 4)
internal fun mediaPlayerElementPage() = ExamplePage {
    val tasks = GalleryPageTasks(this)
    val first = mediaPlayerElementMediaPlayerElementWithTransportControlsSample()
    val second = mediaPlayerElementMediaPlayerElementThatAutoplaysAVideoSample1()
    example("A MediaPlayerElement with transport controls.", first, Button("Open a file") {
        tasks.launch {
            val picker = FileOpenPicker(checkNotNull(checkNotNull(first.xamlRoot).contentIslandEnvironment).appWindowId)
            val file = picker.pickSingleFileAsync().await() ?: return@launch
            first.mediaPlayer?.pause()
            first.source = MediaSource.createFromStorageFile(StorageFile.getFileFromPathAsync(file.path).await())
        }
    })
    example("A MediaPlayerElement that autoplays a video.", second)
    unloaded.add { _, _ ->
        first.mediaPlayer?.pause(); second.mediaPlayer?.pause()
        first.source = null; second.source = null
    }
}

@GallerySample(route = "MediaPlayerElement", title = "A MediaPlayerElement with transport controls.")
internal fun mediaPlayerElementMediaPlayerElementWithTransportControlsSample() = MediaPlayerElement().apply {
        maxWidth = 400.0; areTransportControlsEnabled = true; autoPlay = false
        source = MediaSource.createFromUri(Uri("ms-appx:///Assets/SampleMedia/ladybug.wmv"))
    }

@GallerySample(route = "MediaPlayerElement", title = "A MediaPlayerElement that autoplays a video.")
internal fun mediaPlayerElementMediaPlayerElementThatAutoplaysAVideoSample1() = MediaPlayerElement().apply {
        maxWidth = 400.0; autoPlay = true
        source = MediaSource.createFromUri(Uri("ms-appx:///Assets/SampleMedia/fishes.wmv"))
    }
