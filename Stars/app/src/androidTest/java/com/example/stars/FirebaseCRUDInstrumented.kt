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
import kotlinx.coroutines.runBlocking
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
    private lateinit var lotteryRepo: LotteryRepository

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
        runBlocking { userRepo.addOrUpdateUser(UserProfile("test1", "John Doe", "email@email.com", "555-555-5555", UserPermission.OWNER))}
        var user = runBlocking { userRepo.getUser("test1") }
        assertNotNull(user)
        // Match all attributes
        assertEquals("test1", user?.id)
        assertEquals("John Doe", user?.name)
        assertEquals("email@email.com", user?.email)
        assertEquals(UserPermission.OWNER, user?.permission)
        // Delete, search again, and check that it's gone
        runBlocking { userRepo.deleteUser("test1") }
        user = runBlocking { userRepo.getUser("test1") }
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
        val imageId = runBlocking { imageRepo.uploadImage(Uri.fromFile(file)) }
        // Use the returned UUID to check that the image exists and is downloadable
        var image = runBlocking { imageRepo.getImage(imageId) }
        assertNotNull(image)
        assertNotEquals(0, image?.downloadUrl?.length)
        // Delete and confirm it's gone
        runBlocking { imageRepo.deleteImage(imageId) }
        image = runBlocking { imageRepo.getImage(imageId) }
        assertNull(image)
    }

    /**
     * Creates a partially formed campsite, adds a reservation to it, then checks if all information appears as expected
     */
    @Test
    fun addAndRemoveCampsite() {
        // Create a user to own the campsite
        // There is no check whether the user that owns the campsite is actually valid at this time, but do it anyway for realism
        val user = UserProfile("test1", "John Doe", "email@email.com", "555-555-5555", UserPermission.OWNER)
        runBlocking { userRepo.addOrUpdateUser(user) }

        // Add a partial campsite and give it a reservation
        runBlocking { campsiteRepo.addOrUpdateCampsite(Campsite(id = "test2", ownerId = "test1", reservations = listOf(Reservation(id = "test3", timeRange = EpochRange(1, 2))))) }
        var campsite = runBlocking { campsiteRepo.getCampsite("test2") }
        // Campsite should exist and have matching attributes
        assertNotNull(campsite)
        assertEquals("test1", campsite?.ownerId)
        assertEquals(0, campsite?.capacity)
        // Make sure the reservation exists
        assertNotEquals(emptyList<Reservation>(), campsite?.reservations)
        val reservation = campsite?.reservations?.find {it.id == "test3" && it.timeRange == EpochRange(1, 2)}
        assertNotNull(reservation)
        // Delete everything, including the test user
        runBlocking { campsiteRepo.deleteCampsite("test2") }
        campsite = runBlocking { campsiteRepo.getCampsite("test2") }
        assertNull(campsite)

        runBlocking { userRepo.deleteUser("test1", user) }
    }

}