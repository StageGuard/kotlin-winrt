// Ported from WinUI Gallery Samples/AccessibilityKeyboard (MIT).
package io.github.composefluent.winrt.gallery

import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.automation.AutomationProperties
import microsoft.ui.xaml.automation.peers.AutomationHeadingLevel
import microsoft.ui.xaml.input.XYFocusKeyboardNavigationMode
import microsoft.ui.xaml.shapes.Rectangle
import windows.foundation.Uri
import windows.system.VirtualKey
import windows.ui.text.FontWeight

internal fun accessibleHeading(text: String, level: Int = 2) = label(text, if (level == 2) 20.0 else 14.0).apply {
    margin = Thickness(0.0, 20.0, 0.0, 0.0); fontWeight = FontWeight(600u)
    AutomationProperties.setHeadingLevel(this, if (level == 2) AutomationHeadingLevel.Level2 else AutomationHeadingLevel.Level3)
}

internal fun referenceLink(title: String, url: String) = HyperlinkButton().apply { content = title; navigateUri = Uri(url) }
