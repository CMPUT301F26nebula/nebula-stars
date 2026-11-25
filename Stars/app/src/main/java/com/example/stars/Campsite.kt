/*
Contains classes underpinning the representation of a Campsite

TODO: Add fields qr codes and cancellation policy
 */
package com.example.stars
import com.google.firebase.firestore.Exclude
/**
 * An instance of a campsite stored within Firestore
 *
 * @param id the unique identifier of the campsite
 * @param title human-readable name of campsite
 * @param price price to reserve the campsite (per day?)
 * @param capacity number of people that can use the campsite concurrently
 * @param description short blurb describing this campsite
 * @param rules campsite rules policy
 * @param ownerId user profile ID of the account that owns this campsite
 * @param imageIds list of promotional images uploaded to this campsite's page
 * @param reservationIds list of current reservation ids
 * @param availabilitySchedule list of times the campsite is available
 * @param location geographic location of the campsite
 * @param ratingTotal sum of all ratings for this campsite
 * @param ratingCount number of ratings for this campsite
 */
data class Campsite (
    override val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val capacity: Int = 0,
    val description: String = "",
    val rules: String = "",
    val ownerId: String = "",
    val imageIds: List<String> = emptyList(),
    val reservationIds: List<String> = emptyList(),
    val availabilitySchedule: List<EpochRange> = emptyList(),
    val location: LocationCoordinates = LocationCoordinates(0.0, 0.0),
    val ratingTotal: Double = 0.0,
    val ratingCount: Int = 0
): UniqueIdentifier {
    /**
     * Calculates the average rating of this campsite, or 0.0 if no ratings have been made
     */
    @get:Exclude
    val averageRating: Double
        get() = if (ratingCount == 0) 0.0 else ratingTotal / ratingCount
}
