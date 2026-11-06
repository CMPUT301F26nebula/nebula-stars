package com.example.stars

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * A class for testing all the profile settings and other settings
 * TODO: create settings tests when settings exist
 */
class SettingsMenuTests {

    @get:Rule
    val composeTestRule = createComposeRule() // access MainActivity
    lateinit var navController: TestNavHostController


    /*
    create a good new profile without a phone number
     */
    @Test
    fun createNewProfileWithoutPhoneNumber() = runTest {
        val userRepo = UserRepository(this)

        // create ID
        val user = UserProfile("Unit Test")

        /*
        created by claude with earlier version of the function and: "You must call setGraph() before calling getGraph()"
        "it is required in the code for MakeNewProfile. how do I fix this in the test?", and "write the recomended code"
        https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
        claude sonnet 5.5, 2026-11-02
         */
        composeTestRule.setContent {
            val context = LocalContext.current
            navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            NavHost(navController, startDestination = "new profile") {
                composable("new profile") { MakeNewProfile(navController, user, userRepo) }
                composable("home") { }
            }
        }

        // input good info
        composeTestRule.onNodeWithTag("Name Field").performTextInput("Unit Test")
        composeTestRule.onNodeWithTag("Email Field").performTextInput("Unit@test.test")
        composeTestRule.onNodeWithTag("Confirm").performClick()

        // ensure creation
        val fireUser = userRepo.getUser(user.id)
        assertEquals(user, fireUser)

        // delete user
        userRepo.deleteUser(user)
        val deleted = userRepo.getUser(user.id)
        assertNull(deleted)
    }

    /*
    Assert can't create with blank name or email, create new user with information that is incorrect, assert can't make profile,
    change to correct info, confirm creation, delete profile
     */
    @Test
    fun createNewProfileWithBadPhoneAndEmail() = runTest {
        val userRepo = UserRepository(this)
        val user = UserProfile("Unit Test")

        /*
        created by claude with earlier version of the function and: "You must call setGraph() before calling getGraph()"
        "it is required in the code for MakeNewProfile. how do I fix this in the test?", and "write the recomended code"
        https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
        claude sonnet 5.5, 2026-11-02
         */
        composeTestRule.setContent {
            val context = LocalContext.current
            navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            NavHost(navController, startDestination = "new profile") {
                composable("new profile") { MakeNewProfile(navController, user, userRepo) }
                composable("home") { }
            }
        }

        // ensure you can't make an incomplete profile
        composeTestRule.onNodeWithTag("Confirm").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("Name Field").performTextInput("Unit Test")
        composeTestRule.onNodeWithTag("Confirm").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("Name Field").performTextClearance()
        composeTestRule.onNodeWithTag("Email Field").performTextInput("Unit@test.test")
        composeTestRule.onNodeWithTag("Confirm").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("Email Field").performTextClearance()

        //enter wrong stuff
        composeTestRule.onNodeWithTag("Name Field").performTextInput("Unit Test")
        composeTestRule.onNodeWithTag("Email Field").performTextInput("ad!qr14F>,1902<.{} %{}@9828320.com")
        composeTestRule.onNodeWithTag("Phone Field").performTextInput("ad!qr14F>,1902<.{} %{} 9828320")
        composeTestRule.onNodeWithTag("Confirm").assertIsNotEnabled()

        // clear and enter good email
        composeTestRule.onNodeWithTag("Email Field").performTextClearance()
        composeTestRule.onNodeWithTag("Email Field").performTextInput("Unit@test.test")
        composeTestRule.onNodeWithTag("Confirm").performClick()


        // confirm creation
        val fireUser = userRepo.getUser(user.id)
        assertEquals("1419029828", fireUser?.phone)
        assertEquals("Unit@test.test", fireUser?.email)

        // delete
        userRepo.deleteUser(user)
        val deleted = userRepo.getUser(user.id)
        assertNull(deleted)
    }

    /*
    have user, try edits, assert correct changes
     */
    @Test
    fun editProfileCorrectlyAndDelete() = runTest {
        val userRepo = UserRepository(this)
        val user = UserProfile("Unit Test", "Unit Test", "Unit@test.test")

        userRepo.addOrUpdateUser(user)
        /*
       created by claude with earlier version of the function and: "You must call setGraph() before calling getGraph()"
       "it is required in the code for MakeNewProfile. how do I fix this in the test?", and "write the recomended code"
       https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
       claude sonnet 5.5, 2026-11-02
        */
        composeTestRule.setContent {
            val context = LocalContext.current
            navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            NavHost(navController, startDestination = "new profile") {
                composable("new profile") { EditProfile(navController, user, userRepo) }
                composable("home") { }
            }
        }

        composeTestRule.onNodeWithTag("Edit Button").performClick()
        composeTestRule.onNodeWithTag("Name Field").performTextInput("Addition ")
        composeTestRule.onNodeWithTag("Email Field").performTextClearance()
        composeTestRule.onNodeWithTag("Email Field").performTextInput("t@t.ca")
        composeTestRule.onNodeWithTag("Phone Field").performTextInput("9999999999")
        composeTestRule.onNodeWithTag("Save Edits").performClick()

        val fireUser = userRepo.getUser(user.id)
        assertEquals("Addition Unit Test", fireUser?.name)
        assertEquals("t@t.ca", fireUser?.email)
        assertEquals("9999999999", fireUser?.phone)

        val deleted = run {
            userRepo.deleteUser(user)
            userRepo.getUser("Unit Test")
        }

        assertNull(deleted)
    }

