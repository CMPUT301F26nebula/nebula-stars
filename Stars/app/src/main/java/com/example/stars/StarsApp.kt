package com.example.stars

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable

// This should be where the current User, and other data shared between screens is stored

/**
 * Contains the NavController
 */
@Composable
fun StarsApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("campsites") {  CampsitesScreen(navController) }
        composable("waitlists") {  }
        composable("reservations") {  }
        composable("messages") {  }
        composable("notifications") {  }
        composable("profile") {  }
        composable("settings") {  }
    }
}
