/**
 * Base class for all repository type classes
 */
package com.example.stars

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 * Abstract class all other repository type classes inherit from
 *
 * @param T object stored in the repository collection
 * @param collectionPath path to the Firebase collection to access
 * @param externalScope CoroutineScope to run async functions in
 */
abstract class AbstractRepository<T : UniqueIdentifier>(
    collectionPath: String,
    protected val externalScope: CoroutineScope,
    protected val dataClass: Class<T>
) {
    protected val db = Firebase.firestore
    protected val collection = db.collection(collectionPath)

    // Caches the last known field-value map for each fetched document
    protected val lastKnownState = ConcurrentHashMap<String, Map<String, Any?>>()

    /**
     * Gets all documents in the repository collection
     *
     * @return list of all documents as a [dataClass]
     */
    open suspend fun getAll(): List<T> {
        val snapshots = collection.get().await()
        snapshots.documents.forEach { cacheSnapshot(it) }
        return snapshots.toObjects(dataClass)
    }

    /**
     * Searches the collection for all documents whose field matches key
     *
     * @param field name of the document field to check
     * @param key value to match against
     * @return list of matching documents cast to [dataClass]
     */
    open suspend fun getWhereFieldEquals(field: String, key: String): List<T> {
        val snapshots = collection
            .whereEqualTo(field, key)
            .get().await()
        snapshots.documents.forEach { cacheSnapshot(it) }
        return snapshots.toObjects(dataClass)
    }

    /**
     * Returns the document with unique id [id]
     *
     * @return the document cast to a [dataClass], or null if no such document exists
     */
    open suspend fun getWithId(id: String): T? {
        if (id == "") {
            return null
        }
        val snapshot = collection.document(id).get().await()
        cacheSnapshot(snapshot)
        return snapshot.toObject(dataClass)
    }

    /**
     * Deletes the document with unique id [id]
     */
    open fun removeWithId(id: String) {
        lastKnownState.remove(id)
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
     * Add or update a document by its unique [id].
     * If the document was previously fetched, only modified fields are sent to Firestore.
     *
     * @param id document's unique id
     * @param data object to add or update
     */
    open fun addOrUpdateWithId(id: String, data: T) {
        externalScope.launch {
            val currentState = toFieldMap(data)
            val previousState = lastKnownState[id]

            if (previousState == null) {
                // Document wasn't cached (e.g., brand new) — perform a full write
                collection.document(id).set(data).await()
                lastKnownState[id] = currentState
                return@launch
            }

            // Find only the fields that changed since the last fetch
            val changedFields = currentState.filter { (key, newValue) ->
                previousState[key] != newValue
            }

            if (changedFields.isEmpty()) {
                return@launch // Nothing changed; skip unnecessary Firestore write
            }

            try {
                collection.document(id).update(changedFields).await()
                lastKnownState[id] = previousState + changedFields
            } catch (e: FirebaseFirestoreException) {
                // Fallback to set() if the document was deleted on the server
                if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    collection.document(id).set(data).await()
                    lastKnownState[id] = currentState
                } else {
                    throw e
                }
            }
        }
    }

    /**
     * Adds a listener callback for the entire collection
     *
     * @param callback callback function that receives a list of [dataClass]
     */
    fun addListener(callback: (List<T?>) -> Unit): ListenerRegistration {
        return collection.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                Log.d("Repository(${collection.path})", "Listener error: ", exception)
                return@addSnapshotListener
            }
            if (snapshots != null) {
                snapshots.documents.forEach { cacheSnapshot(it) }
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
    fun addListenerToId(id: String, callback: (T?) -> Unit): ListenerRegistration {
        return collection.document(id).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.d("Repository(${collection.path})", "ListenerToId error: ", exception)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                cacheSnapshot(snapshot)
                callback(snapshot.toObject(dataClass))
            }
        }
    }

    // The Below 3 functions + current AddOrUpdate are written by gemini
    // https://gemini.google.com/share/d/1N88E3CVlmpVC5ZIelAsc4z1-3ZYj3mxF?usp=sharing 2026-11-28
    private fun cacheSnapshot(snapshot: DocumentSnapshot) {
        val data = snapshot.data
        if (snapshot.exists() && data != null) {
            lastKnownState[snapshot.id] = data
        } else {
            lastKnownState.remove(snapshot.id)
        }
    }

    /**
     * Converts [data] into a map of Firestore field names to normalized values.
     * Respects Firestore's @Exclude and @PropertyName annotations.
     */
    private fun toFieldMap(data: T): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        var currentClass: Class<*>? = data.javaClass

        while (currentClass != null && currentClass != Any::class.java) {
            for (field in currentClass.declaredFields) {
                if (Modifier.isStatic(field.modifiers) || Modifier.isTransient(field.modifiers)) {
                    continue
                }
                if (field.isAnnotationPresent(Exclude::class.java)) {
                    continue
                }
                field.isAccessible = true
                val key = field.getAnnotation(PropertyName::class.java)?.value ?: field.name
                map[key] = normalizeValue(field.get(data))
            }
            currentClass = currentClass.superclass
        }
        return map
    }

    /**
     * Normalizes Kotlin/JVM number types to match Firestore's storage types
     * (Long for integers, Double for floating-point) to prevent false-positive diffs.
     */
    private fun normalizeValue(value: Any?): Any? {
        return when (value) {
            is Byte, is Short, is Int -> (value as Number).toLong()
            is Float -> value.toDouble()
            is List<*> -> value.map { normalizeValue(it) }
            is Map<*, *> -> value.mapValues { (_, v) -> normalizeValue(v) }
            else -> value
        }
    }
}

/**
 * Abstract base class for repository type classes that handle data owned by a user
 */
abstract class AbstractUserDataRepository<T : UniqueIdentifier>(
    collectionPath: String,
    externalScope: CoroutineScope,
    dataClass: Class<T>
) : AbstractRepository<T>(collectionPath, externalScope, dataClass) {

    /**
     * Returns all documents whose unique id matches [userId] as objects of type [dataClass]
     */
    abstract suspend fun getWithUserId(userId: String): List<T>
}