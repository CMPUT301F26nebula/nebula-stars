/**
 * Contains repository helper class to get and store Lottery objects from firestore
 */
package com.example.stars

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

/**
 * A container of lotteries stored in Firestore
 *
 * @param scope the global coroutine scope used to run basic database operations
 */
class LotteryRepository(private val scope: CoroutineScope):
    AbstractUserDataRepository<Lottery>("lotteries", scope, Lottery::class.java) {

    /**
     * Gets a list of all lotteries run by [userId]
     *
     * @return list of all lotteries run by the user, which will be empty if not applicable
     */
    override suspend fun getWithUserId(userId: String): List<Lottery> {
        return getWhereFieldEquals("ownerID", userId)
    }

    /**
     * Gets a list of all lotteries for campsite [campsiteId]]
     *
     * @return list of all lotteries run by the user, which will be empty if not applicable
     */
    suspend fun getWithCampsiteId(campsiteId: String): List<Lottery> {
        return collection
            .whereArrayContains("campsiteIDs", campsiteId)
            .get().await()
            .toObjects(dataClass)
    }

    /*
    /**
     * Gets the Lottery from Firestore associated with the given ID
     */
    suspend fun getFromId(lotteryId: String): Lottery? {
        return getFromId(lotteryId)
    }

    /**
     * Adds or updates a [lottery] in the lotteries list
     * If lottery.id matches an existing lottery, updates it, otherwise creates a new entry
     */
    fun addOrUpdate(lottery: Lottery) {
        addOrUpdateFromId(lottery.id, lottery)
    }

    /**
     * Deletes the lottery with ID [lotteryId] from the list of users
     */
    fun removeFromId(lotteryId: String) {
        removeFromId(lotteryId)
    }
    */
}