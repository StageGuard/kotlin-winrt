package io.github.composefluent.winrt.gallery

import io.github.composefluent.winrt.runtime.await
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import windows.applicationmodel.Package
import windows.applicationmodel.datatransfer.Clipboard
import windows.applicationmodel.datatransfer.DataPackage
import windows.foundation.Uri
import winui3package.ContentAlignment
import winui3package.SettingsCard
import winui3package.SettingsExpander

internal fun gallerySettingsPage(
    root: FrameworkElement,
    theme: ElementTheme,
    setTheme: (ElementTheme) -> Unit,
    setTopNavigation: (Boolean) -> Unit,
    hasRecents: Boolean,
    hasFavorites: Boolean,
    clearRecents: () -> Unit,
    clearFavorites: () -> Unit,
): UIElement {
    val page = stack(4.0).apply {
        maxWidth = 1064.0
        horizontalAlignment = HorizontalAlignment.Stretch
        margin = Thickness(0.0, 0.0, 0.0, 36.0)
    }
    val tasks = GalleryPageTasks(page)
    fun section(title: String) = label(title).apply {
        fontWeight = windows.ui.text.FontWeight(600u)
        margin = Thickness(1.0, 30.0, 0.0, 6.0)
    }
    fun link(title: String, uri: String) = HyperlinkButton().apply {
        content = title
        navigateUri = Uri(uri)
        horizontalAlignment = HorizontalAlignment.Left
        padding = Thickness(0.0, 0.0, 0.0, -1.0)
    }
    page.children.add(section("Appearance & behavior"))
    page.children.add(SettingsCard().apply {
        header = "App theme"
        description = "Select which app theme to display"
        headerIcon = glyph("\uE790", 20.0)
        content = select("", listOf("Light", "Dark", "Use system setting"),
            when (theme) { ElementTheme.Light -> 0; ElementTheme.Dark -> 1; else -> 2 },
        ) {
            val selected = listOf(ElementTheme.Light, ElementTheme.Dark, ElementTheme.Default)[it]
            setTheme(selected)
            announce(page, "Theme changed to $selected", "ThemeChangedNotificationActivityId")
        }
    })
    page.children.add(SettingsCard().apply {
        header = "Navigation style"
        headerIcon = glyph("\uF594", 20.0)
        content = select("", listOf("Left", "Top"), if (GalleryPreferences.flag("TopNavigation")) 1 else 0) {
            setTopNavigation(it == 1)
        }.apply { minWidth = 120.0 }
    })
    val spatial = ToggleSwitch().apply {
        isOn = GalleryPreferences.flag("SpatialAudio")
        toggled.add { _, _ ->
            ElementSoundPlayer.spatialAudioMode = if (isOn) ElementSpatialAudioMode.On else ElementSpatialAudioMode.Off
            GalleryPreferences.putFlag("SpatialAudio", isOn)
        }
    }
    val spatialCard = SettingsCard().apply {
        header = "Enable Spatial Audio"
        description = link("Learn more about enabling sounds in your app", "https://learn.microsoft.com/windows/apps/design/input/sound")
        content = spatial
        isEnabled = GalleryPreferences.flag("Sound")
    }
    page.children.add(SettingsExpander().apply {
        header = "Sound"
        description = "Controls provide audible feedback"
        headerIcon = glyph("\uEC4F", 20.0)
        content = ToggleSwitch().apply {
            isOn = GalleryPreferences.flag("Sound")
            toggled.add { _, _ ->
                ElementSoundPlayer.state = if (isOn) ElementSoundPlayerState.On else ElementSoundPlayerState.Off
                GalleryPreferences.putFlag("Sound", isOn)
                spatialCard.isEnabled = isOn
                if (!isOn) spatial.isOn = false
            }
        }
        items.add(spatialCard)
    })
    fun confirm(title: String, description: String, actionText: String, action: () -> Unit) {
        tasks.launch {
            val dialog = ContentDialog().apply {
                xamlRoot = root.xamlRoot
                requestedTheme = root.actualTheme
                this.title = title
                content = description
                primaryButtonText = actionText
                closeButtonText = "Cancel"
                defaultButton = ContentDialogButton.Primary
            }
            try {
                if (dialog.showAsync().await() == ContentDialogResult.Primary) action()
            } finally {
                dialog.hide()
            }
        }
    }
    val clear = Button().apply { content = "Clear recents"; minWidth = 120.0; isEnabled = hasRecents }
    clear.click.add { _, _ ->
        confirm("Clear recently visited samples?", "This will remove all samples from your recent history.", "Clear") {
            clearRecents()
            clear.isEnabled = false
        }
    }
    val unfavorite = Button().apply { content = "Remove favorites"; minWidth = 120.0; isEnabled = hasFavorites }
    unfavorite.click.add { _, _ ->
        confirm("Remove all favorites?", "This will unfavorite all your samples.", "Remove") {
            clearFavorites()
            unfavorite.isEnabled = false
        }
    }
    page.children.add(SettingsCard().apply {
        header = "Manage samples"
        description = "Clear your recent or favorite samples"
        headerIcon = glyph("\uE8A9", 20.0)
        content = stack(8.0, true) {
            children.add(clear)
            children.add(unfavorite)
        }
    })
    page.children.add(section("About"))
    val appVersion = if (GalleryPreferences.packaged) {
        val version = checkNotNull(checkNotNull(Package.current).id).version
        "${version.major}.${version.minor}.${version.build}.${version.revision}"
    } else ""
    page.children.add(SettingsExpander().apply {
        header = "Kotlin WinUI Gallery"
        description = "An independent project by compose-fluent."
        headerIcon = picture("ms-appx:///Assets/AppList.png", 20.0)
        margin = Thickness(0.0, 0.0, 0.0, 36.0)
        content = TextBlock().apply {
            text = if (appVersion.isEmpty()) "Windows App SDK 2.5" else appVersion
            foreground = GalleryTheme.brush("TextFillColorSecondaryBrush")
            isTextSelectionEnabled = true
        }
        items.add(SettingsCard().apply {
            header = "To clone this repository"
            content = label("git clone https://github.com/compose-fluent/kotlin-winrt", 12.0).apply {
                fontFamily = microsoft.ui.xaml.media.FontFamily("Consolas")
                foreground = GalleryTheme.brush("TextFillColorSecondaryBrush")
                isTextSelectionEnabled = true
            }
            actionIcon = glyph("\uE8C8")
            isClickEnabled = true
            click.add { _, _ ->
                Clipboard.setContent(DataPackage().apply {
                    setText("git clone https://github.com/compose-fluent/kotlin-winrt")
                })
                announce(page, "Repository clone command copied", "RepositoryCopied")
            }
        })
        items.add(SettingsCard().apply {
            header = "File a bug or request a new sample"
            actionIcon = glyph("\uE8A7")
            isClickEnabled = true
            click.add { _, _ -> tasks.launch {
                windows.system.Launcher.launchUriAsync(Uri("https://github.com/compose-fluent/kotlin-winrt/issues")).await()
            } }
        })
        items.add(SettingsCard().apply {
            header = "Dependencies & references"
            contentAlignment = ContentAlignment.Vertical
            horizontalContentAlignment = HorizontalAlignment.Left
            content = stack(4.0) {
                children.add(link("Kotlin/WinRT", "https://github.com/compose-fluent/kotlin-winrt"))
                children.add(link("Windows App SDK", "https://aka.ms/windowsappsdk"))
                children.add(link("WinUI 3", "https://aka.ms/winui"))
                children.add(link("WinUI Gallery", "https://github.com/microsoft/WinUI-Gallery"))
                children.add(link("Win2D", "https://github.com/microsoft/Win2D"))
                children.add(link("WinUI Essential 1.8.0", "https://github.com/HO-COOH/WinUIEssentials"))
            }
        })
        items.add(SettingsCard().apply {
            header = "THIS CODE AND INFORMATION IS PROVIDED ‘AS IS’ WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE."
            contentAlignment = ContentAlignment.Vertical
            horizontalContentAlignment = HorizontalAlignment.Left
            content = stack(4.0) {
                children.add(link("Microsoft Services Agreement", "https://go.microsoft.com/fwlink/?LinkId=822631"))
                children.add(link("Microsoft Privacy Statement", "https://go.microsoft.com/fwlink/?LinkId=521839"))
            }
        })
    })
    return Grid().apply {
        rowDefinitions.add(autoRow())
        rowDefinitions.add(starRow())
        children.add(label("Settings", 28.0).apply {
            maxWidth = 1064.0
            horizontalAlignment = HorizontalAlignment.Stretch
            margin = Thickness(36.0, 24.0, 36.0, 0.0)
            microsoft.ui.xaml.automation.AutomationProperties.setHeadingLevel(
                this, microsoft.ui.xaml.automation.peers.AutomationHeadingLevel.Level1,
            )
        })
        val body = scroll(Border().apply {
            horizontalAlignment = HorizontalAlignment.Stretch
            child = page
        }).apply {
            padding = Thickness(36.0, 0.0, 36.0, 0.0)
            horizontalContentAlignment = HorizontalAlignment.Stretch
        }
        Grid.setRow(body, 1)
        children.add(body)
    }
}
