package com.example.stars

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * A container of UserProfiles stored in Firestore
 *
 * @property users a list of all user profiles registered to the app. Updates on any changes in Firestore->users.
 * @constructor initializes users from the current state of the Firestore users collection
 */
class UserRepository {
    private val db = Firebase.firestore
    private val usersRef = db.collection("users")

    private val _users: MutableList<UserProfile> = mutableStateListOf()
    val users: List<UserProfile>
        get() = _users

    // On initialization, start listening for changes in Firestore
    init {
        usersRef.addSnapshotListener { snapshot, error ->
            // Propagate any errors through
            if (error != null) {
                return@addSnapshotListener
            }

            // Reset the user list
            _users.clear()

            // Loop over all user entries in db and convert them to objects if applicable
            snapshot?.documents?.forEach { document ->
                val user = document.toObject(UserProfile::class.java)
                if (user != null) {
                    _users.add(user)
                }
            }
        }
    }

    /**
     * Adds or updates a [user] in the users list
     * If user.id matches an existing user, updates it, otherwise creates a new entry
     */
    fun addOrUpdateUser(user: UserProfile) = runBlocking {
        usersRef.document(user.id).set(user).await()
    }

    /**
     * Deletes the user with ID [userId] from the list of users
     * Also removes any campsites, reservations, and waiting list positions held by that user
     */
    fun deleteUser(userId: String) = runBlocking {
        // TODO: delete all reservations, campsites, and waiting list positions associated with the user
        val campsiteRepository = CampsiteRepository()
        for (campsite in campsiteRepository.campsites) {
            // Delete any campsites belonging to this user
            if (campsite.ownerId == userId)
                campsiteRepository.deleteCampsite(campsite.id)

            // Remove entries in reservations list for the deleted user
            // TODO: recalculate availability schedule after this change
            // TODO: if userId in campsite waiting list, delete that entry in the waiting list
            val newReservations = campsite.reservations.toMutableList()
            newReservations.removeAll {it.entrantId == userId}
            campsiteRepository.addOrUpdateCampsite(
                Campsite(
                    campsite.id,
                    campsite.title,
                    campsite.price,
                    campsite.capacity,
                    campsite.rules,
                    campsite.ownerId,
                    campsite.imageIds,
                    newReservations.toList(),
                    campsite.availabilitySchedule
                )
            )
        }

        usersRef.document(userId).delete().await()
    }
}