package com.example.stars

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsMenuTests {

    private lateinit var userRepo: UserRepository

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity

    /**
     * Configures firebase repositories with their items
     */

    @Before
    fun setUp() {
        userRepo = UserRepository()
    }

    @Test
    fun editUserCorrectly(){
        composeTestRule.onNodeWithTag("settingsButton").performClick()
        composeTestRule.onNodeWithTag("editProfile").performClick()


    }
}