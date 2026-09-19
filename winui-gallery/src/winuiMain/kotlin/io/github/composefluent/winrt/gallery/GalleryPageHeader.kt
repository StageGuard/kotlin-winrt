// PageHeader layout and behavior adapted from WinUI Gallery (MIT).
package io.github.composefluent.winrt.gallery

import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.FlyoutPlacementMode
import microsoft.ui.xaml.controls.primitives.ToggleButton
import microsoft.ui.xaml.automation.AutomationProperties
import microsoft.ui.xaml.automation.peers.AutomationHeadingLevel
import microsoft.ui.xaml.media.FontFamily
import windows.foundation.Uri
import windows.applicationmodel.datatransfer.Clipboard
import windows.applicationmodel.datatransfer.DataPackage

internal fun galleryPageHeader(
    page: GalleryPageInfo,
    favorite: Boolean,
    toggleTheme: () -> Unit,
    setFavorite: (Boolean) -> Unit,
) = Grid().apply {
    rowDefinitions.add(autoRow()); rowDefinitions.add(autoRow())
    val heading = stack(4.0, horizontal = true) {
        children.add(label(page.title, 28.0).apply {
            fontWeight = windows.ui.text.FontWeight(600u)
            textWrapping = TextWrapping.NoWrap; textTrimming = TextTrimming.CharacterEllipsis
            AutomationProperties.setAutomationId(this, "PageHeader")
            AutomationProperties.setHeadingLevel(this, AutomationHeadingLevel.Level1)
        })
        if (page.apiNamespace.isNotEmpty() || page.baseClasses.isNotEmpty()) children.add(Button().apply {
            content = glyph("\uE946", 14.0).apply { foreground = GalleryTheme.brush("AccentTextFillColorPrimaryBrush") }
            padding = inset(4.0); margin = Thickness(0.0, 0.0, 0.0, 3.0)
            verticalAlignment = VerticalAlignment.Bottom; style = controlStyle("SubtleButtonStyle")
            named(this, "API details")
            ToolTipService.setToolTip(this, "API namespace and inheritance")
            flyout = Flyout().apply {
                placement = FlyoutPlacementMode.Bottom
                content = stack(16.0) {
                    minWidth = 396.0; maxWidth = 776.0
                    if (page.apiNamespace.isNotEmpty()) children.add(stack(8.0) {
                        children.add(label("Namespace", 12.0).apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") })
                        children.add(label(page.apiNamespace, 12.0).apply { fontFamily = FontFamily("Consolas"); isTextSelectionEnabled = true })
                    })
                    if (page.baseClasses.isNotEmpty()) children.add(stack(8.0) {
                        children.add(label("Inheritance", 12.0).apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush") })
                        children.add(BreadcrumbBar().apply { itemsSource = page.baseClasses; isHitTestVisible = false })
                    })
                }
            }
        })
    }
    children.add(heading)
    val toolbar = Grid().apply {
        Grid.setRow(this, 1); margin = Thickness(0.0, 12.0, 0.0, 12.0)
        rowDefinitions.add(autoRow()); rowDefinitions.add(autoRow())
        columnDefinitions.add(column(1.0, GridUnitType.Star)); columnDefinitions.add(column(1.0, GridUnitType.Auto))
    }
    fun caption(text: String, icon: String) = stack(8.0, horizontal = true) {
        children.add(glyph(icon)); children.add(label(text))
    }
    val links = stack(4.0, horizontal = true) {
        if (page.docs.isNotEmpty()) children.add(DropDownButton().apply {
            content = caption("Documentation", "\uE8A5")
            named(this, "Documentation")
            flyout = Flyout().apply {
                placement = FlyoutPlacementMode.Bottom
                content = stack(0.0) {
                    margin = inset(-12.0)
                    page.docs.forEach { link -> children.add(HyperlinkButton().apply {
                        content = link.title
                        navigateUri = Uri(link.uri)
                        ToolTipService.setToolTip(this, link.uri)
                        horizontalAlignment = HorizontalAlignment.Stretch; horizontalContentAlignment = HorizontalAlignment.Left
                    }) }
                }
            }
        })
        children.add(DropDownButton().apply {
            content = stack(8.0, horizontal = true) {
                children.add(Viewbox().apply { width = 18.0; height = 18.0; child = githubIcon() })
                children.add(label("Source"))
            }
            named(this, "Source code")
            ToolTipService.setToolTip(this, "Source code of this sample page")
            val sourceFlyout = Flyout().apply { placement = FlyoutPlacementMode.Bottom }
            sourceFlyout.content = stack(0.0) {
                margin = Thickness(0.0, -8.0, 0.0, -12.0)
                if (page.sourcePath.isNotBlank()) {
                    children.add(label("Control source code", 12.0).apply {
                        foreground = GalleryTheme.brush("TextFillColorSecondaryBrush")
                    })
                    children.add(HyperlinkButton().apply {
                        content = page.title
                        val sourceUrl = "https://github.com/microsoft/microsoft-ui-xaml/tree/main/controls/dev${page.sourcePath}"
                        navigateUri = Uri(sourceUrl)
                        margin = Thickness(-12.0, 4.0, -12.0, 0.0)
                        horizontalAlignment = HorizontalAlignment.Stretch
                        horizontalContentAlignment = HorizontalAlignment.Left
                        ToolTipService.setToolTip(this, sourceUrl)
                    })
                    children.add(MenuFlyoutSeparator().apply { margin = inset(-12.0) })
                }
                children.add(label("Sample page source code", 12.0).apply {
                    margin = Thickness(0.0, 8.0, 0.0, 0.0)
                    foreground = GalleryTheme.brush("TextFillColorSecondaryBrush")
                })
                children.add(HyperlinkButton().apply {
                    content = "Kotlin"
                    val sourceUrl = "https://github.com/compose-fluent/kotlin-winrt/blob/master/${page.repositorySourcePath}"
                    navigateUri = Uri(sourceUrl)
                    margin = Thickness(-12.0, 4.0, -12.0, 0.0)
                    horizontalAlignment = HorizontalAlignment.Stretch
                    horizontalContentAlignment = HorizontalAlignment.Left
                    named(this, "Kotlin sample page source")
                    ToolTipService.setToolTip(this, sourceUrl)
                })
            }
            flyout = sourceFlyout
        })
    }
    toolbar.children.add(links)
    val actions = stack(4.0, horizontal = true) {
        Grid.setColumn(this, 1); horizontalAlignment = HorizontalAlignment.Right
        children.add(Button().apply {
            height = 32.0
            content = glyph("\uE793")
            ToolTipService.setToolTip(this, "Toggle theme")
            named(this, "Toggle theme")
            click.add { _, _ -> toggleTheme(); announce(this, "Theme changed.", "SampleThemeChanged") }
        })
        children.add(AppBarSeparator())
        children.add(Button().apply {
            height = 32.0
            content = glyph("\uE71B")
            ToolTipService.setToolTip(this, "Copy link")
            named(this, "Copy link")
            val copyButton = this
            val copyTip = TeachingTip().apply {
                target = copyButton
                title = "Quickly reference this sample!"
                subtitle = "Share with others or paste this link into the Run dialog to open the app to this page directly."
                preferredPlacement = TeachingTipPlacementMode.Bottom
                actionButtonContent = "Don't show again"; closeButtonContent = "Got it!"
                heroContent = copyLinkIllustration("kotlin-winui-gallery://sample/${page.id}")
                actionButtonClick.add { _, _ ->
                    GalleryPreferences.putFlag("HideCopyLinkTeachingTip", true)
                    isOpen = false
                }
            }
            resources["CopyLinkTeachingTip"] = copyTip
            unloaded.add { _, _ -> copyTip.isOpen = false }
            click.add { _, _ ->
                Clipboard.setContent(DataPackage().apply { setText("kotlin-winui-gallery://sample/${page.id}") })
                copyTip.isOpen = !GalleryPreferences.flag("HideCopyLinkTeachingTip")
                announce(this, "Link copied.", "SampleLinkCopied")
            }
        })
        children.add(ToggleButton().apply {
            isChecked = favorite
            height = 32.0; named(this, "Favorite sample")
            fun updateIcon() {
                val selected = isChecked == true
                content = glyph(if (selected) "\uE735" else "\uE734")
                ToolTipService.setToolTip(this, if (selected) "Remove from favorites" else "Add to favorites")
            }
            updateIcon()
            click.add { _, _ ->
                setFavorite(isChecked == true)
                updateIcon()
            }
        })
    }
    toolbar.children.add(actions); children.add(toolbar)
    sizeChanged.add { _, _ ->
        val narrow = actualWidth < 540.0
        Grid.setRow(actions, if (narrow) 1 else 0); Grid.setColumn(actions, if (narrow) 0 else 1)
        Grid.setColumnSpan(actions, if (narrow) 2 else 1)
        actions.margin = Thickness(0.0, if (narrow) 8.0 else 0.0, 0.0, 0.0)
    }
}

// Reconstruct the reference's Run-dialog illustration with the actual app URI.
// This is a noninteractive illustration, not a system dialog or markup loader.
private fun copyLinkIllustration(uri: String) = Viewbox().apply {
    maxWidth = 472.0
    AutomationProperties.setName(this, "Illustration of the sample link in the Run dialog")
    isHitTestVisible = false
    child = Grid().apply {
        width = 472.0; height = 280.0; background = brush(0x8DAFC4u)
        children.add(Border().apply {
            margin = Thickness(36.0, 32.0, 36.0, 40.0)
            cornerRadius = corners(8.0); background = brush(0xFFFFFFu)
            child = stack(0.0) {
                children.add(Grid().apply {
                    height = 32.0; background = brush(0xEDF5FAu)
                    children.add(label("Run", 12.0).apply { margin = Thickness(28.0, 6.0, 0.0, 0.0); foreground = brush(0x000000u) })
                    children.add(glyph("\uE8BB", 10.0).apply { horizontalAlignment = HorizontalAlignment.Right; margin = inset(12.0); foreground = brush(0x000000u) })
                })
                children.add(label("Type the name of a program, folder, document, or Internet resource, and Windows will open it for you.", 12.0).apply {
                    margin = Thickness(64.0, 20.0, 12.0, 16.0); foreground = brush(0x000000u)
                })
                children.add(Grid().apply {
                    margin = Thickness(12.0, 0.0, 12.0, 20.0)
                    columnDefinitions.add(column(52.0, GridUnitType.Pixel)); columnDefinitions.add(column(1.0, GridUnitType.Star))
                    children.add(label("Open:", 12.0).apply { verticalAlignment = VerticalAlignment.Center; foreground = brush(0x000000u) })
                    children.add(Border().apply {
                        Grid.setColumn(this, 1); borderThickness = inset(1.0); borderBrush = brush(0x0078D4u); padding = Thickness(4.0, 3.0, 4.0, 3.0)
                        child = label(uri, 11.0).apply { textWrapping = TextWrapping.NoWrap; textTrimming = TextTrimming.CharacterEllipsis; foreground = brush(0x000000u) }
                    })
                })
                children.add(stack(8.0, horizontal = true) {
                    background = brush(0xF0F0F0u); padding = Thickness(108.0, 18.0, 12.0, 18.0)
                    listOf("OK", "Cancel", "Browse...").forEach { title -> children.add(Border().apply {
                        width = 84.0; height = 24.0; cornerRadius = corners(4.0); borderThickness = inset(1.0)
                        borderBrush = brush(if (title == "OK") 0x0078D4u else 0xCCCCCCu); background = brush(0xFFFFFFu)
                        child = label(title, 12.0).apply { horizontalAlignment = HorizontalAlignment.Center; verticalAlignment = VerticalAlignment.Center; foreground = brush(0x000000u) }
                    }) }
                })
            }
        })
    }
}
