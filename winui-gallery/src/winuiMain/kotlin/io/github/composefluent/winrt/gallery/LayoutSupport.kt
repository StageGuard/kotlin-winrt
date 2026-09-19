// Ported from WinUI Gallery Samples/Layout controls (MIT).
package io.github.composefluent.winrt.gallery

import microsoft.ui.xaml.shapes.Rectangle

internal val layoutColors = listOf(0xFF0000u, 0x0000FFu, 0x008000u, 0xFFFF00u)
internal fun tile(color: UInt, size: Double = 40.0) = Rectangle().apply { width = size; height = size; fill = brush(color) }
