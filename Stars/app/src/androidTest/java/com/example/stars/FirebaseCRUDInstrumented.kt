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

class FirebaseCRUDInstrumented {
    private lateinit var userRepo: UserRepository
    private lateinit var campsiteRepo: CampsiteRepository
    private lateinit var imageRepo: ImageRepository

    @Before
    fun setUp() {
        userRepo = UserRepository()
        campsiteRepo = CampsiteRepository()
        imageRepo = ImageRepository()
    }

    @Test
    fun addAndDeleteUser() {
        userRepo.addOrUpdateUser(UserProfile("test1", "John Doe", "email@email.com", UserPermission.OWNER))
        var user = userRepo.users.find {it.id == "test1"}
        assertNotNull(user)
        assertEquals("test1", user?.id)
        assertEquals("John Doe", user?.name)
        assertEquals("email@email.com", user?.email)
        assertEquals(UserPermission.OWNER, user?.permission)
        userRepo.deleteUser("test1")
        user = userRepo.users.find {it.id == "test1"}
        assertNull(user)
    }

    @Test
    fun uploadAndDeleteImage() {
        // Set up dummy image file
        val file = File(ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir, "testing123.jpg")
        FileOutputStream(file).use { out ->
            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        val imageId = imageRepo.uploadImage(Uri.fromFile(file))
        var image = imageRepo.images.find {it.id == imageId}
        assertNotNull(image)
        assertNotEquals(0, image?.downloadUrl?.length)
        imageRepo.deleteImage(imageId)
        image = imageRepo.images.find {it.id == imageId}
        assertNull(image)
    }

    @Test
    fun addAndRemoveCampsite() {
        userRepo.addOrUpdateUser(UserProfile("test1", "John Doe", "email@email.com", UserPermission.OWNER))

        campsiteRepo.addOrUpdateCampsite(Campsite(id = "test2", ownerId = "test1"))
        var campsite = campsiteRepo.campsites.find {it.id == "test2"}
        assertNotNull(campsite)
        assertEquals("test1", campsite?.ownerId)
        assertEquals(0, campsite?.capacity)
        assertEquals(emptyList<Reservation>(), campsite?.reservations)
        campsiteRepo.deleteCampsite("test2")
        campsite = campsiteRepo.campsites.find {it.id == "test2"}
        assertNull(campsite)

        userRepo.deleteUser("test1")
    }
}