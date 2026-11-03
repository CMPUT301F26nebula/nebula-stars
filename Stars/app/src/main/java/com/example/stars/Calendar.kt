package com.example.stars

import android.icu.util.Calendar
import android.icu.util.GregorianCalendar

// TODO i have no idea if any of this works yet lol, ill test it tomorrow

/**
 * Represents a single day in the calendar as well as any reservations that are on that day
 *
 * @param date Calendar representing the date of the day
 * @param events list of reservations this day is in the range of
 * @param isCurrent true if it is the current day
 * @param isFiller true if the day is not part of the month being displayed
 */
class CalendarDay(
    val date: GregorianCalendar,
    val events: List<Reservation>,
    val isCurrent: Boolean = false,
    val isFiller: Boolean = false
)

/**
 * Reservation dates range stored as Calendar objects instead of milliseconds
 *
 * @param reservation a Reservation object supplying an EpochRange
 */
class CalendarReservation(val reservation: Reservation) {
    val start: GregorianCalendar = GregorianCalendar()
    val end: GregorianCalendar = GregorianCalendar()
    init {
        // set date from epoch millis
        start.timeInMillis = reservation.timeRange.start
        end.timeInMillis = reservation.timeRange.end
    }

    /**
     * Returns true if the given day is in the reservation range
     */
    fun isDayInRange(day: GregorianCalendar): Boolean {
        return day in start..end
    }
}

/**
 * Manages the current month data for a calendar widget
 *
 * @param reservations list of user reservations
 */
class CalendarManager(val reservations: List<Reservation>) {
    constructor(): this(listOf())
    // this should probably query the reservation list by month instead of all at once
    private val reservedDates: List<CalendarReservation> = buildList {
        for (reservation in reservations) {
            add(CalendarReservation(reservation))
        }
    }
    private val current: Calendar = Calendar.getInstance()

    /**
     * Changes the current display month to the next month, wrapping the year if needed
     */
    fun nextMonth() {
        current.roll(Calendar.MONTH, 1)
    }

    /**
     * Changes the current display month to the previous month, wrapping the year if needed
     */
    fun lastMonth() {
        current.roll(Calendar.MONTH, -1)
    }

    /**
     * Returns a list of 35 days used to display a month
     *
     * If the start of the month isnt the first day of the week (Sunday) then a few days from the
     * previous month will be added to keep the "shape" of the calendar rectangular. Similarly, some
     * days of the next month will be added after the current month to fill out the end.
     *
     * TODO these params might not stay since the display date should be controlled by nextMonth()
     * TODO and lastMonth()
     */
    fun getMonthDisplayDays(
        month: Int = current.get(Calendar.MONTH),
        year: Int = current.get(Calendar.YEAR)
    ): List<CalendarDay> {
        // find start of the current month
        val displayDay = GregorianCalendar(year, month, 1)
        // if the first day of the month isnt the first sunday on the calendar then we show some
        // days from the last month to make it all rectangular
        val paddingDays = displayDay.get(Calendar.DAY_OF_WEEK) - 1
        if (paddingDays > 0) {
            displayDay.roll(Calendar.DATE, -paddingDays)
        }
        return buildList {
            for (i in 1..35) {
                add(
                    CalendarDay(
                        // dont want this to change when we rollover for the next day so copy it
                        date = displayDay.clone() as GregorianCalendar,
                        events = buildList {
                            for (r in reservedDates) {
                                if (r.isDayInRange(displayDay)) {
                                    add(r.reservation)
                                }
                            }
                        },
                        isCurrent = displayDay.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR),
                        isFiller = displayDay.get(Calendar.MONTH) != current.get(Calendar.MONTH)
                    )
                )
                // go to the next day
                displayDay.roll(Calendar.DATE, 1)
            }
        }
    }
}