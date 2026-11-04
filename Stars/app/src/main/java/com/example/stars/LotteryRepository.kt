package com.example.stars

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class LotteryRepository {

    private val db = Firebase.firestore
    private val lotteriesRef = db.collection("lotteries")

    /**
     * Gets the Lottery from Firestore associated with the given ID
     */
    suspend fun getLottery(lotteryId: String): Lottery? {
        return lotteriesRef.document(lotteryId).get().await().toObject(Lottery::class.java)
    }

    /**
     * Gets a list of all lotteries run by [userId]
     *
     * @return list of all lotteries run by the user, which will be empty if not applicable
     */
    suspend fun getUserLotteries(userId: String): List<Lottery> {
        return lotteriesRef.whereEqualTo("ownerID", userId).get().await().toObjects(Lottery::class.java)
    }

    /**
     * Adds or updates a [lottery] in the lotteries list
     * If lottery.id matches an existing lottery, updates it, otherwise creates a new entry
     */
    suspend fun addOrUpdateLottery(lottery: Lottery) {
        lotteriesRef.document(lottery.id).set(lottery).await()
    }

    /**
     * Deletes the lottery with ID [lotteryId] from the list of users
     */
    suspend fun deleteLottery(lotteryId: String) {
        lotteriesRef.document(lotteryId).delete().await()
    }
}