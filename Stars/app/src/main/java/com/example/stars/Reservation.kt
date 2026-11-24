/**
 * Data class representing a campsite reservation in the database
 */
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
 * @param campsiteId the unique ID of the campsite the reservation is for
 */
data class Reservation (
    override val id: String = "",
    val timeRange: EpochRange = EpochRange(),
    val status: ReservationStatus = ReservationStatus.INVITED,
    val entrantId: String = "",
    val campsiteId: String = ""
): UniqueIdentifier {
    companion object UniqueId {
        /**
         * Generate a unique document id
         *
         * @param entrantId id of reservation holder
         * @param campsiteId id of reserved campsite
         * @param timeRange start and end of reservation in epoch millis
         */
        fun uniqueIdFrom(entrantId: String, campsiteId: String, timeRange: EpochRange): String {
            return "${entrantId}_${campsiteId}_${timeRange.start}_${timeRange.end}"
        }
    }
}