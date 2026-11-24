/*
contains the filter and sort options an entrant can apply when browsing campsites
 */

package com.example.stars


/**
 * Filters an entrant can apply to the campsite list. Null means that the filter is off
 *
 * @param location the location of the campsite
 * @param radius max distance from [location] in kilometers
 * @param dateRange the date range of the campsite
 * @param guests the number of guests
 */

data class CampsiteFilter (
    val location: LocationCoordinates? = null,
    val radius: Double = 50.0,
    val dateRange: EpochRange? = null,
    val guests: Int? = null
) {
    // true if any filters are active
    val isActive: Boolean
        get() {
            return location != null || radius > 0.0 || dateRange != null || guests != null
        }

    // fields the campsite list can be sorted by
    enum class SortField(val label: String) {
        NAME("Name"), PRICE("Price"), RATING("Rating")
    }

    /// how to sort the campsite list (true for ascending, false for descending
    data class CampsiteSort(
        val field: SortField? = null,
        val ascending: Boolean = true
    )


}