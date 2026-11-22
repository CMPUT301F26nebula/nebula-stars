/*
Contains the class representing a  user's rating of a campsite
 */
package com.example.stars
/**
 * A single user's star rating of a campsite, stored in Firestore at
 * campsites/{campsiteId}/ratings/{userId}
 * Each user gets one rating per campsite, rating again overwrites the old one
 *
 * @param userId unique ID of the user who gave the rating
 * @param campsiteId unique ID of the campsite being rated
 * @param stars the rating from 1.0 to 5.0, increments of 0.5
 * @param comment optional comment from the user
 * @param timestamp when the rating was last made or edited
 */
data class Rating (
    val userId: String = "",
    val campsiteId: String = "",
    val stars: Double = 0.0,
    val comment: String = "",
    val timestamp: Long = 0
)

