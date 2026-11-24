/*
Contains repository helper class to get and store ImageUpload objects from firestore
 */
package com.example.stars
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A container of ImageUploads stored in Firestore and Firebase Cloud Storage
 *
 * @param scope the global coroutine scope used to run basic database operations
 */
class ImageRepository(private val scope: CoroutineScope):
    AbstractRepository<ImageUpload>("images", scope, ImageUpload::class.java) {
    private val storageRef = Firebase.storage.reference

    /**
     * Uploads an image to Firebase Cloud Storage and adds a reference in Firestore
     *
     * @param imageUri the local image URI provided by an image picker
     * @return the unique ID of the uploaded image
     */
    suspend fun uploadImage(imageUri: Uri): String {
        // Every image has its own ID which we generate
        val imageId = UUID.randomUUID().toString()
        // Name it and decide where to put it
        val imagePath = "images/$imageId.jpg"
        val imageRef = storageRef.child(imagePath)

        // Await the upload and get the download URL
        // We want exceptions to propagate up, so we'll wait for it to complete instead of using a listener
        imageRef.putFile(imageUri).await()
        val downloadUrl = imageRef.downloadUrl.await().toString()

        // At this point we know the upload succeeded so we can create our Firestore reference
        addOrUpdateWithId(imageId, ImageUpload(imageId, downloadUrl))

        return imageId
    }

    /**
     * Deletes the image [imageId] from the list of images
     */
    fun deleteImage(imageId: String) {
        scope.launch {
            storageRef.child("images/$imageId.jpg").delete().await()
            collection.document(imageId).delete().await()
        }
        // TODO not sure what the above double deletion is for, if its not needed use vvv instead since it updates listening threads
        // removeFromId(imageId)
    }

    /**
     * Gets the embeddable download URI for the image [imageId]
     * @return the download URI if the image ID is valid, or null otherwise
     */
    suspend fun getImageUri(imageId: String): String? {
        val match: ImageUpload? = getWithId(imageId)
        if (match != null)
            return match.downloadUrl
        return null
    }
}