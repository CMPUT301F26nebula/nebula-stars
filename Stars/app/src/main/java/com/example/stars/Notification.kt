/*
Data class to represent a notification in the notifications screen
*/
package com.example.stars

data class Notification (
    val id: String = "",
    val userId: String = "",
    val read: Boolean = false,
    val content: String = "",
    val navDestination: String = ""
)