/*
Contains repository helper class to get and store Notification objects from firestore
 */
package com.example.stars

import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A container of lotteries stored in Firestore
 *
 * @param scope the global coroutine scope used to run basic database operations
 */
class NotificationRepository (private val scope: CoroutineScope):
    AbstractUserDataRepository<Notification>("notifications", scope, Notification::class.java) {

    /**
     * Gets a list of all notifications sent to [userId]
     *
     * @return list of all notifications for the user, which will be empty if not applicable
     */
    override suspend fun getWithUserId(userId: String): List<Notification> {
        return getWhereFieldEquals("userId", userId)
    }

    /**
     * Gets the list of all notifications sent globally
     *
     * @return list of all notifications, empty if not applicable
     */
    suspend fun getGlobalNotifications(): List<Notification> {
        return getAll()
    }

    /**
     * Marks a notification [notification] as read
     */
    fun markAsRead(notification: Notification) {
        val newNotification = Notification(notification.id, notification.userId, true, notification.content, notification.navDestination, notification.senderIds, notification.timestamp)
        addOrUpdateWithId(newNotification.id, newNotification)
    }

    /**
     * Adds a [notification] to the list of notifications
     */
    fun sendNotification(notification: Notification) {
        scope.launch {
            // collection.document(notification.id).set(notification).await()
            addOrUpdate(notification)
            val data = mapOf(
                "userId" to notification.userId,
                "content" to notification.content
            )
            Firebase.functions
                .getHttpsCallable("sendPushNotification")
                .call(data)
                .await()
        }
    }

    /**
     * Triggers the redraw function on firebase, drawing winners back up to number of winners
     */
    fun redrawLottery(lotteryId: String) {
        scope.launch {
            val data = mapOf(
                "lotteryId" to lotteryId
            )
            Firebase.functions
                .getHttpsCallable("RedrawLottery")
                .call(data)
                .await()
        }
    }
}