package com.example.stars
/**
 * everything to do with owners viewing reservations for their site
*/


import android.widget.Button
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.runBlocking

/**
 * For the owner to see a calendar of reservations. Mostly a copy of ReservationCalendar
 */
@Composable
fun OwnerReservationsCalendar(navController: NavController,
                              currentUser: UserProfile,
                              reservationRepository: ReservationRepository,
                              campsiteRepository: CampsiteRepository,
                              userRepository: UserRepository) {

    var userReservations: List<Reservation> by remember { mutableStateOf(emptyList()) }
    var calendarReady by remember { mutableStateOf(false) }
    var selectedReservations: List<Reservation> by remember { mutableStateOf(emptyList()) }
    var selectedReservation: Reservation? by remember { mutableStateOf(null) }
    var ownedCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }

    // This whole effect is just to get the reservations on campsites the user owns
    // if we don't call it async till readd/restart the listener everytime this recomposes, which is everytime the listener fires...
    DisposableEffect(Unit) {
        // TERRIBLY inefficient but we don't store reservations with their user so :P TODO maybe change that later
        val listener = reservationRepository.addListener {
            calendarReady = false

            // get owned campsites
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

            // get reservations for those sites
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
            action = "Delete Reservation"
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
        )

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


@Composable
fun OwnerReservationsList(navController: NavController,reservationRepository: ReservationRepository,
                          campsiteRepository: CampsiteRepository, currentUser: UserProfile,
                          userRepository: UserRepository){

    var userReservations: List<Reservation> by remember { mutableStateOf(emptyList()) }
    var calendarReady by remember { mutableStateOf(false) }
    var selectedReservations: List<Reservation> by remember { mutableStateOf(emptyList()) }
    var ownedCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var selectedReservation: Reservation? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        // TERRIBLY inefficient but we don't store reservations with their user so :P TODO maybe change that later
        val listener = reservationRepository.addListener {
            calendarReady = false

            // get owned campsites
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

            // get reservations for those sites
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


    var campsites: Map<String, Campsite> by remember { mutableStateOf(emptyMap()) }
    var listReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        campsites = buildMap {
            for (res in userReservations) {
                Databases.campsiteRepository.getWithId(res.campsiteId)?.let {
                    put(res.id, it)
                }
            }
        }
        listReady = true
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
            action = "Delete Reservation"
        )
    }


    Column() {

        ScreenHeader(
            navController = navController,
            title = "RESERVATIONS LIST"
        )

        ReservationList(
            Modifier,
            selectedReservations,
            campsites,
            onClick = {
                selectedReservation = it
                selectedReservations = emptyList()
            }
        )

        Row(){
            Button(onClick = {
                    val tempSelectedList = ArrayList<Reservation>()

                    for (reservation in userReservations){
                        if (reservation.status >= ReservationStatus.CANCELLED_BY_ENTRANT){
                            tempSelectedList.add(reservation)
                        }
                    }

                    selectedReservations = tempSelectedList
                }
            ){
                Text(   Text = "Cancelled",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center, maxLines = 1, autoSize = TextAutoSize.StepBased(
                            minFontSize = 1.sp,
                            maxFontSize = 50.sp,
                            stepSize = 1.sp
                        ),
                    )
            }
        }
    }


}