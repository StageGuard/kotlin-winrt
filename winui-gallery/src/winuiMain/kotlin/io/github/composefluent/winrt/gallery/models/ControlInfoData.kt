package io.github.composefluent.winrt.gallery.models

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.WinRTObservableList

internal class ControlInfoDataItem(private val page: GalleryPageInfo) {
    val UniqueId: String get() = page.id
    val Title: String get() = page.title
    val Subtitle: String get() = page.subtitle
    val Description: String get() = page.description
    val ImagePath: String get() = page.image
    val BadgeString: String get() = if (page.isNew) "New" else if (page.isUpdated) "Updated" else ""
    val Tags: List<String> get() = page.tags
    val ApiNamespace: String get() = page.apiNamespace
    val BaseClasses: List<String> get() = page.baseClasses
    val IsNew: Boolean get() = page.isNew
    val IsUpdated: Boolean get() = page.isUpdated
    val IsPreview: Boolean get() = false
    val IsExperimental: Boolean get() = page.isExperimental
    val IncludedInBuild: Boolean get() = true
    val SourcePath: String get() = page.sourcePath
    override fun toString(): String = Title
}
internal class ControlInfoDataGroup(val UniqueId: String, val Title: String, val IconGlyph: String,
    val Items: MutableList<ControlInfoDataItem>, val IsSpecialSection: Boolean = false) {
    override fun toString(): String = Title
}
internal object ControlInfoDataSource {
    val Groups: List<ControlInfoDataGroup> get() = GalleryCatalog.groups.map { group -> ControlInfoDataGroup(group.id, group.title, group.glyph, WinRTObservableList(group.pages.map(::ControlInfoDataItem))) }
    val Items: List<ControlInfoDataItem> get() = GalleryCatalog.pages.map(::ControlInfoDataItem)
}
