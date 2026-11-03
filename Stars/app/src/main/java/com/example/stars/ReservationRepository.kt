package com.example.stars

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ReservationRepository {
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
}