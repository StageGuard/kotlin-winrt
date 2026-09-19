package io.github.composefluent.winrt.gallery.navigation

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*

@GalleryPage(route = "BreadcrumbBar", title = "BreadcrumbBar", group = "Navigation", order = 0)
internal fun breadcrumbBarPage() = ExamplePage {
    val navigation = breadcrumbBarFoldersSample()
    example("A BreadcrumbBar control.", breadcrumbBarBreadcrumbBarControlSample())
    example("A BreadcrumbBar with navigable folder items.", navigation, Button("Reset sample") {
        navigation.itemsSource = breadcrumbFolders
        announce(navigation, "BreadcrumbBar sample reset successful.", "BreadCrumbBarSampleResetNotificationId")
    })
}

private val breadcrumbFolders = listOf("Home", "Folder1", "Folder2", "Folder3")

@GallerySample(route = "BreadcrumbBar", title = "A BreadcrumbBar with navigable folder items.")
internal fun breadcrumbBarFoldersSample() = BreadcrumbBar().apply {
    itemsSource = breadcrumbFolders
    itemClicked.add { _, args -> itemsSource = breadcrumbFolders.take(args.index + 1) }
}

@GallerySample(route = "BreadcrumbBar", title = "A BreadcrumbBar control.")
internal fun breadcrumbBarBreadcrumbBarControlSample() = BreadcrumbBar().apply {
        itemsSource = listOf("Home", "Documents", "Design", "Northwind", "Images", "Folder1", "Folder2", "Folder3")
    }
