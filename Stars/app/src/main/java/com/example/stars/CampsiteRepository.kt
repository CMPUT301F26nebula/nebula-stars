/*
Contains repository helper class to get and store Campsite objects from firestore
TODO: Fill out getFilteredCampsites signature
 */
package com.example.stars

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

/**
 * A container of campsites stored in Firestore
 *
 * @param scope the global coroutine scope used to run basic database operations
 */
class CampsiteRepository(private val scope: CoroutineScope):
    AbstractUserDataRepository<Campsite>("campsites", scope, Campsite::class.java){

    /**
     * Gets a list of campsites matching the passed filters
     * TODO: Add filters as parameters
     *
     * @return list of campsites matching the filters, which can be empty if none work
     */
    suspend fun getFilteredCampsites(): List<Campsite> {
        return getAll()
    }

    /**
     * Gets a list of all campsites owned by [userId]
     *
     * @return list of all campsites owned by the user, which will be empty if not applicable
     */
    override suspend fun getWithUserId(userId: String): List<Campsite> {
        return getWhereFieldEquals("ownerId", userId)
    }

    /*
    /**
     * Gets the Campsite from Firestore associated with the given ID
     *
     * @return the associated campsite, or null if it doesn't exist
     */
    suspend fun getFromId(campsiteId: String): Campsite? {
        return getFromId(campsiteId)
    }

    /**
     * Adds or updates a [campsite] in the campsite list
     * If campsite Id matches an existing campsite, updates it, otherwise creates a new entry
     */
    fun addOrUpdate(campsite: Campsite) {
        addOrUpdateFromId(campsite.id, campsite)
    }

    /**
     * Deletes the campsite with ID [campsiteId] from the list of campsites
     */
    fun removeFromId(campsiteId: String) {
        removeFromId(campsiteId)
    }
     */
}