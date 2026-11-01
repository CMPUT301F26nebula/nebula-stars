/*
Contains repository helper class to get and store ImageUpload objects from firestore
 */
package com.example.stars
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.UUID
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * A container of ImageUploads stored in Firestore and Firebase Cloud Storage
 */
class ImageRepository {
    private val storageRef = Firebase.storage.reference
    private val db = Firebase.firestore
    private val imageListRef = db.collection("images")

    /**
     * Gets the ImageUpload from Firestore associate with the given ID
     */
    suspend fun getImage(imageId: String): ImageUpload? {
        return imageListRef.document(imageId).get().await().toObject(ImageUpload::class.java)
    }

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
        imageListRef.document(imageId).set(ImageUpload(imageId, downloadUrl)).await()

        return imageId
    }

    /**
     * Deletes the image [imageId] from the list of images
     */
    suspend fun deleteImage(imageId: String) {
        storageRef.child("images/$imageId.jpg").delete().await()
        imageListRef.document(imageId).delete().await()
    }

    /**
     * Gets the embeddable download URI for the image [imageId]
     * @return the download URI if the image ID is valid, or null otherwise
     */
    suspend fun getImageUri(imageId: String): String? {
        val match: ImageUpload? = getImage(imageId)
        if (match != null)
            return match.downloadUrl
        return null
    }
}