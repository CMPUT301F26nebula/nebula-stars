/**
 * Base class for all repository type classes
 */
package com.example.stars

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Abstract class all other repository type classes inherit from
 *
 * @param T object stored in the repository collection
 * @param collectionPath path to the Firebase collection to access
 * @param externalScope CoroutineScope to run async functions in
 */
abstract class AbstractRepository<T: UniqueIdentifier>(
    collectionPath: String,
    protected val externalScope: CoroutineScope,
    protected val dataClass: Class<T>
) {
    protected val db = Firebase.firestore
    protected val collection = db.collection(collectionPath)

    /**
     * Gets all documents in the repository collection
     *
     * @return list of all documents as a [dataClass]
     */
    open suspend fun getAll(): List<T> {
        return collection.get().await().toObjects(dataClass)
    }

    /**
     * Searches the collection for all documents whose field matches key
     *
     * @param field name of the document field to check
     * @param key value to match against
     * @return list of matching documents cast to [dataClass]
     */
    open suspend fun getWhereFieldEquals(field: String, key: String): List<T> {
        return collection
            .whereEqualTo(field, key)
            .get().await()
            .toObjects(dataClass)
    }

    /**
     * Returns the document with unique id [id]
     *
     * @return the document cast to a [dataClass], or null if no such document exists
     */
    open suspend fun getWithId(id: String): T? {
        return collection.document(id).get().await().toObject(dataClass)
    }

    /**
     * Deletes the document with unique id [id]
     */
    open fun removeWithId(id: String) {
        externalScope.launch {
            collection.document(id).delete()
        }
    }

    /**
     * Adds or updates a document with the id in [data]
     */
    open fun addOrUpdate(data: T) {
        addOrUpdateWithId(data.id, data)
    }

    /**
     * Add or update a document by its unique [id]
     *
     * @param id documents unique id
     * @param data object to add
     */
    open fun addOrUpdateWithId(id: String, data: T) {
        externalScope.launch {
            collection.document(id).set(data).await()
        }
    }

    /**
     * Adds a listener callback for the entire collection
     *
     * @param callback callback function that receives a list of [dataClass]
     */
    fun addListener(callback: (List<T?>) -> Unit) {
        collection.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                Log.d("Repository(${collection.path})", "Listener error: ", exception)
                return@addSnapshotListener
            }
            if (snapshots != null) {
                callback(snapshots.toObjects(dataClass))
            }
        }
    }

    /**
     * Add a listener callback for changes to a document
     *
     * @param id document id to listen on
     * @param callback callback function that receives the document cast to a [dataClass]
     */
    fun addListenerToId(id: String, callback: (T?) -> Unit) {
        collection.document(id).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.d("Repository(${collection.path})", "ListenerToId error: ", exception)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                callback(snapshot.toObject(dataClass))
            }
        }
    }
}

/**
 * Abstract base class for repository type classes that handle data owned by a user
 */
abstract class AbstractUserDataRepository<T: UniqueIdentifier>(
    collectionPath: String,
    externalScope: CoroutineScope,
    dataClass: Class<T>
): AbstractRepository<T>(collectionPath, externalScope, dataClass) {

    /**
     * Returns all documents whose unique id matches [userId] as objects of type [dataClass]
     */
    abstract suspend fun getWithUserId(userId: String): List<T>
}