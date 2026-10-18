package com.example.stars
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.UUID
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * A container of ImageUploads stored in Firestore and Firebase Cloud Storage
 *
 * @property images a list of all images uploaded to the app. Updates on any changes in Firestore->images.
 * @constructor initializes images from the current state of the Firestore images collection
 */
class ImageRepository {
    private val storageRef = Firebase.storage.reference
    private val db = Firebase.firestore
    private val imageListRef = db.collection("images")

    private val _images: MutableList<ImageUpload> = mutableStateListOf()
    val images: List<ImageUpload>
        get() = _images

    // On initialization, start listening for changes in Firestore
    init {
        imageListRef.addSnapshotListener { snapshot, error ->
            // Propagate any errors through
            if (error != null) {
                return@addSnapshotListener
            }

            // Reset the image list
            _images.clear()

            // Loop over all image entries in db and convert them to objects if applicable
            snapshot?.documents?.forEach { document ->
                val image = document.toObject(ImageUpload::class.java)
                if (image != null) {
                    _images.add(image)
                }
            }
        }
    }

    /**
     * Uploads an image to Firebase Cloud Storage and adds a reference in Firestore
     *
     * @param imageUri the local image URI provided by an image picker
     * @return the unique ID of the uploaded image
     */
    fun uploadImage(imageUri: Uri): String = runBlocking {
        // Every image has its own ID which we generate
        val imageId = UUID.randomUUID().toString()
        // Name it and decide where to put it
        val imagePath = "images/$imageId.jpg"
        val imageRef = storageRef.child(imagePath)

        // Await the upload and get the download URL
        // We want exceptions to propagate up, so we'll wait for it to complete instead of using a listener
        imageRef.putFile(imageUri).await()
        val downloadUrl = imageRef.downloadUrl.await()

        // At this point we know the upload succeeded so we can create our Firestore reference
        // This will implicitly update the local _images once it succeeds
        imageListRef.document(imageId).set(ImageUpload(imageId, downloadUrl))

        // Implicit return from runBlocking
        imageId
    }

    /**
     * Deletes the image [imageId] from the list of images
     */
    fun deleteImage(imageId: String) {
        imageListRef.document(imageId).delete()
    }

    /**
     * Gets the embeddable download URI for the image [imageId]
     * @return the download URI if the image ID is valid, or null otherwise
     */
    fun getImageUri(imageId: String): Uri? {
        val match: ImageUpload? = _images.find {it.id == imageId}
        if (match != null)
            return match.downloadUrl
        return null
    }
}