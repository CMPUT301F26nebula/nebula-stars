/*
Contains repository helper class to get and store UserProfile objects from firestore

TODO: Delete waiting list positions for user when deleting user
TODO: Recalculate availability schedule for campsite after deleting user info (if necessary dependent on schedule implementation)
 */
package com.example.stars

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A container of UserProfiles stored in Firestore
 *
 * @param scope the global coroutine scope used to run basic database operations
 */
class UserRepository(private val scope: CoroutineScope):
    AbstractRepository<UserProfile>("users", scope, UserProfile::class.java) {

    /**
     * Deletes the user [user] with ID [user.id] from the list of users
     * Also removes any campsites, reservations, and waiting list positions held by that user
     * Also edit [user] to be effectively deleted
     */
    suspend fun deleteUser(user: UserProfile) {
        // TODO: delete all reservations and waiting list positions associated with the user
        val campsiteRepository = CampsiteRepository(scope)
        val imageRepository = ImageRepository(scope)

        for (campsite in campsiteRepository.getWithUserId(user.id)) {
            campsiteRepository.removeWithId(campsite.id)
        }

        if (user.profilePic != ""){
            imageRepository.deleteImage(user.profilePic)
        }

        user.name = ""
        user.email = ""
        user.phone = ""
        user.permission = UserPermission.ENTRANT
        user.profilePic = ""
        addOrUpdate(user)
    }

    /**
     * receives [ownerProfile] and uses it to delete all campsites registered by the owner
     * before demoting him to Entrant and banning him from ever again becoming owner or admin
     * TODO: notify owner that they have been banned
     * TODO: notify owner and anyone in a lottery for this site that it has been removed
     */
    suspend fun banOwner(ownerProfile: UserProfile) {

        val campsiteRepository = CampsiteRepository(scope)
        val ownedCampsites = campsiteRepository.getWithUserId(ownerProfile.id)
        for (camp in ownedCampsites) {
            campsiteRepository.removeWithId(camp.id)
        }

        ownerProfile.banned = true
        ownerProfile.permission = UserPermission.ENTRANT
        addOrUpdate(ownerProfile)
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
        val registration = collection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)                  // ends the flow and reports the error
                    return@addSnapshotListener
                }
                // null means the document doesn't exist (or was deleted)
                trySend(snapshot?.toObject(dataClass))
            }

        // runs when the collector stops, so the listener never leaks
        awaitClose { registration.remove() }
    }

    /**
     * Removes a [user] from the database
     */
    fun deleteUserEntirely(user: UserProfile) {
        removeWithId(user.id)
    }

}