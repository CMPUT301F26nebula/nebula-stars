package com.example.stars

import android.icu.util.Calendar
import android.icu.util.GregorianCalendar

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
) {
    fun dayNumber(): Int {
        return date.get(Calendar.DAY_OF_MONTH)
    }
}

/**
 * Reservation dates range stored as Calendar objects instead of milliseconds
 *
 * @param reservation a Reservation object supplying an EpochRange
 */
class CalendarReservation(val reservation: Reservation) {
    val start: GregorianCalendar = GregorianCalendar.getInstance() as GregorianCalendar
    val end: GregorianCalendar = GregorianCalendar.getInstance() as GregorianCalendar
    init {
        // set date from epoch millis
        start.timeInMillis = reservation.timeRange.start
        end.timeInMillis = reservation.timeRange.end
    }

    /**
     * Returns true if the given day is in the reservation range
     */
    fun isDayInRange(day: GregorianCalendar): Boolean {
        // want this to be comparing by day not by millisecond
        return start.get(Calendar.DAY_OF_YEAR) <= day.get(Calendar.DAY_OF_YEAR) &&
                day.get(Calendar.DAY_OF_YEAR) <= end.get(Calendar.DAY_OF_YEAR) &&
                start.get(Calendar.YEAR) <= day.get(Calendar.YEAR) &&
                day.get(Calendar.YEAR) <= end.get(Calendar.YEAR)
    }
}

// TODO maybe make this return a month manager object or something to force recompose
/**
 * Manages the current month data for a calendar widget
 *
 * @param reservations list of user reservations
 */
class CalendarManager(val reservations: List<Reservation>) {
    constructor(): this(emptyList())
    // this should probably query the reservation list by month instead of all at once
    private var reservedDates: List<CalendarReservation> = buildList {
        for (reservation in reservations) {
            add(CalendarReservation(reservation))
        }
    }

    private val current: Calendar = Calendar.getInstance()

    /**
     * Changes the current display month to the next month, wrapping the year if needed
     */
    fun nextMonth() {
        current.add(Calendar.MONTH, 1)
    }

    /**
     * Changes the current display month to the previous month, wrapping the year if needed
     */
    fun lastMonth() {
        current.add(Calendar.MONTH, -1)
    }

    /**
     * Returns the name of the currently selected month
     */
    fun currentMonthName(): String {
        // if theres a builtin calendar method for this then i cannot find it
        return when (current.get(Calendar.MONTH)) {
            0 -> "January"
            1 -> "February"
            2 -> "March"
            3 -> "April"
            4 -> "May"
            5 -> "June"
            6 -> "July"
            7 -> "August"
            8 -> "September"
            9 -> "October"
            10 -> "November"
            11 -> "December"
            else -> ""
        }
    }

    /**
     * Returns the currently selected year
     */
    fun currentYear(): Int {
        return current.get(Calendar.YEAR)
    }

    /**
     * Updates the list of user reservations with a new list
     *
     * @param reservations new list of user reservations
     */
    fun updateReservations(reservations: List<Reservation>) {
        reservedDates = buildList {
            for (reservation in reservations) {
                add(CalendarReservation(reservation))
            }
        }
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
        val displayDay = GregorianCalendar.getInstance()
        val actualDate = displayDay.get(Calendar.DAY_OF_YEAR)
        val actualYear = displayDay.get(Calendar.YEAR)

        // find start of the current month
        displayDay.set(year, month, 1)

        // if the first day of the month isnt the first sunday on the calendar then we show some
        // days from the last month to make it all rectangular
        val paddingDays = displayDay.get(Calendar.DAY_OF_WEEK) - 1
        if (paddingDays > 0) {
            displayDay.add(Calendar.DATE, -paddingDays)
        }
        return buildList {
            for (i in 1..35) {
                val dateCopy = displayDay.clone() as GregorianCalendar
                add(
                    CalendarDay(
                        // dont want this to change when we roll over for the next day so copy it
                        date = dateCopy,
                        events = buildList {
                            for (r in reservedDates) {
                                if (r.isDayInRange(dateCopy)) {
                                    add(r.reservation)
                                }
                            }
                        },
                        isCurrent =
                            displayDay.get(Calendar.DAY_OF_YEAR) == actualDate &&
                            displayDay.get(Calendar.YEAR) == actualYear,
                        isFiller = displayDay.get(Calendar.MONTH) != current.get(Calendar.MONTH)
                    )
                )
                // go to the next day
                displayDay.add(Calendar.DATE, 1)
            }
        }
    }
}