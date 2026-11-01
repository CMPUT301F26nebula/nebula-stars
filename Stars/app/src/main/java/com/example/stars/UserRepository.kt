/*
Contains repository helper class to get and store UserProfile objects from firestore

TODO: Delete waiting list positions for user when deleting user
TODO: Recalculate availability schedule for campsite after deleting user info (if necessary dependent on schedule implementation)
 */
package com.example.stars

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * A container of UserProfiles stored in Firestore
 */
class UserRepository {
    private val db = Firebase.firestore
    private val usersRef = db.collection("users")

    /**
     * Gets the UserProfile from Firestore associated with the given User ID
     */
    suspend fun getUser(userId: String): UserProfile? {
        return usersRef.document(userId).get().await().toObject(UserProfile::class.java)
    }

    /**
     * Adds or updates a [user] in the users list
     * If user.id matches an existing user, updates it, otherwise creates a new entry
     */
    suspend fun addOrUpdateUser(user: UserProfile) {
        usersRef.document(user.id).set(user).await()
    }

    /**
     * Deletes the user with ID [userId] from the list of users
     * Also removes any campsites, reservations, and waiting list positions held by that user
     */
    suspend fun deleteUser(userId: String) {
        // TODO: delete all reservations and waiting list positions associated with the user
        val campsiteRepository = CampsiteRepository()
        for (campsite in campsiteRepository.getUserCampsites(userId)) {
            campsiteRepository.deleteCampsite(campsite.id)
        }

        usersRef.document(userId).delete().await()
    }
}