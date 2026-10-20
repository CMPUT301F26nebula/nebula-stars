package com.example.stars

/**
 * An instance of a campsite stored within Firestore
 *
 * @param id the unique identifier of the campsite
 * @param title human-readable name of campsite
 * @param price price to reserve the campsite (per day?)
 * @param capacity number of people that can use the campsite concurrently
 * @param rules campsite rules policy
 * @param ownerId user profile ID of the account that owns this campsite
 * @param imageIds list of promotional images uploaded to this campsite's page
 * @param reservations list of current reservations
 * @param availabilitySchedule list of times the campsite is available
 */
data class Campsite (
    // TODO: add fields geolocation, waiting list, qr codes, reservations, cancellation policy
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val capacity: Int = 0,
    val rules: String = "",
    val ownerId: String = "",
    val imageIds: List<String> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val availabilitySchedule: List<EpochRange> = emptyList()
)