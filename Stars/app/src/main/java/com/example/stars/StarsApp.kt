package com.example.stars

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// This should be where the current User, and other data shared between screens is stored

/**
 * Contains the NavHost
 */
@Composable
fun StarsApp() {
    val navController = rememberNavController()
    val campsiteRepository = remember { CampsiteRepository() }
    val userRepository = remember { UserRepository() }
    val currentUser =  getUserID(navController, userRepository, LocalContext.current)


    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("campsites") {  CampsitesScreen(navController, campsiteRepository) }
        composable("waitlists") {  }
        composable("reservations") {  }
        composable("messages") {  }
        composable("notifications") {  }
        composable("profile") {  }
        composable("settings") { SettingsScreen(navController, currentUser, userRepository) }
        composable("edit profile") {EditProfile(navController, currentUser, userRepository)}
    }
}
