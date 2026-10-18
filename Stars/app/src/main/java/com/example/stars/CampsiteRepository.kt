package com.example.stars

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * A container of campsites stored in Firestore
 *
 * @property campsites a list of all campsites available in the app. Updates on any changes in Firestore->campsites.
 * @constructor initializes campsites from the current state of the Firestore campsites collection
 */
class CampsiteRepository {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")

    private val _campsites: MutableList<Campsite> = mutableStateListOf()
    val campsites: List<Campsite>
        get() = _campsites

    // On initialization, start listening for changes in Firestore
    init {
        campsitesRef.addSnapshotListener { snapshot, error ->
            // Propagate any errors through
            if (error != null) {
                return@addSnapshotListener
            }

            // Reset the campsite list
            _campsites.clear()

            // Loop over all image entries in db and convert them to objects if applicable
            snapshot?.documents?.forEach { document ->
                val campsite = document.toObject(Campsite::class.java)
                if (campsite != null) {
                    _campsites.add(campsite)
                }
            }
        }
    }

    /**
     * Adds or updates a [campsite] in the campsite list
     * If campsite.id matches an existing campsite, updates it, otherwise creates a new entry
     */
    fun addOrUpdateCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.id).set(campsite)
    }

    /**
     * Deletes the campsite with ID [campsiteId] from the list of campsites
     */
    fun deleteCampsite(campsiteId: String) {
        campsitesRef.document(campsiteId).delete()
    }
}