// Ported from WinUI Gallery Samples/{InfoBadge,InfoBar,ProgressBar,ProgressRing,ToolTip} (MIT).
package io.github.composefluent.winrt.gallery

import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.PlacementMode
import windows.foundation.Rect
import windows.foundation.Uri

internal fun controlStyle(name: String): Style = GalleryTheme.resource(name).asWinRT()
