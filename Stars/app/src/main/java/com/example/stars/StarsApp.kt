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
    var currentUser: UserProfile? = null

    /**
     * taken from the firebase website https://firebase.google.com/docs/projects/manage-installations?utm_source=google&utm_medium=cpc&utm_campaign=Cloud-SS-DR-Firebase-FY26-global-gsem-1713590&utm_content=text-ad&utm_term=KW_firebase&gad_source=1&gad_campaignid=23417478209&gbraid=0AAAAADpUDOi4qBEUiNLxYdYJU3glALvaT&gclid=CjwKCAjwuobXBhBXEiwAYUaIldXIUqO1-_xSoHG19ncJXyuhYvLieWuGTE500Fjld2hxvPmAeiOnQxoCoRMQAvD_BwE#kotlin
     * no specified author Last updated 2026-10-28 UTC
     */
    // this gets the device ID from firebase
    FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("Installations", "Installation ID: " + task.result)

            // check if the user ID already exists
            userRepository.users.forEach { user ->
                if (user.id == task.result) {
                    currentUser = user
                }
            }

            // if currentUser was never assigned then it's a new account so create a new user
            // and add it to the user repo list
            if(currentUser == null){
                currentUser = UserProfile(id = task.result)
                userRepository.addOrUpdateUser(currentUser)
            }


        } else {
            Log.e("Installations", "Unable to get Installation ID")
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("campsites") {  CampsitesScreen(navController, campsiteRepository) }
        composable("waitlists") {  }
        composable("reservations") {  }
        composable("messages") {  }
        composable("notifications") {  }
        composable("profile") {  }
        composable("settings") { SettingsScreen(navController, currentUser) }
        composable("edit profile" ) {EditProfile(navController, currentUser)}
    }
}