    /*
    have user, try impossible edits, fail, cancel edits, delete user
     */
    @Test
    fun failEditAndDeleteProfile() = runTest {
        val userRepo = UserRepository(this)
        // set up the user
        val user = UserProfile("Unit Test", "Unit Test", "Unit@test.test", phone = "1111111111",
            listOf(),false,UserPermission.ADMIN)

        // add the user to the repo
        userRepo.addOrUpdateUser(user)

        /*
       created by claude with earlier version of the function and: "You must call setGraph() before calling getGraph()"
       "it is required in the code for MakeNewProfile. how do I fix this in the test?", and "write the recomended code"
       https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
       claude sonnet 5.5, 2026-11-02
        */
        composeTestRule.setContent {
            val context = LocalContext.current
            navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            NavHost(navController, startDestination = "new profile") {
                composable("new profile") { EditProfile(navController, user, userRepo) }
                composable("home") { }
            }
        }

        // try edits with impossible email
        composeTestRule.onNodeWithTag("Edit Button").performClick()
        composeTestRule.onNodeWithTag("Name Field").performTextInput("Addition ")
        composeTestRule.onNodeWithTag("Email Field").performTextInput("@ca")
        composeTestRule.onNodeWithTag("Phone Field").performTextInput("9999999999")
        composeTestRule.onNodeWithTag("Save Edits").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("Cancel Edits").performClick()

        // check that the current user, FireBaseUser and original user are still the same
        val fireUser = userRepo.getUser(user.id)
        assertEquals(fireUser, user)
        assertEquals("Unit Test",user.name)
        assertEquals("Unit@test.test",user.email)
        assertEquals("1111111111", user.phone)

        // cancel deletion and then actually delete
        composeTestRule.onNodeWithTag("Delete Button").performClick()
        composeTestRule.onNodeWithTag("Cancel").performClick()
        composeTestRule.onNodeWithTag("Delete Button").performClick()
        composeTestRule.onNodeWithTag("Confirm").performClick()

        // ensure that the user was deleted
        composeTestRule.waitUntil { runBlocking {userRepo.getUser(user.id)} == null } // wait for firebase
        composeTestRule.waitUntil{ user.name == "" } // wait for deleteUser
        assertEquals("",user.name)
        assertEquals("",user.email)
        assertEquals("", user.phone)
        assertEquals(UserPermission.ENTRANT, user.permission)
    }

    /*
    testing to see if you can get and remove owner status
     */
    @Test
    fun getOwnerStatusRemoveOwnerStatus() = runTest {
        val userRepo = UserRepository(this)

        // set up the user
        val user = UserProfile("Unit Test", "Unit Test", "Unit@test.test", phone = "1111111111",
            listOf(), false,UserPermission.ENTRANT)

        // add the user to the repo
        userRepo.addOrUpdateUser(user)

        /*
       created by claude with earlier version of the function and: "You must call setGraph() before calling getGraph()"
       "it is required in the code for MakeNewProfile. how do I fix this in the test?", and "write the recomended code"
       https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
       claude sonnet 5.5, 2026-11-02
        */
        composeTestRule.setContent {
            val context = LocalContext.current
            navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            NavHost(navController, startDestination = "new profile") {
                composable("new profile") { SettingsScreen(navController, user, userRepo) }
                composable("home") { }
            }
        }

        // hit the become owner buttons
        composeTestRule.onNodeWithTag("Become Owner").performClick()
        composeTestRule.onNodeWithTag("Confirm Become Owner").performClick()

        // ensure local and remote owner status
        assertEquals(UserPermission.OWNER, user.permission)
        val fireOwner = userRepo.getUser(user.id)
        assertEquals(UserPermission.OWNER, fireOwner?.permission)

        // hit the remove owner status buttons
        composeTestRule.onNodeWithTag("Remove Owner").performClick()
        composeTestRule.onNodeWithTag("Confirm").performClick()

        // ensure local and remote entrant status
        assertEquals(UserPermission.ENTRANT, user.permission)
        val fireUser = userRepo.getUser(user.id)
        assertEquals(UserPermission.ENTRANT, fireUser?.permission)

        // delete
        userRepo.deleteUser(user)
        composeTestRule.waitUntil { runBlocking {userRepo.getUser(user.id)} == null } // wait for firebase


    }
}