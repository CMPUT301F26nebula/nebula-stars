/*
Contains repository helper class to get and store Campsite objects from firestore
TODO: Fill out getFilteredCampsites signature
 */
package com.example.stars

import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

/**
 * A container of campsites stored in Firestore
 *
 * @param scope the global coroutine scope used to run basic database operations
 */
class CampsiteRepository(private val scope: CoroutineScope):
    AbstractUserDataRepository<Campsite>("campsites", scope, Campsite::class.java){
    
    suspend fun removeCampsite(id: String) {
        val lotteryRepository = LotteryRepository(scope)
        val imageRepository = ImageRepository(scope)

        for (lottery in lotteryRepository.getWithCampsiteId(id)) {
            lottery.campsiteIDs = lottery.campsiteIDs.filter { it != id }
            lotteryRepository.addOrUpdate(lottery)
            if (lottery.campsiteIDs.isEmpty()) {
                lotteryRepository.removeWithId(lottery.id)
            }
        }

        for (imageId in getWithId(id)?.imageIds ?: emptyList()){
            imageRepository.deleteImage(imageId)
        }

        removeWithId(id)
    }

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

    /**
     * Gets the rating [userId] gave the campsite with ID [campsiteId]
     *
     * @return the user's rating, or null if they haven't rated this campsite
     */
    suspend fun getUserRating(campsiteId: String, userId: String): Rating? {
        return collection.document(campsiteId)
            .collection("ratings")
            .document(userId)
            .get().await()
            .toObject(Rating::class.java)
    }

    /**
     * Gets every rating for the campsite with ID [campsiteId], newest first
     *
     * @return list of ratings, will be empty if none exist
     */
    suspend fun getRatings(campsiteId: String): List<Rating> {
        return collection.document(campsiteId)
            .collection("ratings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Rating::class.java)
    }


    /**
     * Adds or replaces [userId]'s rating of the campsite with ID [campsiteId],
     * and updates the campsite's ratingTotal and ratingCount to match
     *
     * @param stars the rating from 1.0 to 5.0, in increments of 0.5
     * @param comment optional comment from the user
     * @param stayRange the start and end time the user stayed at the campsite
     */
    suspend fun setRating(campsiteId: String, userId: String, stars: Double, comment: String, stayRange: EpochRange) {
        require (stars in 1.0..5.0) { "stars must be between 1.0 and 5.0, was $stars" }

        val campsiteDoc = collection.document(campsiteId)
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
                // if its a new rating, add their stars rating and review count++
                newTotal = oldTotal + stars
                newCount = oldCount + 1
            } else {
                // if its editing an old rating, minus old stars, add new stars, and review count stays the same
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
                    stayRange = stayRange,
                    timestamp = System.currentTimeMillis()
                )
            )
            transaction.update(
                campsiteDoc,
                mapOf("ratingTotal" to newTotal, "ratingCount" to newCount)
            )
        }.await()
    }


    /**
     * deletes [userId]s rating of the campsite with ID [campsiteId] and removes it from the campsite's ratingTotal and ratingCount
     */
    suspend fun deleteRating(campsiteId: String, userId: String) {
        val campsiteDoc = collection.document(campsiteId)
        val ratingDoc = campsiteDoc.collection("ratings").document(userId)

        db.runTransaction { transaction ->
            val campsiteSnapshot = transaction.get(campsiteDoc)
            val ratingSnapshot = transaction.get(ratingDoc)

            val stars = ratingSnapshot.getDouble("stars") // will be null if rating doesnt exist
            if (stars != null) {
                val oldTotal = campsiteSnapshot.getDouble("ratingTotal") ?: 0.0
                val oldCount = campsiteSnapshot.getLong("ratingCount") ?: 0L

                transaction.delete(ratingDoc)
                transaction.update(
                    campsiteDoc,
                    mapOf(
                        "ratingTotal" to oldTotal - stars,
                        "ratingCount" to oldCount - 1)
                )
            }
        }.await()
    }

    /**
     * Clears the comment on [userId]s rating, but keeps the star rating and review count the same
     */
    suspend fun removeComment(campsiteId: String, userId: String) {
        collection.document(campsiteId)
            .collection("ratings")
            .document(userId)
            .update("comment", "")
            .await()
    }
}