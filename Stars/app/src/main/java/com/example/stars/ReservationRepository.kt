/**
 * Class for reservation related database access
 */
package com.example.stars

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

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

    /**
     * Checks whether [userId] has finished a stay at the campsite with ID [campsiteId]
     * meaning they have accepted the reservation there whose end time has already passed
     *
     * @return the most recent completed reservation, or null if the user hasn't stayed there
     */
    suspend fun getLatestCompletedStay(userId: String, campsiteId: String): Reservation? {
        val now = System.currentTimeMillis()

        return collection
            .whereEqualTo("entrantId", userId)
            .whereEqualTo("campsiteId", campsiteId)
            .get().await()
            .toObjects(dataClass)
            .filter { reservation ->
                reservation.status == ReservationStatus.ACCEPTED && reservation.timeRange.end < now
            }
            .maxByOrNull { it.timeRange.end }
    }
}