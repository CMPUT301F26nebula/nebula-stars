/*
Contains repository helper class to get and store Campsite objects from firestore
TODO: Fill out getFilteredCampsites signature
 */
package com.example.stars

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * A container of campsites stored in Firestore
 */
class CampsiteRepository {
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
    suspend fun addOrUpdateCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.id).set(campsite).await()
    }

    /**
     * Deletes the campsite with ID [campsiteId] from the list of campsites
     */
    suspend fun deleteCampsite(campsiteId: String) {
        campsitesRef.document(campsiteId).delete().await()
    }
}