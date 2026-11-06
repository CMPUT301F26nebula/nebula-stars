/*
Contains a simple data class representing an uploaded image reference in firestore
 */
package com.example.stars

/**
 * A reference to an uploaded image in Firebase Cloud Storage
 *
 * @param id the unique ID assigned to the image
 * @param downloadUrl the embeddable download link to the image
 */
data class ImageUpload (val id: String = "", val downloadUrl: String = "")