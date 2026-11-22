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
    suspend fun addOrUpdateCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.id).set(campsite).await()

    }

    /**
     * Deletes the campsite with ID [campsiteId] from the list of campsites
     * TODO: Notify all people affected (reservations, entrants and the owner) that the campsite has been removed
     */
    suspend fun deleteCampsite(campsiteId: String) {
        campsitesRef.document(campsiteId).delete().await()
    }

    /**
     * Gets the rating [userId] gave the campsite with ID [campsiteId]
     *
     * @return the user's rating, or null if they haven't rated this campsite
     */
    suspend fun getUserRating(campsiteId: String, userId: String): Rating? {
        return campsitesRef.document(campsiteId)
            .collection("ratings")
            .document(userId)
            .get().await()
            .toObject(Rating::class.java)
    }

    /**
     * Adds or replaces [userId]'s rating of the campsite with ID [campsiteId],
     * and updates the campsite's ratingTotal and ratingCount to match
     *
     * @param stars the rating from 1.0 to 5.0, in increments of 0.5
     * @param comment optional comment from the user
     */
    suspend fun setRating(campsiteId: String, userId: String, stars: Double, comment: String) {
        require (stars in 1.0..5.0) { "stars must be between 1.0 and 5.0, was $stars" }

        val campsiteDoc = campsitesRef.document(campsiteId)
        val ratingDoc = campsiteDoc.collection("ratings").document(userId)

        db.runTransaction { transaction ->
            val campsiteSnapshot = transaction.get(campsiteDoc)
            val oldRatingSnapshot = transaction.get(ratingDoc)
            val oldTotal = campsiteSnapshot.getDouble("ratingTotal") ?: 0.0
            val oldCount = campsiteSnapshot.getLong("ratingCount") ?: 0L
            val oldStars = oldRatingSnapshot.getDouble("stars")
            val newTotal: Double
            val newCount: Long
            if (oldStars == null) {
                // new rating: add their stars and one more rating
                newTotal = oldTotal + stars
                newCount = oldCount + 1
            } else {
                // editing: swap their old stars for new ones, count stays the same
                newTotal = oldTotal - oldStars + stars
                newCount = oldCount
            }

            transaction.set(
                ratingDoc,
                Rating(
                    userId = userId,
                    campsiteId = campsiteId,
                    stars = stars,
                    comment = comment.trim(),
                    timestamp = System.currentTimeMillis()
                )
            )
            transaction.update(
                campsiteDoc,
                mapOf("ratingTotal" to newTotal, "ratingCount" to newCount)
            )
        }.await()
    }
}