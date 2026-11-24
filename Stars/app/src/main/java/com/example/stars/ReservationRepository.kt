/**
 * Class for reservation related database access
 */
package com.example.stars

import kotlinx.coroutines.CoroutineScope

/**
 * Database accessor for reservations
 *
 * @param scope CoroutineScope for running async functions that dont have a return value
 */
class ReservationRepository(private val scope: CoroutineScope):
    AbstractUserDataRepository<Reservation>("reservations", scope, Reservation::class.java) {

    /**
     * Gets a list of all reservations made by [userId], including past and future reservations
     *
     * @return list of all reservations made by the user, which will be empty if they have none
     */
    override suspend fun getWithUserId(userId: String): List<Reservation> {
        return getWhereFieldEquals("entrantId", userId)
    }

    /*
    /**
     * Deletes the user reservation with unique id [reservationId]
     */
    fun removeFromId(reservationId: String) {
        removeFromId(reservationId)
    }

    /**
     * Generates a (hopefully) unique ID for a new reservation and adds it to the database
     *
     * @param entrantId unique ID of the entrant making this reservation
     * @param campsiteId unique ID of the campsite being reserved
     * @epochRange start and end of the reservation as an EpochRange
     * @status current status of the reservation
     */
    fun addUserReservation(
        entrantId: String,
        campsiteId: String,
        epochRange: EpochRange,
        status: ReservationStatus = ReservationStatus.INVITED
    ) {
        val id = Reservation.uniqueIdFrom(entrantId, campsiteId, epochRange)
        addOrUpdateFromId(id, Reservation(
            id = id,
            timeRange = epochRange,
            status = status,
            entrantId = entrantId,
            campsiteId = campsiteId
        ))
        Log.d("ReservationRepository", "Added $id")
    }
     */
}