/*
contains the filter and sort options an entrant can apply when browsing campsites
 */

package com.example.stars

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * Filters an entrant can apply to the campsite list. Null means that the filter is off
 *
 * @param location the location of the campsite
 * @param radius max distance from [location] in kilometers
 * @param dateRange the date range for the filter
 * @param guests the number of guests for the filter
 */

data class CampsiteFilter(
    val location: LocationCoordinates? = null,
    val radius: Double = 50.0,
    val dateRange: EpochRange? = null,
    val guests: Int? = null
) {
    // true if any filters are active
    val isActive: Boolean
        get() {
            return location != null || dateRange != null || guests != null
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

//used Google Search's automatic ai summary for this.
// "how to find distance between two points on globe", "how do i implement haversine formula in kotlin" 24-11-2026
// uses Haversine formula
fun distanceKm(a: LocationCoordinates, b: LocationCoordinates): Double {
    val earthRadius = 6371.0
    val latDiff = Math.toRadians(b.latitude - a.latitude)
    val lonDiff = Math.toRadians(b.longitude - a.longitude)
    val h =
        sin(latDiff / 2).pow(2) + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(
            lonDiff / 2
        ).pow(2)
    return 2 * earthRadius * asin(sqrt(h))
}

// date range
fun isAvailableFor(campsite: Campsite, range: EpochRange): Boolean {
    return campsite.availabilitySchedule.any {
        it.start <= range.start && range.end <= it.end
    }
}

// returns only the campsites that pass every active filter
fun applyFilter(campsite: List<Campsite>, filter: CampsiteFilter): List<Campsite> {
    return campsite.filter { campsite ->
        val locationOk = filter.location == null ||
                distanceKm(filter.location, campsite.location) <= filter.radius
        val dateOk = filter.dateRange == null || isAvailableFor(campsite, filter.dateRange)
        val guestsOk = filter.guests == null || campsite.capacity >= filter.guests
        locationOk && dateOk && guestsOk
    }
}

// sorts campsites
fun applySort(campsites: List<Campsite>, sort: CampsiteFilter.CampsiteSort): List<Campsite> {
    val field = sort.field ?: return campsites

    val toSort: List<Campsite>
    val unrated: List<Campsite>
    if (field == CampsiteFilter.SortField.RATING) {
        toSort = campsites.filter { it.ratingCount > 0 }
        unrated = campsites.filter { it.ratingCount == 0 }
    } else {
        toSort = campsites
        unrated = emptyList()
    }

    val sorted = when (field) {
        //sort by alphabetical
        CampsiteFilter.SortField.NAME ->
            if (sort.ascending) toSort.sortedBy { it.title.lowercase() }
            else toSort.sortedByDescending { it.title.lowercase() }
        //sort by price
        CampsiteFilter.SortField.PRICE ->
            if (sort.ascending) toSort.sortedBy { it.price }
            else toSort.sortedByDescending { it.price }
        //sort by rating
        CampsiteFilter.SortField.RATING ->
            if (sort.ascending) toSort.sortedBy { it.averageRating }
            else toSort.sortedByDescending { it.averageRating }
    }
    return sorted + unrated
}