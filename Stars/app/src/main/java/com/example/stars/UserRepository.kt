/*
Contains repository helper class to get and store UserProfile objects from firestore

TODO: Delete waiting list positions for user when deleting user
TODO: Recalculate availability schedule for campsite after deleting user info (if necessary dependent on schedule implementation)
 */
package com.example.stars

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A container of UserProfiles stored in Firestore
 *
 * @param scope the global coroutine scope used to run basic database operations
 */
class UserRepository(private val scope: CoroutineScope) {
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
     * Deletes the user [user] with ID [user.id] from the list of users
     * Also removes any campsites, reservations, and waiting list positions held by that user
     * Also edit [user] to be effectively deleted
     */
    suspend fun deleteUser(user: UserProfile) {
        // TODO: delete all reservations and waiting list positions associated with the user
        val campsiteRepository = CampsiteRepository(scope)
        val imageRepository = ImageRepository(scope)

        for (campsite in campsiteRepository.getUserCampsites(user.id)) {
            campsiteRepository.deleteCampsite(campsite.id)
        }

        if (user.profilePic != ""){
            imageRepository.deleteImage(user.profilePic)
        }

        user.name = ""
        user.email = ""
        user.phone = ""
        user.permission = UserPermission.ENTRANT
        user.profilePic = ""
        addOrUpdateUser(user)

    }

    /**
     * returns the current list of users
     */
    suspend fun getUsersList(): MutableList<UserProfile> {
        return usersRef.get().await().toObjects(UserProfile::class.java)
    }


    /**
     * receives [ownerProfile] and uses it to delete all campsites registered by the owner
     * before demoting him to Entrant and banning him from ever again becoming owner or admin
     * TODO: notify owner that they have been banned
     * TODO: notify owner and anyone in a lottery for this site that it has been removed
     */
    suspend fun banOwner(ownerProfile: UserProfile) {

        val campsiteRepository = CampsiteRepository(scope)
        val ownedCampsites = campsiteRepository.getUserCampsites(ownerProfile.id)
        for (camp in ownedCampsites) {
            campsiteRepository.deleteCampsite(camp.id)
        }

        ownerProfile.banned = true
        ownerProfile.permission = UserPermission.ENTRANT
        addOrUpdateUser(ownerProfile)

    }

    /**
     * Generted by claude prompt: "write me the code to make a listener for changes in user profile"
     * Claude sonnet 5.5, 2026-11-21
     * https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
     */
    /**
     * uses [userId] to watch firebase for changes in the current users profile so that you get
     * instantly returned to make user profile if you get deleted.
     */
    fun observeUser(userId: String): Flow<UserProfile?> = callbackFlow {
        val registration = usersRef.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)                  // ends the flow and reports the error
                    return@addSnapshotListener
                }
                // null means the document doesn't exist (or was deleted)
                trySend(snapshot?.toObject(UserProfile::class.java))
            }

        // runs when the collector stops, so the listener never leaks
        awaitClose { registration.remove() }
    }

    suspend fun deleteUserEntirely(user: UserProfile){
        usersRef.document(user.id).delete().await()
    }

}