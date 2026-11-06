package com.example.stars

import androidx.compose.ui.test.junit4.v2.createComposeRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * testing class for admin abilities
 */
class AdminAbilityTests {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * test to make sure that ban both changes ban state to true and deletes all owned campsites
     * TODO: add notification to affected user tests when notifications are implemented
     */
    @Test
    fun createOwnerCreateCampSitesBanOwner() = runTest {
        val userRepo = UserRepository(this)
        val campsiteRepo = CampsiteRepository(this)

        val owner = UserProfile("Admin Power Test","Test Owner", "admin@test.power",
            phone = "", permission = UserPermission.OWNER)

        val campsite = Campsite("Admin Campsite Test", "Test Campsite", 10.0, 5,
            "does not need description","Don't die when deleted", "Admin Power Test")

        userRepo.addOrUpdateUser(owner)
        campsiteRepo.addOrUpdateCampsite(campsite)

        userRepo.banOwner(owner)

        val deletedCampsite = campsiteRepo.getCampsite(campsite.id)
        assertEquals(null, deletedCampsite)
        assertEquals(true, owner.banned)

        userRepo.deleteUser(owner)
        val deletedOwner = userRepo.getUser(owner.id)
        assertNull(deletedOwner)
    }

    /**
     * test to make sure that deleting a user deletes their profile and campsites
     * TODO: add notification to affected user tests when notifications are implemented
     */
    @Test
    fun createOwnerAndCampsiteAndDeleteOwnerProfile() = runTest {
        val userRepo = UserRepository(this)
        val campsiteRepo = CampsiteRepository(this)

        val owner = UserProfile("Admin Power Test","Test Owner", "admin@test.power",
            phone = "", permission = UserPermission.OWNER)

        val campsite = Campsite("Admin Campsite Test", "Test Campsite", 10.0, 5,
            "does not need description","Don't die when deleted", "Admin Power Test")

        userRepo.addOrUpdateUser(owner)
        campsiteRepo.addOrUpdateCampsite(campsite)
        userRepo.deleteUser(owner)
        val deletedCampsite = campsiteRepo.getCampsite(campsite.id)
        assertEquals(null, deletedCampsite)
        assertEquals("", owner.name)

        userRepo.deleteUser(owner)
        val deletedOwner = userRepo.getUser(owner.id)
        assertNull(deletedOwner)
    }
}