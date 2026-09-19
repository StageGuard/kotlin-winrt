package io.github.composefluent.winrt.gallery.statusandinfo

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.PlacementMode
import windows.foundation.Rect
import windows.foundation.Uri

@GalleryPage(route = "InfoBar", title = "InfoBar", group = "StatusAndInfo", order = 1)
internal fun infoBarPage() = ExamplePage {
    fun openOption(bar: InfoBar) = option("Is Open", true) { bar.isOpen = it }.also { toggle ->
        bar.closed.add { _, _ -> toggle.isChecked = false }
    }
    val severity = infoBarClosableInfoBarWithSeverityOptionsSample()
    example("A closable InfoBar with severity options.", severity, stack {
        children.add(openOption(severity))
        children.add(select("Severity", listOf("Informational", "Success", "Warning", "Error")) {
            severity.severity = listOf(InfoBarSeverity.Informational, InfoBarSeverity.Success, InfoBarSeverity.Warning, InfoBarSeverity.Error)[it]
        })
    })
    val actions = infoBarLongMessageSample()
    example("A closable InfoBar with long and short messages.", actions, stack {
        children.add(openOption(actions))
        children.add(select("Message Length", listOf("Short", "Long"), 1) { actions.message = if (it == 0) "A short essential app message." else longInfoBarMessage })
        children.add(select("Action Button", listOf("None", "Button", "Hyperlink")) {
            actions.actionButton = when (it) {
                1 -> Button().apply { content = "Action" }
                2 -> HyperlinkButton().apply { content = "Informational link"; navigateUri = Uri("https://www.microsoft.com/") }
                else -> null
            }
        })
    })
    val display = infoBarInfoBarWithDisplayOptionsSample2()
    example("An InfoBar with display options.", display, stack {
        children.add(openOption(display))
        children.add(option("Is Icon Visible", true) { display.isIconVisible = it })
        children.add(option("Is Closable", true) { display.isClosable = it })
    })

}

@GallerySample(route = "InfoBar", title = "A closable InfoBar with long and short messages.")
internal fun infoBarLongMessageSample() = InfoBar().apply {
    title = "Title"
    message = longInfoBarMessage
    isClosable = true
    isOpen = true
}

private const val longInfoBarMessage = "A long essential app message for your users to be informed of, acknowledge, or take action on. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin dapibus dolor vitae justo rutrum, ut lobortis nibh mattis. Aenean id elit commodo, semper felis nec."

@GallerySample(route = "InfoBar", title = "A closable InfoBar with severity options.")
internal fun infoBarClosableInfoBarWithSeverityOptionsSample() = InfoBar().apply {
    title = "Title"
    message = "Essential app message for your users to be informed of, acknowledge, or take action on."
    isClosable = true
    isOpen = true
}

@GallerySample(route = "InfoBar", title = "An InfoBar with display options.")
internal fun infoBarInfoBarWithDisplayOptionsSample2() = InfoBar().apply {
    title = "Title"
    message = "Essential app message for your users to be informed of, acknowledge, or take action on."
    isClosable = true
    isOpen = true
}
