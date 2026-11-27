/**
 * App toplevel composable that handles navigation and contains global values
 */
package com.example.stars

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.SavedState
import com.google.firebase.firestore.ListenerRegistration
import com.example.stars.ui.theme.StarsPalette
import com.example.stars.ui.theme.ThemePresets

object StarsColors {
    // the active theme, changing this recolors the whole app
    var palette: StarsPalette by mutableStateOf(ThemePresets.Moonlight)
    val Background: Color get() = palette.background
    val OnBackground: Color get() = palette.onBackground
    val Surface: Color get() = palette.surface
    val OnSurface: Color get() = palette.onSurface
    val Accent1: Color get() = palette.accent1
    val Accent2: Color get() = palette.accent2
    val Accent3: Color get() = palette.accent3
    val OnAccent: Color get() = palette.onAccent
    val Highlight: Color get() = palette.highlight
    val OnHighlight: Color get() = palette.onHighlight
    val Muted: Color get() = palette.muted
    val Confirm: Color get() = palette.confirm
    val Cancel: Color get() = palette.cancel

    // fixed colors that dont change with the theme
    val Black = Color(0xFF000000)
    val Transparent = Color(0x00FFFFFF)
}

// premade instance for app consistency
val RandomStarsColor = PseudoRandomColor()

// This should be where the current User, and other data shared between screens is stored
object Databases {
    lateinit var campsiteRepository: CampsiteRepository
    lateinit var userRepository: UserRepository
    lateinit var imageRepository: ImageRepository
    lateinit var reservationRepository: ReservationRepository
    lateinit var notificationRepository: NotificationRepository
    lateinit var chatRepository: ChatRepository
    lateinit var lotteryRepository: LotteryRepository
}

object App {
    lateinit var currentUser: UserProfile
    lateinit var navController: ProvidableCompositionLocal<NavController>
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
    var campsiteFilter by remember { mutableStateOf(CampsiteFilter()) }
    var campsiteSort by remember { mutableStateOf(CampsiteSort()) }

    Databases.userRepository = userRepository
    Databases.campsiteRepository = campsiteRepository
    Databases.reservationRepository = reservationRepository
    Databases.imageRepository = imageRepository
    Databases.notificationRepository = notificationRepository
    Databases.chatRepository = chatRepository
    Databases.lotteryRepository = lotteryRepository

    App.currentUser = currentUser
    App.navController = compositionLocalOf { navController }
    CompositionLocalProvider(App.navController provides navController) { }

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
     * "exlpain to me what this does" followed by code
     * Claude sonnet 5.5, 2026-11-24
     * https://claude.ai/share/cb76f5e1-a64f-46e9-9f5b-2c6f793ea551
     */
    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    // create the flow once per device ID, so the Firestore listener isn't re-registered
    val userFlow = remember(deviceId) { userRepository.observeUser(deviceId) }
    val updatedUser by userFlow.collectAsStateWithLifecycle(initialValue = null)

    val user = updatedUser   // local copy, so the smart cast works

    // a loaded profile that is missing its name or email
    val needsProfile = user != null && (user.name.isEmpty() || user.email.isEmpty())
    val navHostReady = currentUser.id.isNotBlank()

    // runs only when Firestore emits a new value, never because currentUser changed
    LaunchedEffect(user) {
        if (user != null) {
            currentUser = user
        }
    }

    // apply the user's saved theme whenever their profile loads or the theme changes
    LaunchedEffect(currentUser.theme) {
        StarsColors.palette = ThemePresets.byName(currentUser.theme)
    }

