// Ported from WinUI Gallery Samples/{FlipView,GridView,ListView,TreeView,PullToRefresh} (MIT).
package io.github.composefluent.winrt.gallery

internal fun landscape(index: Int) = galleryPhotos[index % galleryPhotos.size].image
