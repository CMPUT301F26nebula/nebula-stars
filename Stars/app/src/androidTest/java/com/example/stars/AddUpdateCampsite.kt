/*
Contains basic tests of the campsite uploader/updater
 */
package com.example.stars

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class AddUpdateCampsite {
    @get:Rule
    //val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity
    val composeTestRule = createComposeRule()
    lateinit var navController: TestNavHostController


    /**
     * Adds a new campsite through the UI
     */
    @Test
    fun addCampsite() = runTest {
        //val scenario = ActivityScenario.launch(MainActivity::class.java)
        val user = UserProfile("Unit Test", permission = UserPermission.OWNER)
        val campRepo = CampsiteRepository(this)
        val imageRepo = ImageRepository(this)

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen(navController, user) }
                composable("upload campsite") {
                    UpdateCampsiteScreen(
                        navController,
                        user,
                        campRepo,
                        imageRepo
                    )
                }
            }
        }

        // Wait for home screen to load
        Thread.sleep(2000)
        // Use this UUID to ensure we never accidentally clone an existing campsite
        val uuid = UUID.randomUUID().toString()
        // Go to the uploader screen
        composeTestRule.onNodeWithTag("ownerHome").performClick()
        composeTestRule.onNodeWithTag("uploadCampsiteButton").performClick()
        // Input form fields
        composeTestRule.onNodeWithTag("titleInput").performTextReplacement("Test Campsite $uuid")
        composeTestRule.onNodeWithTag("descriptionInput").performTextReplacement("Home of the Flying Spaghetti Monster")
        composeTestRule.onNodeWithTag("priceInput").performTextReplacement("6.70")
        // This shouldn't apply because of the format check, so the price should remain $6
        composeTestRule.onNodeWithTag("priceInput").performTextReplacement("q")
        composeTestRule.onNodeWithTag("capacityInput").performTextReplacement("4")
        // You can't have .14 of a person, so the capacity should remain 4
        composeTestRule.onNodeWithTag("capacityInput").performTextReplacement("3.14")
        composeTestRule.onNodeWithTag("rulesInput").performTextReplacement("1. Have Fun")

        // TODO: Test geolocation, availability schedule, image uploads
        // Upload it
        composeTestRule.onNodeWithTag("submitCampsite").performClick()
        Log.d("Campsite Update Test", "Clicked")

        // Wait for upload
        Thread.sleep(5000)
        // TODO currently this part just blocks forever since it seems no campsite is being added to firebase for some reason
        // Search the DB for any possible campsites matching this description
        // If the upload succeeded, there should be exactly one such campsite
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            runBlocking {
                campRepo.getWhereFieldEquals("title", "Test Campsite $uuid").isNotEmpty()
            }
        }

        val possibleCampsites = campRepo.getWhereFieldEquals("title", "Test Campsite $uuid")
        assertEquals(1, possibleCampsites.size)

        // Check values and clean up
        for (campsite in possibleCampsites) {
            assertEquals("Home of the Flying Spaghetti Monster", campsite.description)
            assertEquals(6.70, campsite.price, 0.0)
            assertEquals(4, campsite.capacity)
            assertEquals("1. Have Fun", campsite.rules)
            //campsitesRef.document(campsite.id).delete()
            campRepo.removeWithId(campsite.id)
        }
        //scenario.close()
    }
}