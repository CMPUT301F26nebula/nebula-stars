/*
Data class to represent a notification in the notifications screen
*/
package com.example.stars

data class Notification (
    override val id: String = "",
    val userId: String = "",
    val read: Boolean = false,
    val content: String = "",
    val navDestination: String = "",
    val senderIds: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
): UniqueIdentifier