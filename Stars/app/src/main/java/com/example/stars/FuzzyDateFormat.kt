/**
 * Custom date formatter
 */
package com.example.stars

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Formats a timestamp depending on how much time has elapsed
 */
class FuzzyDateFormat(locale: Locale) {
    private val yearPattern = SimpleDateFormat("MMM dd, yyyy", locale)

    /**
     * Inflects a string for plurality by concatenating with the standard plurality affix
     *
     * (Adds an 's' to the end of [label] if [value] is not 1)
     */
    private fun labelUnit(value: Long, label: String): String {
        if (value != 1L) {
            label + "s"
        }
        return "$value $label ago"
    }

    /**
     * Returns the fuzzy date of [then] compared to the time now
     *
     * @param then time in epoch millis
     * @return formatted date
     */
    fun format(then: Long): String {
        val now = System.currentTimeMillis()
        val elapsedMs = abs(now - then)

        val elapsedSec = TimeUnit.SECONDS.convert(elapsedMs, TimeUnit.MILLISECONDS)
        if (elapsedSec < 60) {
            return labelUnit(elapsedSec, "second")
        }

        val elapsedMin = TimeUnit.MINUTES.convert(elapsedMs, TimeUnit.MILLISECONDS)
        if (elapsedMin < 60) {
            return labelUnit(elapsedMin, "minute")
        }

        val elapsedHr = TimeUnit.HOURS.convert(elapsedMs, TimeUnit.MILLISECONDS)
        if (elapsedHr < 24) {
            return labelUnit(elapsedHr, "hour")
        }

        val elapsedDays = TimeUnit.DAYS.convert(elapsedMs, TimeUnit.MILLISECONDS)
        if (elapsedDays < 7) {
            return labelUnit(elapsedDays, "day")
        }

        // if a week or longer ago, just do a regular timestamp
        return yearPattern.format(Date(then))
    }
}