    // only navigates, never touches currentUser
    LaunchedEffect(needsProfile, navHostReady) {
        if (needsProfile
            && navHostReady
            && navController.currentDestination?.route != "new profile"
        ) {
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
                composable("scan qr code"){QRCodeScannerScreen(navController)}
                composable("campsites") {
                    CampsitesScreen(
                        navController,
                        campsiteRepository,
                        imageRepository,
                        filter = campsiteFilter,
                        sort = campsiteSort,
                        onFilterChange = { newFilter, newSort ->
                            campsiteFilter = newFilter
                            campsiteSort = newSort
                        }
                    )
                }
                composable("waitlists") { PlaceholderScreen("WAITLISTS", navController) }
                composable("reservations") {
                    ReservationCalendarScreen(navController, currentUser, reservationRepository)
                }
                composable("messages") { MessagesScreen(navController, currentUser) }
                composable(
                    route = "messages/{chatId}",
                    arguments = listOf(
                        navArgument("chatId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId").orEmpty()
                    ChatScreen(
                        navController,
                        currentUser,
                        chatId
                    )
                }
                composable("admin messages") { MessagesScreen(navController, currentUser, true) }
                composable(
                    route = "admin messages/{chatId}",
                    arguments = listOf(
                        navArgument("chatId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId").orEmpty()
                    ChatScreen(
                        navController,
                        currentUser,
                        chatId,
                        true
                    )
                }

                composable("app policies") {
                    AppPolicyScreen(navController)
                }

                composable("notifications") { NotificationsScreen(navController, currentUser, notificationRepository, userRepository) }
                composable("settings") {
                    SettingsScreen(
                        navController,
                        currentUser,
                        userRepository
                    )
                }
                composable("themes") {
                    ThemesScreen(navController, currentUser, userRepository)
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
                        imageRepository,
                        currentUser
                    )
                }
                composable("notification log") {
                    AdminViewNotifications(
                        navController,
                        notificationRepository,
                        userRepository
                    )
                }
                composable("owned campsites"){
                    OwnerViewCampsites(navController,
                        campsiteRepository,
                        imageRepository,
                        currentUser
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
                composable from claude question: "I have CampsiteDetailsScreen(navController: NavController,
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

                    DisposableEffect(Unit) {
                        var listener: ListenerRegistration? = null
                        campsiteId?.let {
                            listener = Databases.campsiteRepository.addListenerToId(it) { maybeCamp ->
                                if (maybeCamp == null) {
                                    // doesnt pop u back into the correct place for some reason
                                    // TODO someone who knows more about the nav system can figure this out
                                    navController.popBackStack()
                                } else {
                                    campsite = maybeCamp
                                }
                            }
                        }

                        onDispose { listener?.remove() }
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
                    route = "location/{latitude},{longitude}",
                    arguments = listOf(
                        navArgument("latitude") {
                            type = NavType.FloatType
                        },
                        navArgument("longitude") {
                            type = NavType.FloatType
                        }
                    )
                ) { backStackEntry ->
                    val latitude = backStackEntry.arguments
                        ?.getFloat("latitude")?.toDouble()
                    val longitude = backStackEntry.arguments
                        ?.getFloat("longitude")?.toDouble()

                    LocationScreen(
                        navController = navController,
                        initialLatitude = latitude ?: 0.0,
                        initialLongitude = longitude ?: 0.0
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

                composable(
                    route = "user profile/{userID}",
                    arguments = listOf(
                        navArgument("userID") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val userID = backStackEntry.arguments
                        ?.getString("userID")
                        .orEmpty()

                    UserProfileScreen(
                        navController = navController,
                        currentUser = currentUser,
                        userID = userID,
                        campsiteRepository = campsiteRepository,
                        imageRepository = imageRepository,
                        userRepository = userRepository,
                        reservationRepository = reservationRepository
                    )
                }

                composable(
                    route = "lottery confirm/{lotteryID}",
                    arguments = listOf(
                        navArgument("lotteryID") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val lotteryId = backStackEntry.arguments
                        ?.getString("lotteryID").orEmpty()
                    NotificationLotteryConfirmation(
                        navController = navController,
                        currentUser = currentUser,
                        lotteryId = lotteryId,
                        lotteryRepository = lotteryRepository,
                        reservationRepository = reservationRepository,
                        campsiteRepository = campsiteRepository
                    )
                }

                composable("owner calendar"){
                    OwnerReservationsCalendar(
                        navController,
                        currentUser,
                        reservationRepository,
                        campsiteRepository,
                        userRepository
                    )
                }

                composable("owned reservations list"){
                    OwnerReservationsList(
                        navController,
                        reservationRepository,
                        campsiteRepository,
                        currentUser,
                        userRepository
                    )
                }
            }
        }
    }

}
