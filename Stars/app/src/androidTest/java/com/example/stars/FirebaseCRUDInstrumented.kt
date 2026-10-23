/*
This file contains instrumented tests to use for Firebase integration and backend operations

TODO: Expand campsite testing as the campsite class gets implemented further
 */
package com.example.stars

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

/**
 * A basic set of tests for simple CRUD operations on objects stored in firebase
 */
class FirebaseCRUDInstrumented {
    private lateinit var userRepo: UserRepository
    private lateinit var campsiteRepo: CampsiteRepository
    private lateinit var imageRepo: ImageRepository

    /**
     * Configures firebase repositories with their items
     */
    @Before
    fun setUp() {
        userRepo = UserRepository()
        campsiteRepo = CampsiteRepository()
        imageRepo = ImageRepository()
    }

    /**
     * Adds a user, confirms its properties are correct, then deletes it
     */
    @Test
    fun addAndDeleteUser() {
        userRepo.addOrUpdateUser(UserProfile("test1", "John Doe", "email@email.com", UserPermission.OWNER))
        var user = userRepo.users.find {it.id == "test1"}
        assertNotNull(user)
        // Match all attributes
        assertEquals("test1", user?.id)
        assertEquals("John Doe", user?.name)
        assertEquals("email@email.com", user?.email)
        assertEquals(UserPermission.OWNER, user?.permission)
        // Delete, search again, and check that it's gone
        userRepo.deleteUser("test1")
        user = userRepo.users.find {it.id == "test1"}
        assertNull(user)
    }

    /**
     * Uploads a dummy image file, tests that it uploaded successfully, then deletes it from storage and deletes its firestore reference
     */
    @Test
    fun uploadAndDeleteImage() {
        // Set up dummy image file
        val file = File(ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir, "testing123.jpg")
        FileOutputStream(file).use { out ->
            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        // Upload the dummy image to storage
        // Note that uploadImage returns the UUID of the new image
        val imageId = imageRepo.uploadImage(Uri.fromFile(file))
        // Use the returned UUID to check that the image exists and is downloadable
        var image = imageRepo.images.find {it.id == imageId}
        assertNotNull(image)
        assertNotEquals(0, image?.downloadUrl?.length)
        // Delete and confirm it's gone
        imageRepo.deleteImage(imageId)
        image = imageRepo.images.find {it.id == imageId}
        assertNull(image)
    }

    /**
     * Creates a partially formed campsite, adds a reservation to it, then checks if all information appears as expected
     */
    @Test
    fun addAndRemoveCampsite() {
        // Create a user to own the campsite
        // There is no check whether the user that owns the campsite is actually valid at this time, but do it anyway for realism
        userRepo.addOrUpdateUser(UserProfile("test1", "John Doe", "email@email.com", UserPermission.OWNER))

        // Add a partial campsite and give it a reservation
        campsiteRepo.addOrUpdateCampsite(Campsite(id = "test2", ownerId = "test1", reservations = listOf(Reservation(id = "test3", timeRange = EpochRange(1, 2)))))
        var campsite = campsiteRepo.campsites.find {it.id == "test2"}
        // Campsite should exist and have matching attributes
        assertNotNull(campsite)
        assertEquals("test1", campsite?.ownerId)
        assertEquals(0, campsite?.capacity)
        // Make sure the reservation exists
        assertNotEquals(emptyList<Reservation>(), campsite?.reservations)
        val reservation = campsite?.reservations?.find {it.id == "test3" && it.timeRange == EpochRange(1, 2)}
        assertNotNull(reservation)
        // Delete everything, including the test user
        campsiteRepo.deleteCampsite("test2")
        campsite = campsiteRepo.campsites.find {it.id == "test2"}
        assertNull(campsite)

        userRepo.deleteUser("test1")
    }
}