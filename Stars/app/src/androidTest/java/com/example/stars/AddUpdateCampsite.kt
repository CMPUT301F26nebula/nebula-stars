/*
Contains basic tests of the campsite uploader/updater
 */
package com.example.stars

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class AddUpdateCampsite {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity

    /**
     * Adds a new campsite through the UI
     */
    @Test
    fun addCampsite(){
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        // Wait for home screen to load
        Thread.sleep(2000)
        // Use this UUID to ensure we never accidentally clone an existing campsite
        val uuid = UUID.randomUUID().toString()
        // Go to the uploader screen
        composeTestRule.onNodeWithTag("uploadCampsiteButton").performClick()

        composeTestRule.onNodeWithTag("titleInput").performTextReplacement("Test Campsite ${uuid}")
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
        // Search the DB for any possible campsites matching this description
        val db = Firebase.firestore
        val campsitesRef = db.collection("campsites")
        // If the upload succeeded, there should be exactly one such campsite
        composeTestRule.mainClock.advanceTimeUntil(timeoutMillis = 10000) {
            runBlocking {
                campsitesRef
                    .whereEqualTo("title", "Test Campsite ${uuid}")
                    .get()
                    .await()
                    .toObjects(Campsite::class.java)
            }.size > 0
        }
        val possibleCampsites = runBlocking {
            campsitesRef
                .whereEqualTo("title", "Test Campsite ${uuid}")
                .get()
                .await()
                .toObjects(Campsite::class.java)
        }
        assertEquals(1, possibleCampsites.size)
        // Check values and clean up
        for (campsite in possibleCampsites) {
            assertEquals("Home of the Flying Spaghetti Monster", campsite.description)
            assertEquals(6.70, campsite.price, 0.0)
            assertEquals(4, campsite.capacity)
            assertEquals("1. Have Fun", campsite.rules)
            campsitesRef.document(campsite.id).delete()
        }
        scenario.close()
    }
}