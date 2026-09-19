package io.github.composefluent.winrt.gallery.dateandtime

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*
import windows.globalization.Calendar
import windows.globalization.CalendarIdentifiers
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@GalleryPage(route = "CalendarDatePicker", title = "CalendarDatePicker", group = "DateAndTime", order = 0)
internal fun calendarDatePickerPage() = ExamplePage {
    example("A CalendarDatePicker with a header and placeholder text.", calendarDatePickerCalendarDatePickerWithAHeaderAndPlaceholderTextSample())
}

@GallerySample(route = "CalendarDatePicker", title = "A CalendarDatePicker with a header and placeholder text.")
internal fun calendarDatePickerCalendarDatePickerWithAHeaderAndPlaceholderTextSample() = CalendarDatePicker().apply {
        header = "Calendar"; placeholderText = "Pick a date"
    }
