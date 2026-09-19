package io.github.composefluent.winrt.gallery.dateandtime

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*
import windows.globalization.Calendar
import windows.globalization.CalendarIdentifiers
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@GalleryPage(route = "DatePicker", title = "DatePicker", group = "DateAndTime", order = 2)
internal fun datePickerPage() = ExamplePage {
    example("A simple DatePicker with a header.", datePickerSimpleDatePickerWithAHeaderSample())
    example("A DatePicker with a formatted day and no year.", datePickerDatePickerWithAFormattedDayAndNoYearSample1())
}

@GallerySample(route = "DatePicker", title = "A simple DatePicker with a header.")
internal fun datePickerSimpleDatePickerWithAHeaderSample() = DatePicker().apply { header = "Pick a date" }

@GallerySample(route = "DatePicker", title = "A DatePicker with a formatted day and no year.")
internal fun datePickerDatePickerWithAFormattedDayAndNoYearSample1() = DatePicker().apply {
        dayFormat = "{day.integer} ({dayofweek.abbreviated})"; yearVisible = false
        val calendar = Calendar()
        minYear = calendar.getDateTime()
        calendar.addMonths(2); date = calendar.getDateTime()
        calendar.setToNow(); calendar.addYears(5); maxYear = calendar.getDateTime()
    }
