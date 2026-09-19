package io.github.composefluent.winrt.gallery.menusandtoolbars

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.input.KeyboardAccelerator
import microsoft.ui.xaml.media.*
import windows.foundation.Point
import windows.foundation.Uri
import windows.system.VirtualKey
import windows.system.VirtualKeyModifiers

@GalleryPage(route = "AppBarSeparator", title = "AppBarSeparator", group = "MenusAndToolbars", order = 1)
internal fun appBarSeparatorPage() = ExamplePage {
    example("AppBarButtons separated by AppBarSeparators.", appBarSeparatorAppBarButtonsSeparatedByAppBarSeparatorsSample())
}

@GallerySample(route = "AppBarSeparator", title = "AppBarButtons separated by AppBarSeparators.")
internal fun appBarSeparatorAppBarButtonsSeparatedByAppBarSeparatorsSample() = CommandBar().apply {
        primaryCommands.add(appCommand("Attach Camera", Symbol.AttachCamera)); primaryCommands.add(AppBarSeparator())
        primaryCommands.add(appCommand("Like", Symbol.Like)); primaryCommands.add(appCommand("Dislike", Symbol.Dislike))
        primaryCommands.add(AppBarSeparator()); primaryCommands.add(appCommand("Orientation", Symbol.Orientation))
    }
