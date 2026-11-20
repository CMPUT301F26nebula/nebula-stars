/*
Contains repository helper class to get and store Notification objects from firestore
 */
package com.example.stars

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A container of lotteries stored in Firestore
 *
 * @param scope the global coroutine scope used to run basic database operations
 */
class NotificationRepository (private val scope: CoroutineScope) {
    private val db = Firebase.firestore
    private val notificationsRef = db.collection("notifications")

    /**
     * Gets a list of all notifications sent to [userId]
     *
     * @return list of all notifications for the user, which will be empty if not applicable
     */
    suspend fun getUserNotifications(userId: String): List<Notification> {
        return notificationsRef.whereEqualTo("userId", userId).get().await().toObjects(Notification::class.java)
    }

    /**
     * Marks a notification [notification] as read
     */
    fun markAsRead(notification: Notification) {
        scope.launch {
            val newNotification = Notification(notification.id, notification.userId, true, notification.content)
            notificationsRef.document(newNotification.id).set(newNotification).await()
        }
    }

    /**
     * Adds a [notification] to the list of notifications
     */
    fun sendNotification(notification: Notification) {
        scope.launch {
            notificationsRef.document(notification.id).set(notification).await()
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
}