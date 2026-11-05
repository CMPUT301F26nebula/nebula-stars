package com.example.stars

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReservationRepository(private val externalScope: CoroutineScope) {
    private val db = Firebase.firestore
    private val reservationsRef = db.collection("reservations")

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
     * Deletes the user reservation with unique id [reservationId]
     */
    fun removeUserReservation(reservationId: String) {
        externalScope.launch {
            reservationsRef.document(reservationId).delete()
        }
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
        }
    }
}