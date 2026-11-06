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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

/**
 * A basic set of tests for simple CRUD operations on objects stored in firebase
 */
class FirebaseCRUDInstrumented {
    /**
     * Adds a user, confirms its properties are correct, then deletes it
     */
    @Test
    fun addAndDeleteUser() = runTest {
        val userRepo = UserRepository(this)
        val userProfile = UserProfile("test1", "John Doe", "email@email.com", "555-555-5555",
            listOf(), false,UserPermission.OWNER)
        userRepo.addOrUpdateUser(userProfile)
        var user = userRepo.getUser("test1")
        assertNotNull(user)
        // Match all attributes
        assertEquals("test1", user?.id)
        assertEquals("John Doe", user?.name)
        assertEquals("email@email.com", user?.email)
        assertEquals(UserPermission.OWNER, user?.permission)
        // Delete, search again, and check that it's gone
        userRepo.deleteUser(userProfile)
        Thread.sleep(5000)
        user = userRepo.getUser("test1")
        assertNull(user)
    }

    /**
     * Uploads a dummy image file, tests that it uploaded successfully, then deletes it from storage and deletes its firestore reference
     */
    @Test
    fun uploadAndDeleteImage() = runTest {
        val imageRepo = ImageRepository(this)
        // Set up dummy image file
        val file = File(ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir,
            "testing123.jpg")
        FileOutputStream(file).use { out ->
            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        // Upload the dummy image to storage
        // Note that uploadImage returns the UUID of the new image
        val imageId = imageRepo.uploadImage(Uri.fromFile(file))
        // Use the returned UUID to check that the image exists and is downloadable
        var image = imageRepo.getImage(imageId)
        assertNotNull(image)
        assertNotEquals(0, image?.downloadUrl?.length)
        // Delete and confirm it's gone
        imageRepo.deleteImage(imageId)
        Thread.sleep(5000)
        image = imageRepo.getImage(imageId)
        assertNull(image)
    }

    /**
     * Creates a partially formed campsite, adds a reservation to it, then checks if all information appears as expected
     */
    @Test
    fun addAndRemoveCampsite() = runTest {
        val userRepo = UserRepository(this)
        val campsiteRepo = CampsiteRepository(this)
        val reservationRepo = ReservationRepository(this)
        // Create a user to own the campsite
        // There is no check whether the user that owns the campsite is actually valid at this time,
        // but do it anyway for realism
        val userO = UserProfile("testUO", "John Doe", "email@email.com", "555-555-5555",
            listOf(),
            false, UserPermission.OWNER)
        userRepo.addOrUpdateUser(userO)
        assertNotNull(userO)
        assertEquals(userO, userRepo.getUser(userO.id))

        // Create a user to reserve the campsite
        val userE = UserProfile("testUE", "Jenny Calender", "email@email.com", "555-555-5555",
            listOf(),
            false, UserPermission.ENTRANT)
        userRepo.addOrUpdateUser(userE)
        assertNotNull(userE)
        assertEquals(userE, userRepo.getUser(userE.id))

        // Add a partial campsite and give it a reservation
        val campsite = Campsite(id = "testC", ownerId = "testUO")
        campsiteRepo.addOrUpdateCampsite(campsite)

        // Campsite should exist and have matching attributes
        assertNotNull(campsite)
        assertEquals(campsite, campsiteRepo.getCampsite(campsite.id))
        assertEquals("testUO", campsite.ownerId)
        assertEquals(0, campsite.capacity)

        reservationRepo.addUserReservation(userE.id, campsite.id, EpochRange(1,2))
        // Make sure the reservation exists
        assertNotNull(reservationRepo.getUserReservations(userE.id))
        // Delete everything, including the test user
        campsiteRepo.deleteCampsite("testC")
        Thread.sleep(5000)
        assertNull(campsiteRepo.getCampsite("testC"))

        userRepo.deleteUser(userO)
        userRepo.deleteUser(userE)
    }

}