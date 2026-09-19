package io.github.composefluent.winrt.gallery.shell

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*
import microsoft.windows.badgenotifications.BadgeNotificationGlyph
import microsoft.windows.badgenotifications.BadgeNotificationManager

@GalleryPage(route = "BadgeNotificationManager", title = "Badge notifications", group = "Shell", order = 1)
internal fun badgeNotificationPage() = ExamplePage {
    val supported = GalleryPreferences.packaged
    children.add(packageRequirement("BadgeNotificationManager", supported))
    var badgeSet = false
    val count = NumberBox().apply { header = "Badge count"; value = 5.0; minimum = 1.0; smallChange = 1.0; largeChange = 10.0; width = 160.0; spinButtonPlacementMode = NumberBoxSpinButtonPlacementMode.Inline }
    fun setCount() { if (supported && !count.value.isNaN()) { badgeNotificationCountSample(count.value.toUInt()); badgeSet = true } }
    fun clear() { if (supported) { checkNotNull(BadgeNotificationManager.current).clearBadge(); badgeSet = false } }
    count.valueChanged.add { _, _ -> if (badgeSet) setCount() }

    example("Setting badge notifications as a count.",
        stack(8.0) {
            children.add(Button("Set badge as count", ::setCount).apply { width = 160.0; isEnabled = supported })
            children.add(Button("Clear badge", ::clear).apply { width = 160.0; isEnabled = supported })
        }, count)
    val glyphs = listOf(BadgeNotificationGlyph.None, BadgeNotificationGlyph.Activity, BadgeNotificationGlyph.Alarm, BadgeNotificationGlyph.Alert, BadgeNotificationGlyph.Attention, BadgeNotificationGlyph.Available, BadgeNotificationGlyph.Away, BadgeNotificationGlyph.Busy, BadgeNotificationGlyph.Error, BadgeNotificationGlyph.NewMessage, BadgeNotificationGlyph.Paused, BadgeNotificationGlyph.Playing, BadgeNotificationGlyph.Unavailable)
    var selected = 1
    fun setGlyph() { if (supported) { badgeNotificationGlyphSample(glyphs[selected]); badgeSet = true } }

    example("Setting badge notifications as a glyph.",
        stack(8.0) {
            children.add(Button("Set badge glyph", ::setGlyph).apply { width = 160.0; isEnabled = supported })
            children.add(Button("Clear badge", ::clear).apply { width = 160.0; isEnabled = supported })
        },
        select("BadgeNotificationGlyph", listOf("None", "Activity", "Alarm", "Alert", "Attention", "Available", "Away", "Busy", "Error", "NewMessage", "Paused", "Playing", "Unavailable"), 1) {
        selected = it; if (badgeSet) setGlyph()
    }.apply { width = 160.0 })


}

@GallerySample(route = "BadgeNotificationManager", title = "Setting badge notifications as a count.")
internal fun badgeNotificationCountSample(count: UInt) =
    checkNotNull(BadgeNotificationManager.current).setBadgeAsCount(count)

@GallerySample(route = "BadgeNotificationManager", title = "Setting badge notifications as a glyph.")
internal fun badgeNotificationGlyphSample(glyph: BadgeNotificationGlyph) =
    checkNotNull(BadgeNotificationManager.current).setBadgeAsGlyph(glyph)
