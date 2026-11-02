package com.example.stars

import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.testing.TestNavHostController
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsMenuTests {

    private lateinit var userRepo: UserRepository

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity
    lateinit var navController: TestNavHostController
    /**
     * Configures firebase repositories with their items
     */

    @Before
    fun setUp() {
        userRepo = UserRepository()
    }

    @Composable
    @Test
    fun createNewProfileWithoutPhoneNumber(){
        val user = UserProfile("Unit Test")
        MakeNewProfile(navController, user, userRepo)
        composeTestRule.onNodeWithTag("Name Field").performTextInput("Unit Test")
        composeTestRule.onNodeWithTag("Email Field").performTextInput("Unit@test.test")
        composeTestRule.onNodeWithTag("Confirm Profile").performClick()
        assertEquals(UserProfile("Unit Test","Unit Test","Unit@test.test"),user)


    }
    @Test
    fun editUserCorrectly(){
        composeTestRule.onNodeWithTag("settingsButton").performClick()
        composeTestRule.onNodeWithTag("editProfile").performClick()

    }
}