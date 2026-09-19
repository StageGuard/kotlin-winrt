package io.github.composefluent.winrt.gallery.media

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.await
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.ScaleTransform
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.media.imaging.BitmapImage
import windows.foundation.Point
import windows.foundation.Size
import windows.media.capture.*
import windows.media.capture.frames.MediaFrameSourceGroup
import windows.media.capture.frames.MediaFrameSourceKind
import windows.media.core.MediaSource
import windows.media.mediaproperties.ImageEncodingProperties
import windows.storage.streams.InMemoryRandomAccessStream

@GalleryPage(route = "CaptureElementPreview", title = "Capture Element / Camera Preview", group = "Media", order = 1)
internal fun cameraPreviewPage() = ExamplePage {
    val tasks = GalleryPageTasks(this)
    example("MediaCapture preview displayed via MediaPlayerElement.", captureElementPreviewSample(tasks))
}

@GallerySample(route = "CaptureElementPreview", title = "MediaCapture preview displayed via MediaPlayerElement.")
internal fun captureElementPreviewSample(tasks: GalleryPageTasks) = StackPanel().apply { this.spacing = 0.0; val cameraLock = Mutex()
    var capture: MediaCapture? = null
    var previewSource: MediaSource? = null
    var devices = emptyList<MediaFrameSourceGroup>()
    var generation = 0
    val title = TextBlock().apply { this.text = ""; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
    val captured = TextBlock().apply { this.text = "Captured:"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { visibility = Visibility.Collapsed }
    val snapshots = StackPanel().apply { this.spacing = 2.0;  }
    val preview = MediaPlayerElement().apply { autoPlay = true; stretch = Stretch.Uniform }
    val cameras = ComboBox().apply { header = "Camera source" }
    val takePhoto = Button().apply { this.content = "Capture Photo" }.also { galleryButton -> galleryButton.click.add { _, _ -> tasks.launch {
            cameraLock.withLock {
                val current = capture ?: return@withLock
                val stream = InMemoryRandomAccessStream()
                try {
                    current.capturePhotoToStreamAsync(ImageEncodingProperties.createJpeg(), stream).await()
                    stream.seek(0uL)
                    val bitmap = BitmapImage()
                    bitmap.setSourceAsync(stream).await()
                    snapshots.children.add(0, Image().apply { source = bitmap })
                    captured.visibility = Visibility.Visible
                } finally { stream.close() }
            }
        } } }
    cameras.selectionChanged.add { _, _ ->
        val selected = devices.getOrNull(cameras.selectedIndex)
        if (selected != null) {
            val request = ++generation
            tasks.launch {
                cameraLock.withLock {
                    if (request != generation) return@withLock
                    takePhoto.isEnabled = false
                    preview.source = null
                    previewSource?.close(); previewSource = null
                    capture?.close(); capture = null
                    val candidate = MediaCapture()
                    var attached = false
                    try {
                        candidate.initializeAsync(MediaCaptureInitializationSettings().apply {
                            sourceGroup = selected; sharingMode = MediaCaptureSharingMode.SharedReadOnly
                            streamingCaptureMode = StreamingCaptureMode.Video; memoryPreference = MediaCaptureMemoryPreference.Cpu
                        }).await()
                        if (request == generation) {
                            val info = selected.sourceInfos.first { it.sourceKind == MediaFrameSourceKind.Color }
                            val frame = checkNotNull(candidate.frameSources[info.id])
                            previewSource = MediaSource.createFromMediaFrameSource(frame)
                            preview.source = previewSource
                            capture = candidate; attached = true; takePhoto.isEnabled = true
                            title.text = "Viewing: ${selected.displayName}"
                        }
                    } finally { if (!attached) candidate.close() }
                }
            }
        }
    }

    children.add(Grid().apply {
        minWidth = 400.0; minHeight = 300.0; columnSpacing = 4.0; rowSpacing = 10.0
        columnDefinitions.add(column(1.0, GridUnitType.Star)); columnDefinitions.add(column(100.0, GridUnitType.Pixel))
        rowDefinitions.add(autoRow()); rowDefinitions.add(starRow())
        children.add(title)
        Grid.setRow(preview, 1); children.add(preview)
        Grid.setColumn(captured, 1); children.add(captured)
        children.add(GallerySnapshotContainer().apply {
            Grid.setRow(this, 1); Grid.setColumn(this, 1)
            children.add(ScrollViewer().apply { this.content = snapshots; this.verticalScrollBarVisibility = ScrollBarVisibility.Auto; this.horizontalScrollBarVisibility = ScrollBarVisibility.Disabled; this.horizontalContentAlignment = HorizontalAlignment.Stretch; this.verticalContentAlignment = VerticalAlignment.Top })
        })
    })
    children.add(StackPanel().apply { this.spacing = 16.0; children.add(cameras)
        children.add(ToggleSwitch().apply {
            header = "Mirror preview"
            ToolTipService.setToolTip(this, "Mirrors only the preview, not captured photos")
            toggled.add { _, _ ->
                preview.renderTransform = if (isOn) ScaleTransform().apply { scaleX = -1.0 } else null
                preview.renderTransformOrigin = Point(0.5f, 0.5f)
            }
        })
        children.add(takePhoto.apply { isEnabled = false }) })
    loaded.add { _, _ ->
        tasks.launch {
            try {
                devices = MediaFrameSourceGroup.findAllAsync().await().filter { group ->
                    group.sourceInfos.any { it.sourceKind == MediaFrameSourceKind.Color }
                }
                if (devices.isEmpty()) title.text = "No camera devices found."
                else { cameras.itemsSource = devices.map { it.displayName }; cameras.selectedIndex = 0 }
                // Keep capture lifetime inside the page job so cancellation also releases it.
                kotlinx.coroutines.awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    cameraLock.withLock {
                        generation++
                        preview.source = null
                        previewSource?.close(); previewSource = null
                        capture?.close(); capture = null
                    }
                }
            }
        }
    } }

// Ported from WinUI Gallery Samples/CaptureElementPreview (MIT).


class GallerySnapshotContainer : Grid() {
    override fun measureOverride(availableSize: Size): Size = super.measureOverride(Size(availableSize.width, 100.0f))
}
