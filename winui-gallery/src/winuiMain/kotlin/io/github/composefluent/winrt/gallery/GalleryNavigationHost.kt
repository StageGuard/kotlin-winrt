package io.github.composefluent.winrt.gallery

/** App-owned route dispatch for samples that link to another Gallery page. */
internal object GalleryNavigationHost {
    var navigate: (String) -> Unit = { error("Gallery navigation has not been initialized") }
}
