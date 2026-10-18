package com.example.stars

/**
 * Possible statuses of a reservation
 */
enum class ReservationStatus {
    INVITED, ACCEPTED, DECLINED, CANCELLED_BY_ENTRANT, CANCELLED_BY_OWNER
}

/**
 * A reservation held for a campsite
 *
 * @param id the unique id of the reservation
 * @param timeRange the timestamps between which the reservation is active
 * @param status the current status of the reservation
 * @param entrantId the unique ID of the user who made the reservation
 */
data class Reservation (
    val id: String,
    val timeRange: EpochRange,
    val status: ReservationStatus,
    val entrantId: String
)