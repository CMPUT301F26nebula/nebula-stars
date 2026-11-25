/**
 * App toplevel composable that handles navigation and contains global values
 */
package com.example.stars

import android.provider.Settings
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object StarsColors {
    val Purple = Color(0xFF4B0082)
    val DullPurple = Color(0xFF800080)
    val BrightPurple = Color(0xFF9400D3)
    val Cyan = Color(0xFF008B8B)
    val Magenta = Color(0xFFFF1493)
    val Yellow = Color(0xFFEFC700)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val Transparent = Color(0x00FFFFFF)
}

// This should be where the current User, and other data shared between screens is stored
object Databases {
    lateinit var campsiteRepository: CampsiteRepository
    lateinit var userRepository: UserRepository
    lateinit var imageRepository: ImageRepository
    lateinit var reservationRepository: ReservationRepository
    lateinit var notificationRepository: NotificationRepository
    lateinit var chatRepository: ChatRepository
}

/**
 * Contains the NavHost
 * @param notifs true if we have permission to send push notifications
 */
@Composable
fun StarsApp(notifs: Boolean = false) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val campsiteRepository = remember { CampsiteRepository(scope) }
    val userRepository = remember { UserRepository(scope) }
    val imageRepository = remember { ImageRepository(scope) }
    val lotteryRepository = remember { LotteryRepository(scope) }
    val reservationRepository = remember { ReservationRepository(scope) }
    val notificationRepository = remember { NotificationRepository(scope) }
    val chatRepository = remember { ChatRepository(scope) }
    var currentUser by remember { mutableStateOf(UserProfile()) }
    var startAt by remember { mutableStateOf("home") }

    Databases.userRepository = userRepository
    Databases.campsiteRepository = campsiteRepository
    Databases.reservationRepository = reservationRepository
    Databases.imageRepository = imageRepository
    Databases.notificationRepository = notificationRepository
    Databases.chatRepository = chatRepository

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        currentUser = getUserID(navController, context, userRepository, notifs)
        if (currentUser.name.isNotEmpty() && currentUser.email.isNotEmpty()) {
            startAt = "home"
        }
        if (currentUser.name.isEmpty() || currentUser.email.isEmpty()) {
            startAt = "new profile"
        }
    }

    /**
     * to end of launchEffect generated bty claude conversation starting at:
     * "write me the code to make a listener for changes in user profile"
     * Claude sonnet 5.5, 2026-11-21
     * https://claude.ai/share/c67ca8f5-33e2-4715-a58d-36c88dbbfe32
     */
    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    // create the flow once per device ID, so the Firestore listener isn't re-registered
    val userFlow = remember(deviceId) { userRepository.observeUser(deviceId) }
    val updatedUser by userFlow.collectAsStateWithLifecycle(initialValue = null)

    val user = updatedUser   // local copy, so the smart cast works

    // the one fact we care about: a loaded profile that is missing its name or email
    val needsProfile = user != null && (user.name.isEmpty() || user.email.isEmpty())
    val navHostReady = currentUser.id.isNotBlank()
    LaunchedEffect(needsProfile, navHostReady) {
        if (needsProfile && navController.currentDestination?.route != "new profile" && navHostReady) {
            currentUser = user
            navController.navigate("new profile") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (currentUser.id.isNotBlank()) {
        StarsBackground {
            NavHost(navController = navController, startDestination = startAt) {
                composable("home") { HomeScreen(navController, currentUser) }
                composable("campsites") { CampsitesScreen(navController, campsiteRepository, imageRepository) }
                composable("waitlists") { PlaceholderScreen("WAITLISTS", navController) }
                composable("reservations") {
                    ReservationCalendarScreen(navController, currentUser, reservationRepository)
                }
                composable("messages") { PlaceholderScreen("MESSAGES", navController) }
                composable("notifications") { NotificationsScreen(navController, currentUser, notificationRepository) }
                composable("settings") {
                    SettingsScreen(
                        navController,
                        currentUser,
                        userRepository
                    )
                }
                composable("profile") { EditProfile(navController, currentUser, userRepository, imageRepository) }
                composable("new profile") {
                    MakeNewProfile(
                        navController,
                        currentUser,
                        userRepository,
                        imageRepository
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
                composable("users list") {
                    AdminViewUserProfiles(
                        navController,
                        userRepository,
                        campsiteRepository
                    )
                }
                composable("notification log") {
                    AdminViewNotifications(
                        navController,
                        notificationRepository
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
                composable("admin images"){ AdminViewPictures(navController, userRepository, campsiteRepository,imageRepository) }


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
                        campsiteId?.let {
                            Databases.campsiteRepository.addListenerToId(it) { maybeCamp ->
                                if (maybeCamp == null) {
                                    // doesnt pop u back into the correct place for some reason
                                    // TODO someone who knows more about the nav system can figure this out
                                    navController.popBackStack()
                                } else {
                                    campsite = maybeCamp
                                }
                            }
                        }
                    }

                    if (campsite != null) {
                        StarsBackground(Modifier, {
                            CampsiteDetailsScreen(
                                navController = navController,
                                currentUser = currentUser,
                                campsite = campsite ?: Campsite(),
                                campsiteRepository = campsiteRepository,
                                imageRepo = imageRepository,
                                userRepository = userRepository,
                                lotteryRepository = lotteryRepository,
                                reservationRepository = reservationRepository
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

                composable(
                    route = "entrants/{lotteryID}",
                    arguments = listOf(
                        navArgument("lotteryID"){
                            type = NavType.StringType
                        }
                    )
                ){ backStackEntry ->
                    val lotteryID = backStackEntry.arguments
                        ?.getString("lotteryID")
                        .orEmpty()

                    OwnerViewUserProfiles(
                        navController = navController,
                        lotteryID = lotteryID,
                        userRepository = userRepository,
                        lotteryRepository = lotteryRepository
                    )
                }
            }
        }
    }

}
