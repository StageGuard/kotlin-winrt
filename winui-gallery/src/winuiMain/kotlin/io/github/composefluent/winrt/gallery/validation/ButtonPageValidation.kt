package io.github.composefluent.winrt.gallery.validation

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.gallery.basicinput.ButtonPage
import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.TextWrapping
import microsoft.ui.xaml.automation.peers.ButtonAutomationPeer
import microsoft.ui.xaml.automation.peers.ToggleButtonAutomationPeer
import microsoft.ui.xaml.controls.StackPanel
import microsoft.ui.xaml.controls.TextBlock

internal fun validateButtonPage(page: ButtonPage) = with(page) {
    GalleryXamlValidation.onLoaded("Button", this, trigger = {
        ButtonAutomationPeer(standardButton).invoke()
        ButtonAutomationPeer(imageButton).invoke()
        ToggleButtonAutomationPeer(disableButton).toggle()
        textSource.isExpanded = true
    }, verify = {
        check(textOutput.text == "You clicked: Standard XAML button")
        check(imageOutput.text == "You clicked: Image button")
        check(disableButton.isChecked == true && !standardButton.isEnabled)
        check(checkNotNull(content).asWinRT<StackPanel>().children.size == 4)
        check(accentButton.style != null && subtleButton.style != null)
        check(wrappedFirst.maxWidth == 240.0 && wrappedSecond.maxWidth == 240.0)
        check(checkNotNull(wrappedFirst.content).asWinRT<TextBlock>().textWrapping == TextWrapping.Wrap)
        check(textSource.content != null)
        check(checkNotNull(GalleryCodeCatalog.xamlDocument("Button")).source.contains("Click=\"onStandardClick\""))
        check(checkNotNull(GalleryCodeCatalog.document("Button", "", 0)).source.contains("private fun onStandardClick"))
    })
}
