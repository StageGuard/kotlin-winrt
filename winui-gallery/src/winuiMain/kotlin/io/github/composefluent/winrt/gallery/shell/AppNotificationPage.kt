package io.github.composefluent.winrt.gallery.shell

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.windows.appnotifications.AppNotificationManager
import microsoft.windows.appnotifications.builder.*
import windows.foundation.Uri
import windows.globalization.Calendar

@GalleryPage(route = "AppNotification", title = "App notifications", group = "Shell", order = 0)
internal fun appNotificationPage() = ExamplePage {
    val tasks = GalleryPageTasks(this)
    children.add(stack(8.0) {
        margin = Thickness(0.0, 16.0, 0.0, 0.0)
        children.add(InfoBar().apply {
            isOpen = true; isClosable = false; severity = InfoBarSeverity.Informational
            content = label("If Do Not Disturb mode is enabled in system settings, these notifications will not appear as toast popups, but they will still be added to the notification center and can be viewed there.").apply { margin = Thickness(0.0, 12.0, 12.0, 12.0) }
        })
        children.add(InfoBar().apply {
            isOpen = true; isClosable = false; severity = InfoBarSeverity.Warning; title = "App notifications should not be noisy"
            message = "App notifications are designed to convey timely and relevant information without disrupting the user experience. Excessive use of sound, prolonged durations, or overly attention-seeking visuals can lead to user fatigue and diminish the overall effectiveness of notifications."
        })
    })
    example("A basic notification.", Button("Show notification") { tasks.launch { appNotificationBasicNotificationSample() } })
    val sounds = listOf(AppNotificationSoundEvent.Default, AppNotificationSoundEvent.IM, AppNotificationSoundEvent.Reminder, AppNotificationSoundEvent.SMS, AppNotificationSoundEvent.Alarm, AppNotificationSoundEvent.Call)
    var sound = 0
    example("An informational notification with a logo and custom audio.",
        Button("Show informational notification with logo and custom audio") { tasks.launch { appNotificationInformationalSample(sounds[sound]) } },
        select("AppNotificationSoundEvent", listOf("Default", "IM", "Reminder", "SMS", "Alarm", "Call")) { sound = it }.apply { width = 180.0 })
    example("A visual notification with a hero image.", Button("Show visual notification with hero image and attribution") { tasks.launch { appNotificationVisualNotificationWithAHeroImageSample2() } })
    example("A notification with AppNotification controls.", Button("Show notification with AppNotification controls") { tasks.launch { appNotificationNotificationWithAppNotificationControlsSample3() } })
    example("A notification with a progress bar.", Button("Show notification with progress bar") { tasks.launch { appNotificationNotificationWithAProgressBarSample4() } })

}

@GallerySample(route = "AppNotification", title = "An informational notification with a logo and custom audio.")
internal fun appNotificationInformationalSample(sound: AppNotificationSoundEvent) = run {
    val notification = AppNotificationBuilder()
        .addText("Control Highlight: PersonPicture")
        .addText("Use the PersonPicture control to display user avatars with initials or images.")
        .setAppLogoOverride(Uri("ms-appx:///Assets/ControlImages/PersonPicture.png"), AppNotificationImageCrop.Circle)
        .setAudioEvent(sound)
        .setTimeStamp(Calendar().getDateTime())
        .buildNotification()
    checkNotNull(AppNotificationManager.default).show(notification)
}

@GallerySample(route = "AppNotification", title = "A basic notification.")
internal fun appNotificationBasicNotificationSample() = run {
    val notification = AppNotificationBuilder()
        .addText("Welcome to Kotlin WinUI Gallery")
        .addText("Explore interactive samples and discover the power of modern Windows UI.")
        .buildNotification()
    checkNotNull(AppNotificationManager.default).show(notification)
}

@GallerySample(route = "AppNotification", title = "A visual notification with a hero image.")
internal fun appNotificationVisualNotificationWithAHeroImageSample2() = run {
    val notification = AppNotificationBuilder()
        .addText("Harbor Scene with Boats")
        .addText("A quiet harbor with boats gently anchored in view.")
        .setHeroImage(Uri("ms-appx:///Assets/SampleMedia/LandscapeImage5.jpg"))
        .setAttributionText("WinUI gallery assets")
        .buildNotification()
    checkNotNull(AppNotificationManager.default).show(notification)
}

@GallerySample(route = "AppNotification", title = "A notification with AppNotification controls.")
internal fun appNotificationNotificationWithAppNotificationControlsSample3() = run {
    val notification = AppNotificationBuilder()
        .addText("Survey")
        .addText("Please select your satisfaction level and leave a comment.")
        .addComboBox(AppNotificationComboBox("satisfaction")
            .addItem("1", "Very Bad").addItem("2", "Bad").addItem("3", "Neutral")
            .addItem("4", "Good").addItem("5", "Excellent").setSelectedItem("3"))
        .addTextBox("comment", "Leave a comment here...", "")
        .addButton(AppNotificationButton("Submit").addArgument("action", "submit_survey"))
        .buildNotification()
    checkNotNull(AppNotificationManager.default).show(notification)
}

@GallerySample(route = "AppNotification", title = "A notification with a progress bar.")
internal fun appNotificationNotificationWithAProgressBarSample4() = run {
    val notification = AppNotificationBuilder()
        .addText("Progress Bar Example")
        .addText("This is a sample notification showing how to use a progress bar.")
        .addProgressBar(AppNotificationProgressBar().apply {
            title = "Demo Progress"
            value = 0.6
            valueStringOverride = "60%"
            status = "In progress..."
        })
        .buildNotification()
    checkNotNull(AppNotificationManager.default).show(notification)
}
