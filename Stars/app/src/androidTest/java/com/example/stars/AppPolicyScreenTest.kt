
package com.example.stars

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import org.junit.Rule
import org.junit.Test

class AppPolicyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun policyScreenOpensAndReturnsToSettings() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }

            val user = remember { UserProfile("Policy Test") }
            val userRepository = remember {
                UserRepository(scope)
            }

            NavHost(
                navController = navController,
                startDestination = "settings"
            ) {
                composable("settings") {
                    SettingsScreen(
                        navController,
                        user,
                        userRepository
                    )
                }

                composable("app policies") {
                    AppPolicyScreen(navController)
                }
            }
        }

        composeTestRule
            .onNodeWithTag("appPolicyButton")
            .assertExists()

        composeTestRule
            .onNodeWithTag("appPolicyButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("policyContent")
            .assertExists()

        composeTestRule
            .onNodeWithTag("policyBackButton")
            .assertExists()

        composeTestRule
            .onNodeWithTag("policyBackButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("appPolicyButton")
            .assertExists()
    }
}
