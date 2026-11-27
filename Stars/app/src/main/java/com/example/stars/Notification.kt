/*
Data class to represent a notification in the notifications screen
*/
package com.example.stars

/**
 * Represents a notification containing information sent to a user
 * @param id
 * @param userId the user ID of the recipient
 * @param read true if the user has marked the notification as read, false otherwise
 * @param content the message within the notification
 * @param navDestination a string passed to the NavController, which navigates to that location when the notification is tapped
 * @param sender identifies who or what sent the notification
 * @param timestamp when the notification was sent, in epoch ms
 */
data class Notification (
    override val id: String = "",
    val userId: String = "",
    val read: Boolean = false,
    val content: String = "",
    val navDestination: String = "",
    val sender: String = "Unknown",
    val timestamp: Long = System.currentTimeMillis()
): UniqueIdentifier