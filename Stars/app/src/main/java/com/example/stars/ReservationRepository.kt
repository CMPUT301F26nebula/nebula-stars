package com.example.stars

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Database accessor for reservations
 *
 * @param externalScope CoroutineScope for running async functions that dont have a return value
 */
class ReservationRepository(private val externalScope: CoroutineScope) {
    private val db = Firebase.firestore
    private val reservationsRef = db.collection("reservations")

    private val updates = Signaller<String>()

    /**
     * Suspends until this ReservationRepository updates the database
     *
     * @return unique Id of the reservation that was added/modified/deleted
     */
    suspend fun waitForUpdate(): String {
        return updates.wait()
    }

    /**
     * Notifies that the database has been updated
     *
     * @param reservationId unique ID of the reservation that was added/modified/deleted
     */
    private suspend fun sendUpdate(reservationId: String) {
        updates.broadcast(reservationId)
    }

    /**
     * Gets a list of all reservations made by [userId], including past and future reservations
     *
     * @return list of all reservations made by the user, which will be empty if they have none
     */
    suspend fun getUserReservations(userId: String): List<Reservation> {
        return reservationsRef
            .whereEqualTo("entrantId", userId)
            .get().await()
            .toObjects(Reservation::class.java)
    }

    /**
     * Checks wheter [userId] has finished a stay at the campsite with ID [campsiteId]
     * meaning they have accepted the reservation there whose end time has already passed
     *
     * @return true if the user has completed a stay and is allowed to rate the campsite
     */
    suspend fun hasCompletedStay(userId: String, campsiteId: String): Boolean {
        val now = System.currentTimeMillis()

        return reservationsRef
            .whereEqualTo("entrantId", userId)
            .whereEqualTo("campsiteId", campsiteId)
            .get().await()
            .toObjects(Reservation::class.java)
            .any { reservation ->
                reservation.status == ReservationStatus.ACCEPTED && reservation.timeRange.end < now
            }

    }

    /**
     * Deletes the user reservation with unique id [reservationId]
     */
    fun removeUserReservation(reservationId: String) {
        externalScope.launch {
            reservationsRef.document(reservationId).delete()
            sendUpdate(reservationId)
        }
        Log.d("ReservationRepository", "Deleted $reservationId")
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
        externalScope.launch {
            reservationsRef
                .document(id)
                .set(
                    Reservation(
                        id = id,
                        timeRange = epochRange,
                        status = status,
                        entrantId = entrantId,
                        campsiteId = campsiteId
                    )
                )
                .await()
            sendUpdate(id)
        }
        Log.d("ReservationRepository", "Added $id")
    }
}