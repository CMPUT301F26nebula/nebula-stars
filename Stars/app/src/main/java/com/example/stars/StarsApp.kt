package com.example.stars

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Background Color
 */
val starsPurple = Color(0xFF4B0082)
/**
 * Greyed Out Color
 */
val starsDullPurple = Color(0xFF63357E)
val starsBrightPurple = Color(0xFF8900EC)
val starsCyan = Color(0xFF008B8B)
val starsMagenta = Color(0xFFFF1493)
val starsYellow = Color(0xFFEFC700)
val starsWhite = Color(0xFFFFFFFF)
val starsTransparent = Color(0x00FFFFFF)

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
    val campsiteRepository = remember { CampsiteRepository(scope) }
    val userRepository = remember { UserRepository(scope) }
    val imageRepository = remember { ImageRepository(scope) }
    val lotteryRepository = remember { LotteryRepository(scope) }
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
        currentUser = getUserID(navController, context, userRepository)
        if (currentUser.name.isNotEmpty() && currentUser.email.isNotEmpty()) {
            startAt = "home"
        }
        if (currentUser.name.isEmpty() || currentUser.email.isEmpty()) {
            startAt = "new profile"
        }
    }

    if (currentUser.id.isNotBlank()) {
        StarsBackground {
            NavHost(navController = navController, startDestination = startAt) {
                composable("home") { HomeScreen(navController, currentUser) }
                composable("campsites") { CampsitesScreen(navController, campsiteRepository) }
                composable("waitlists") { }
                composable("reservations") {
                    ReservationCalendarScreen(
                        navController,
                        currentUser,
                        reservationRepository
                    )
                }
                composable("messages") { }
                composable("notifications") { NotificationsScreen(navController, currentUser) }
                composable("settings") {
                    SettingsScreen(
                        navController,
                        currentUser,
                        userRepository
                    )
                }
                composable("profile") { EditProfile(navController, currentUser, userRepository) }
                composable("new profile") {
                    MakeNewProfile(
                        navController,
                        currentUser,
                        userRepository
                    )
                }
                composable("upload campsite") {
                    UpdateCampsiteScreen(
                        navController,
                        currentUser,
                        campsiteRepository,
                        imageRepository
                    )
                }
                composable("Users List") {
                    AdminViewUserProfiles(
                        navController,
                        userRepository,
                        campsiteRepository
                    )
                }
                composable(
                    route = "edit campsite {campsiteId}",
                    arguments = listOf(
                        navArgument("campsiteId") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    UpdateCampsiteScreen(
                        navController,
                        currentUser,
                        campsiteRepository,
                        imageRepository,
                        backStackEntry.arguments?.getString("campsiteId").orEmpty()
                    )
                }
                composable("upload lottery") {
                    UpdateLotteryScreen(
                        navController,
                        currentUser,
                        campsiteRepository,
                        lotteryRepository
                    )
                }
                composable(
                    route = "edit lottery {lotteryId}",
                    arguments = listOf(
                        navArgument("lotteryId") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    UpdateLotteryScreen(
                        navController,
                        currentUser,
                        campsiteRepository,
                        lotteryRepository,
                        backStackEntry.arguments?.getString("lotteryId").orEmpty()
                    )
                }

                /*
                composable from claude qeustion: "I have CampsiteDetailsScreen(navController: NavController,
                              currentUser: UserProfile,
                              campsite: Campsite,
                              campsiteRepository: CampsiteRepository,
                              imageRepo: ImageRepository,
                              userRepository: UserRepository,
                              lotteryRepository: LotteryRepository) but campsite needs to be passed by a different function. Show me the code to do that."
                 https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
                 claude sonnet 5.5, 2026-11-5
                 */
                composable(
                    route = "campsite/{campsiteId}",
                    arguments = listOf(navArgument("campsiteId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val campsiteId = backStackEntry.arguments?.getString("campsiteId")

                    var campsite by remember(campsiteId) { mutableStateOf<Campsite?>(null) }

                    LaunchedEffect(campsiteId) {
                        campsite = campsiteId?.let { campsiteRepository.getCampsite(it) }
                    }

                    val loaded = campsite   // local copy, so Kotlin can smart cast it
                    if (loaded != null) {
                        StarsBackground(Modifier, {
                            CampsiteDetailsScreen(
                                navController = navController,
                                currentUser = currentUser,
                                campsite = loaded,
                                campsiteRepository = campsiteRepository,
                                imageRepo = imageRepository,
                                userRepository = userRepository,
                                lotteryRepository = lotteryRepository
                            )
                        }
                        )
                    }
                }
                composable(
                    route = "qr code/{campsiteId}",
                    arguments = listOf(
                        navArgument("campsiteId") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val campsiteId = backStackEntry.arguments
                        ?.getString("campsiteId")
                        .orEmpty()

                    QRCodeScreen(
                        navController = navController,
                        campsiteId = campsiteId
                    )
                }
            }
        }
    }
}
