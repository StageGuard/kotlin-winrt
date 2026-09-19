package io.github.composefluent.winrt.gallery.dateandtime

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*
import windows.globalization.Calendar
import windows.globalization.CalendarIdentifiers
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@GalleryPage(route = "CalendarView", title = "CalendarView", group = "DateAndTime", order = 1)
internal fun calendarViewPage() = ExamplePage {
    val calendar = calendarViewBasicCalendarViewSample()
    val identifiers = listOf(CalendarIdentifiers.gregorian, CalendarIdentifiers.hebrew, CalendarIdentifiers.hijri,
        CalendarIdentifiers.japanese, CalendarIdentifiers.julian, CalendarIdentifiers.korean, CalendarIdentifiers.persian,
        CalendarIdentifiers.taiwan, CalendarIdentifiers.thai, CalendarIdentifiers.umAlQura)
    example("A basic CalendarView.", calendar, stack {
        children.add(option("IsGroupLabelVisible", true) { calendar.isGroupLabelVisible = it })
        children.add(option("IsOutOfScopeEnabled", true) { calendar.isOutOfScopeEnabled = it })
        children.add(select("SelectionMode", listOf("None", "Single", "Multiple"), 1) {
            calendar.selectionMode = listOf(CalendarViewSelectionMode.None, CalendarViewSelectionMode.Single, CalendarViewSelectionMode.Multiple)[it]
        })
        children.add(select("CalendarIdentifier", identifiers) { calendar.calendarIdentifier = identifiers[it] }.apply { width = 220.0 })
        children.add(select("Language", galleryLanguages.map { it.first }) { calendar.language = galleryLanguages[it].second }.apply { width = 220.0 })
    })
}

@GallerySample(route = "CalendarView", title = "A basic CalendarView.")
internal fun calendarViewBasicCalendarViewSample() = CalendarView().apply { selectionMode = CalendarViewSelectionMode.Single }
