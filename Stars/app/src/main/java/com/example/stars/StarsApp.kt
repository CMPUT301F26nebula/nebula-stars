package com.example.stars

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.google.firebase.installations.FirebaseInstallations

// This should be where the current User, and other data shared between screens is stored

/**
 * Contains the NavHost
 */
@Composable
fun StarsApp() {
    val navController = rememberNavController()
    val campsiteRepository = remember { CampsiteRepository() }
    val userRepository = remember { UserRepository() }
    val currentUser = remember {getIDOnStart(userRepository)}


    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("campsites") {  CampsitesScreen(navController, campsiteRepository) }
        composable("waitlists") {  }
        composable("reservations") {  }
        composable("messages") {  }
        composable("notifications") {  }
        composable("profile") {  }
        composable("settings") { SettingsScreen(navController, currentUser, userRepository) }
        composable("edit profile" ) {EditProfile(navController, currentUser, userRepository)}
    }
}
