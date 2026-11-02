package com.example.stars

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// This should be where the current User, and other data shared between screens is stored

/**
 * Contains the NavHost
 */
@Composable
fun StarsApp() {
    val navController = rememberNavController()
    val campsiteRepository = remember { CampsiteRepository() }
    val userRepository = remember { UserRepository() }
    val imageRepository = remember { ImageRepository() }
    var currentUser by remember { mutableStateOf(UserProfile()) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        currentUser = getUserID(navController, context)
    }

    if (currentUser.id.isNotBlank()) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { HomeScreen(navController, currentUser) }
            composable("campsites") { CampsitesScreen(navController, campsiteRepository) }
            composable("waitlists") { }
            composable("reservations") { }
            composable("messages") { }
            composable("notifications") { }
            composable("profile") { }
            composable("settings") { SettingsScreen(navController, currentUser, userRepository) }
            composable("edit profile") { EditProfile(navController, currentUser, userRepository) }
            composable("upload campsite") { UpdateCampsiteScreen(navController, currentUser, campsiteRepository, imageRepository) }
            composable(
                route = "edit campsite {campsiteId}",
                arguments = listOf(
                    navArgument("campsiteId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry -> UpdateCampsiteScreen(navController, currentUser, campsiteRepository, imageRepository,backStackEntry.arguments?.getString("userId").orEmpty()) }
        }
    }
}
