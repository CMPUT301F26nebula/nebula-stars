package com.example.stars
/**
 * everything to do with owners viewing reservations for their site
*/


import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stars.Reservation
import kotlinx.coroutines.runBlocking

@Composable
fun OwnerResrvationsCalander(navController: NavController,
                             currentUser: UserProfile,
                             reservationRepository: ReservationRepository,
                             campsiteRepository: CampsiteRepository,
                             userRepository: UserRepository) {

    var userReservations: List<Reservation> by remember { mutableStateOf(emptyList()) }
    var calendarReady by remember { mutableStateOf(false) }
    var selectedReservations: List<Reservation> by remember { mutableStateOf(emptyList()) }
    var selectedReservation: Reservation? by remember { mutableStateOf(null) }
    var ownedCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }

    // if we dont call it async till readd/restart the listener everytime this recomposes, which is everytime the listener fires...
    DisposableEffect(Unit) {
        // TERRIBLY inefficient but we don't store reservations with their user so :P TODO maybe change that later
        val listener = reservationRepository.addListener {
            calendarReady = false

            val campsites = runBlocking { campsiteRepository.getAll() }
            val myCampsitesTempString = ArrayList<String>()
            val myCampsitesTempCampsite = ArrayList<Campsite>()
            for (campsite in campsites){
                if (campsite.ownerId == currentUser.id){
                    myCampsitesTempString.add(campsite.id)
                    myCampsitesTempCampsite.add(campsite)
                }
            }

            ownedCampsites = myCampsitesTempCampsite

            userReservations = buildList {
                for (reservation in it) {
                    if (reservation != null && reservation.campsiteId in myCampsitesTempString) {
                        add(reservation)
                    }
                }
            }
            calendarReady = true
        }

        // should probably unregister the listener after we exit this screen
        onDispose { listener.remove() }
    }

    // TODO remove at some point
    var debugDialogState by remember { mutableStateOf(false) }
    if (debugDialogState) {
        DebugReservationCreator(
            onClose = {
                debugDialogState = false
            },
            onConfirm = {
                val id = Reservation.uniqueIdFrom(currentUser.id, "07359064-6222-427e-a491-a3e66e731fb3", it)
                reservationRepository.addOrUpdate(Reservation(
                    id = id,
                    timeRange = it,
                    status = ReservationStatus.INVITED,
                    entrantId = currentUser.id,
                    campsiteId = "07359064-6222-427e-a491-a3e66e731fb3"
                ))
            }
        )
    }

    // if reservations overlap then the user should be able to choose which one they wanna look at
    if (selectedReservations.size > 1) {
        ReservationListModal(
            selectedReservations,
            onClose = {
                selectedReservations = emptyList()
            },
            onClick = {
                selectedReservation = it
                selectedReservations = emptyList()
            }
        )
    } else if (selectedReservations.size == 1) {
        selectedReservation = selectedReservations.first()
        selectedReservations = emptyList()
    }

    // reservation details
    if (selectedReservation != null) {
        ReservationDetailsModal(
            navController,
            selectedReservation!!,
            onClose = {
                selectedReservation = null
            },
            onClickOtherPerson = {user, owner -> navController.navigate("user profile/${user}")},
            otherPersonText = {user, owner ->

                "User Booked: ${runBlocking { userRepository.getWithId(user)?.name} }"
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        ScreenHeader(
            navController = navController,
            title = "RESERVATIONS"
        ) {
            Text(
                text = "+ ",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StarsColors.Purple,
                // TODO debug, remove later
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        debugDialogState = true
                    }
                )
            )
        }

        Spacer(Modifier.weight(1.0f))

        // the thingy doesnt like to recompose properly after the reservations are found so loading screen it is :P
        if (calendarReady) {
            ReservationCalendar(userReservations) {
                selectedReservations = it
            }
        } else {
            LoadingText()
        }

        Spacer(Modifier.weight(1.0f))
    }
}