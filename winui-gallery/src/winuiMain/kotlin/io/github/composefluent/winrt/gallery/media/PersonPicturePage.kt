package io.github.composefluent.winrt.gallery.media

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.media.Stretch
import microsoft.ui.xaml.media.imaging.BitmapImage
import microsoft.ui.xaml.media.imaging.SvgImageSource
import windows.foundation.Uri
import windows.ui.text.FontStyle

@GalleryPage(route = "PersonPicture", title = "PersonPicture", group = "Media", order = 5)
internal fun personPicturePage() = ExamplePage {
    val person = personPictureSelectDifferentLooksForAPersonPictureSample()
    example("Select different looks for a PersonPicture.", person, choices("Profile type", listOf("Profile Image", "Display Name", "Initials")) {
        person.profilePicture = if (it == 0) BitmapImage(Uri("https://learn.microsoft.com/windows/uwp/contacts-and-calendar/images/shoulder-tap-static-payload.png")) else null
        person.displayName = if (it == 1) "Jane Doe" else ""
        person.initials = if (it == 2) "SB" else ""
    })
}

@GallerySample(route = "PersonPicture", title = "Select different looks for a PersonPicture.")
internal fun personPictureSelectDifferentLooksForAPersonPictureSample() = PersonPicture().apply {
        height = 300.0; verticalAlignment = VerticalAlignment.Top
        profilePicture = BitmapImage(Uri("https://learn.microsoft.com/windows/uwp/contacts-and-calendar/images/shoulder-tap-static-payload.png"))
    }
