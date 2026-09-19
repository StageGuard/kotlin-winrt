package io.github.composefluent.winrt.gallery.dateandtime

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.controls.*
import windows.globalization.Calendar
import windows.globalization.CalendarIdentifiers
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@GalleryPage(route = "TimePicker", title = "TimePicker", group = "DateAndTime", order = 3)
internal fun timePickerPage() = ExamplePage {
    example("A simple TimePicker.", timePickerSimpleTimePickerSample())
    example("A TimePicker with a header and minute increments.", timePickerTimePickerWithAHeaderAndMinuteIncrementsSample1())
    example("TimePickers with different clock identifiers.", timePickerTimePickersWithDifferentClockIdentifiersSample2())
}

@GallerySample(route = "TimePicker", title = "A simple TimePicker.")
internal fun timePickerSimpleTimePickerSample() = TimePicker()

@GallerySample(route = "TimePicker", title = "A TimePicker with a header and minute increments.")
internal fun timePickerTimePickerWithAHeaderAndMinuteIncrementsSample1() = TimePicker().apply {
        header = "Arrival time"; minuteIncrement = 15
    }

@GallerySample(route = "TimePicker", title = "TimePickers with different clock identifiers.")
internal fun timePickerTimePickersWithDifferentClockIdentifiersSample2() = StackPanel().apply { this.spacing = 8.0; val now = Calendar().apply { changeClock("24HourClock"); setToNow() }
        val time = now.hour.hours + now.minute.minutes + now.second.seconds
        children.add(TimePicker().apply {
            clockIdentifier = "12HourClock"; header = "12-hour clock"
            loaded.add { _, _ -> selectedTime = time }
        })
        children.add(TimePicker().apply {
            clockIdentifier = "24HourClock"; header = "24-hour clock"
            loaded.add { _, _ -> selectedTime = time }
        }) }
