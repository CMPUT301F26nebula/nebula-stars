package com.example.stars

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// This should be where the current User, and other data shared between screens is stored
object Databases {
    lateinit var campsiteRepository: CampsiteRepository
    lateinit var userRepository: UserRepository
    lateinit var imageRepository: ImageRepository
    lateinit var reservationRepository: ReservationRepository
}

/**
 * Contains the NavHost
 */
@Composable
fun StarsApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val campsiteRepository = remember { CampsiteRepository() }
    val userRepository = remember { UserRepository() }
    val imageRepository = remember { ImageRepository() }
    val reservationRepository = remember { ReservationRepository(scope) }
    var currentUser by remember { mutableStateOf(UserProfile()) }
    var startAt by remember { mutableStateOf("home") }

    // TODO if u dont like it feel free to refactor these out after we make everything
    // TODO honestly navController and currentUser would also be handy singletons
    Databases.userRepository = userRepository
    Databases.campsiteRepository = campsiteRepository
    Databases.reservationRepository = reservationRepository
    Databases.imageRepository = imageRepository

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        currentUser = getUserID(navController, context)
        if (currentUser.name.isNotEmpty() && currentUser.email.isNotEmpty()){
            startAt = "home"
        }
        if (currentUser.name.isEmpty() || currentUser.email.isEmpty()){
            startAt = "new profile"
        }
    }

    if (currentUser.id.isNotBlank()){
        NavHost(navController = navController, startDestination = startAt) {
            composable("home") { HomeScreen(navController, currentUser) }
            composable("campsites") { CampsitesScreen(navController, campsiteRepository) }
            composable("waitlists") { }
            composable("reservations") { ReservationCalendarScreen(navController, currentUser, reservationRepository) }
            composable("messages") { }
            composable("notifications") { }
            composable("settings") { }
            composable("profile") { StarsBackground(Modifier,{ EditProfile(navController, currentUser, userRepository) })}
            composable("new profile") { StarsBackground(Modifier,{ MakeNewProfile(navController, currentUser, userRepository) } ) }
            composable("upload campsite") { UpdateCampsiteScreen(navController, currentUser, campsiteRepository, imageRepository, scope) }
            composable(
                route = "edit campsite {campsiteId}",
                arguments = listOf(
                    navArgument("campsiteId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry -> UpdateCampsiteScreen(navController, currentUser, campsiteRepository, imageRepository,scope,backStackEntry.arguments?.getString("userId").orEmpty()) }
        }
    }
}
