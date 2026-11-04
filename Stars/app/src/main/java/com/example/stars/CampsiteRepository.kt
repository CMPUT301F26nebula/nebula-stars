/*
Contains repository helper class to get and store Campsite objects from firestore
TODO: Fill out getFilteredCampsites signature
 */
package com.example.stars

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A container of campsites stored in Firestore
 *
 * @param scope the global coroutine scope used to run basic database operations
 */
class CampsiteRepository(private val scope: CoroutineScope){
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")

    /**
     * Gets a list of campsites matching the passed filters
     * TODO: Add filters as parameters
     *
     * @return list of campsites matching the filters, which can be empty if none work
     */
    suspend fun getFilteredCampsites(): List<Campsite> {
        return campsitesRef.get().await().toObjects(Campsite::class.java)
    }

    /**
     * Gets a list of all campsites owned by [userId]
     *
     * @return list of all campsites owned by the user, which will be empty if not applicable
     */
    suspend fun getUserCampsites(userId: String): List<Campsite> {
        return campsitesRef.whereEqualTo("ownerId", userId).get().await().toObjects(Campsite::class.java)
    }

    /**
     * Gets the Campsite from Firestore associated with the given ID
     *
     * @return the associated campsite, or null if it doesn't exist
     */
    suspend fun getCampsite(campsiteId: String): Campsite? {
        return campsitesRef.document(campsiteId).get().await().toObject(Campsite::class.java)
    }

    /**
     * Adds or updates a [campsite] in the campsite list
     * If campsite Id matches an existing campsite, updates it, otherwise creates a new entry
     */
    fun addOrUpdateCampsite(campsite: Campsite) {
        scope.launch {
            campsitesRef.document(campsite.id).set(campsite).await()
        }
    }

    /**
     * Deletes the campsite with ID [campsiteId] from the list of campsites
     * TODO: Notify all people affected (reservations, entrants and the owner) that the campsite has been removed
     */
    suspend fun deleteCampsite(campsiteId: String) {
        scope.launch {
            campsitesRef.document(campsiteId).delete().await()
        }
    }
}