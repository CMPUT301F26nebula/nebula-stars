package com.example.stars
import android.net.Uri

/**
 * A reference to an uploaded image in Firebase Cloud Storage
 *
 * @param id the unique ID assigned to the image
 * @param downloadUrl the embeddable download link to the image
 */
data class ImageUpload (val id: String = "", val downloadUrl: String = "")