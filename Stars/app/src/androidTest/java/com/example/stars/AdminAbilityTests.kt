package com.example.stars

import androidx.compose.ui.test.junit4.v2.createComposeRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * testing class for admin abilities
 */
class AdminAbilityTests {

    private lateinit var userRepo: UserRepository
    private lateinit var campsiteRepo: CampsiteRepository

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Configures firebase repositories with their items
     */
    @Before
    fun setUp() {
        userRepo = UserRepository()
        campsiteRepo = CampsiteRepository()
    }

    /**
     * test to make sure that ban both changes ban state to true and deletes all owned campsites
     * TODO: add notification to affected user tests when notifications are implemented
     */
    @Test
    fun createOwnerCreateCampSitesBanOwner(){

        val owner = UserProfile("Admin Power Test","Test Owner", "admin@test.power", phone = "", banned = false,
            UserPermission.OWNER)

        val campsite = Campsite("Admin Campsite Test", "Test Campsite", 10.0, 5,
            "does not need description","Don't die when deleted", "Admin Power Test")

        runBlocking { userRepo.addOrUpdateUser(owner) }
        runBlocking { campsiteRepo.addOrUpdateCampsite(campsite) }

        runBlocking { userRepo.banOwner(owner) }

        val deleted = runBlocking { campsiteRepo.getCampsite(campsite.id) }
        assertEquals(null, deleted)
        assertEquals(true, owner.banned)
    }

    /**
     * test to make sure that deleting a user deletes their profile and campsites
     * TODO: add notification to affected user tests when notifications are implemented
     */
    @Test
    fun createOwnerAndCampsiteAndDeleteOwnerProfile(){
        val owner = UserProfile("Admin Power Test","Test Owner", "admin@test.power", phone = "", banned = false,
            UserPermission.OWNER)

        val campsite = Campsite("Admin Campsite Test", "Test Campsite", 10.0, 5,
            "does not need description","Don't die when deleted", "Admin Power Test")

        runBlocking { userRepo.addOrUpdateUser(owner) }
        runBlocking { campsiteRepo.addOrUpdateCampsite(campsite) }
        runBlocking { userRepo.deleteUser(owner) }
        val deleted = runBlocking { campsiteRepo.getCampsite(campsite.id) }
        assertEquals(null, deleted)
        assertEquals("", owner.name)
    }
}