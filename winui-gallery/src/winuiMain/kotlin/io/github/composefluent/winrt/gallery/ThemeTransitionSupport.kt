// Ported from WinUI Gallery ThemeTransition (MIT).
package io.github.composefluent.winrt.gallery

import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.Popup
import microsoft.ui.xaml.automation.peers.*
import microsoft.ui.xaml.media.animation.*
import microsoft.ui.xaml.shapes.*

internal fun announce(owner: UIElement, message: String, activity: String) {
    FrameworkElementAutomationPeer.fromElement(owner)?.raiseNotificationEvent(
        AutomationNotificationKind.Other, AutomationNotificationProcessing.ImportantMostRecent, message, activity)
}
