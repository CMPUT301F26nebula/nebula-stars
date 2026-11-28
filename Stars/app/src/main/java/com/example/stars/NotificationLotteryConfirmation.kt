package com.example.stars

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/**
 * Prompts the user with a calendar, campsite selector (if applicable), and accept/decline
 *
 * On accept:
 * moves user from pending winners to confirmed IDs
 * creates a reservation for selected campsite and date range
 * reservation status is [ReservationStatus.ACCEPTED]
 * TODO: optionally sends owner notif?
 *
 * On decline:
 * removes user from pending winners
 * removes user from entrant IDs
 * NOTE: does not make a cancelled reservation since no reservation existed to be cancelled
 * NOTE: does not send a confirmation notification of cancellation
 */
@OptIn(ExperimentalGridApi::class)
@Composable
fun NotificationLotteryConfirmation(
    navController: NavController,
    currentUser: UserProfile,
    lotteryId: String,
    lotteryRepository: LotteryRepository,
    reservationRepository: ReservationRepository,
    campsiteRepository: CampsiteRepository,
    notificationRepository: NotificationRepository) {

    var decline by remember { mutableStateOf(false) }
    var accept by remember { mutableStateOf(false) }

    //var startDate by remember { mutableStateOf(GregorianCalendar.getInstance()) }
    var startDate by remember { mutableLongStateOf(0L) }
    //var endDate by remember { mutableStateOf(GregorianCalendar.getInstance()) }
    var endDate by remember { mutableLongStateOf(0L) }

    var lottery by remember { mutableStateOf(Lottery()) }
    val campsites = remember { mutableListOf<Campsite>() }
    var selectedCampsiteIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        lottery = lotteryRepository.getWithId(lotteryId) ?: Lottery()

        lottery.campsiteIDs.forEach {
            campsiteRepository.getWithId(it)?.let { element -> campsites.add(element) }
        }

        selectedCampsiteIndex = 0
        Log.d("Lottery Confirmation Test", "IDs found: ${lottery.campsiteIDs.size}")
        Log.d("Lottery Confirmation Test", "Campsites found: ${campsites.size}")
        //startDate.timeInMillis = Long.MAX_VALUE
        //endDate.timeInMillis = 0
    }

    val dateRangePickerState = rememberDateRangePickerState(selectableDates = object : SelectableDates{
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            //Seems to work? idk if there's time conversion weirdness and I haven't noticed
            return utcTimeMillis < lottery.timePeriod.end && utcTimeMillis > lottery.timePeriod.start
        }
    })

    LaunchedEffect(
        dateRangePickerState.selectedStartDateMillis,
        dateRangePickerState.selectedEndDateMillis
    ) {
        Log.d("Lottery Confirmation Test", "State Changed")
        startDate = dateRangePickerState.selectedStartDateMillis ?: 0L
        endDate = dateRangePickerState.selectedEndDateMillis ?: 0L
    }

    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(Modifier, navController, "Confirm Reservation")

        if (lottery.pendingWinnerIDs.contains(currentUser.id)) {
            // Campsite Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (campsites.size > 1) {
                    Button(
                        modifier = Modifier.border(
                            1.dp,
                            StarsColors.OnBackground,
                            RoundedCornerShape(24.dp)
                        ),
                        onClick = {
                            selectedCampsiteIndex -= 1
                            if (selectedCampsiteIndex < 0) {
                                selectedCampsiteIndex = campsites.size - 1
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarsColors.Transparent,
                            contentColor = StarsColors.OnBackground
                        )
                    ) {
                        Text(
                            text = "<",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    campsites.getOrNull(selectedCampsiteIndex)?.title ?: "No Campsites Found",
                    modifier = Modifier.fillMaxWidth().weight(1.0f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    color = StarsColors.OnBackground
                )

                if (campsites.size > 1) {
                    Button(
                        modifier = Modifier.border(
                            1.dp,
                            StarsColors.OnBackground,
                            RoundedCornerShape(24.dp)
                        ),
                        onClick = {
                            selectedCampsiteIndex += 1
                            selectedCampsiteIndex %= campsites.size
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarsColors.Transparent,
                            contentColor = StarsColors.OnBackground
                        )
                    ) {
                        Text(
                            text = ">",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(Modifier
                .fillMaxSize()
                .padding(30.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(StarsColors.Surface)
                .weight(1.0f)
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    dateFormatter = DatePickerDefaults.dateFormatter(selectedDateSkeleton = "MMMd"),
                    colors = DatePickerDefaults.colors(
                        containerColor = StarsColors.OnSurface,
                        titleContentColor = StarsColors.OnBackground,
                        headlineContentColor = StarsColors.OnBackground,
                        weekdayContentColor = StarsColors.OnBackground,
                        subheadContentColor = StarsColors.OnBackground,
                        navigationContentColor = StarsColors.OnBackground,
                        dayContentColor = StarsColors.Highlight,
                        disabledDayContentColor = StarsColors.Muted,
                        selectedDayContentColor = StarsColors.OnAccent,
                        selectedDayContainerColor = StarsColors.Accent1,
                        todayContentColor = StarsColors.Highlight,
                        todayDateBorderColor = StarsColors.Accent3,
                        dayInSelectionRangeContainerColor = StarsColors.Transparent,
                        dayInSelectionRangeContentColor = StarsColors.OnBackground,
                        dividerColor = StarsColors.OnBackground,
                        dateTextFieldColors = TextFieldDefaults.colors(
                            focusedTextColor = StarsColors.OnBackground,
                            unfocusedTextColor = StarsColors.OnBackground,
                            errorTextColor = StarsColors.OnBackground,
                            focusedContainerColor = StarsColors.OnSurface,
                            unfocusedContainerColor = StarsColors.OnSurface,
                            errorContainerColor = StarsColors.OnSurface,
                            // A label is the text above it when something is typed
                            focusedLabelColor = StarsColors.OnBackground,
                            unfocusedLabelColor = StarsColors.Muted,
                            // An indicator is the outline color
                            focusedIndicatorColor = StarsColors.OnBackground,
                            unfocusedIndicatorColor = StarsColors.Muted,
                            cursorColor = StarsColors.OnBackground
                        )
                    ),
                    title = { Text(
                        "Select Date",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    ) }
                )
            }

            // Accept/Decline prompt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (!decline && !accept) {
                    Button(
                        onClick = {
                            accept = true
                        },
                        //enabled = (startDate.timeInMillis > 0 && endDate.timeInMillis > 0),
                        enabled = (startDate > 0 && endDate > 0),
                        shape = RoundedCornerShape(
                            15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarsColors.Confirm,
                            contentColor = StarsColors.OnAccent,
                            disabledContainerColor = StarsColors.Muted,
                            disabledContentColor = StarsColors.OnAccent,
                        ),
                        border = BorderStroke(2.dp, StarsColors.OnBackground)
                    ) {
                        Text("Accept")
                    }

                    Button(
                        onClick = {
                            decline = true
                        },
                        shape = RoundedCornerShape(
                            15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarsColors.Cancel,
                            contentColor = StarsColors.OnAccent
                        ),
                        border = BorderStroke(2.dp, StarsColors.OnBackground)
                    ) {
                        Text("Decline")
                    }
                } else {
                    Button(
                        onClick = {
                            if (accept) {
                                val reservationId = Reservation.uniqueIdFrom(
                                    currentUser.id,
                                    campsites[selectedCampsiteIndex].id,
                                    EpochRange(startDate, endDate)
                                )

                                lotteryRepository.addOrUpdate(
                                    Lottery(
                                        id = lottery.id,
                                        ownerIDs = lottery.ownerIDs,
                                        campsiteIDs = lottery.campsiteIDs,
                                        timePeriod = lottery.timePeriod,
                                        deadline = lottery.deadline,
                                        numWinners = lottery.numWinners,
                                        entrantIDs = lottery.entrantIDs,
                                        waitlistCapacity = lottery.waitlistCapacity,
                                        pendingWinnerIDs = lottery.pendingWinnerIDs.filter { it != currentUser.id },
                                        confirmedIDs = (lottery.confirmedIDs + currentUser.id)
                                    )
                                )

                                reservationRepository.addOrUpdate(
                                    Reservation(
                                        reservationId,
                                        EpochRange(startDate, endDate),
                                        ReservationStatus.ACCEPTED,
                                        currentUser.id,
                                        campsites[selectedCampsiteIndex].id
                                    )
                                )

                                // Send reservation csv
                                notificationRepository.sendNotification(
                                    Notification(
                                        UUID.randomUUID().toString(),
                                        userId = currentUser.id,
                                        content = "Reservation made for ${campsites[selectedCampsiteIndex].title} " +
                                                "from ${SimpleDateFormat.getDateInstance().format(startDate)} " +
                                                "to ${SimpleDateFormat.getDateInstance().format(endDate)}",
                                        navDestination = "reservation csv/${Uri.encode(reservationId)}",
                                        senderIds = lottery.ownerIDs,
                                    )
                                )

                            } else if (decline) {
                                lotteryRepository.addOrUpdate(
                                    Lottery(
                                        id = lottery.id,
                                        ownerIDs = lottery.ownerIDs,
                                        campsiteIDs = lottery.campsiteIDs,
                                        timePeriod = lottery.timePeriod,
                                        deadline = lottery.deadline,
                                        numWinners = lottery.numWinners,
                                        entrantIDs = lottery.entrantIDs.filter { it != currentUser.id },
                                        waitlistCapacity = lottery.waitlistCapacity,
                                        pendingWinnerIDs = lottery.pendingWinnerIDs.filter { it != currentUser.id },
                                        confirmedIDs = lottery.confirmedIDs
                                    )
                                )
                            }

                            navController.popBackStack()
                        },
                        shape = RoundedCornerShape(
                            15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarsColors.Confirm,
                            contentColor = StarsColors.OnAccent
                        ),
                        border = BorderStroke(2.dp, StarsColors.OnBackground)
                    ) {
                        Text("Confirm")
                    }

                    Button(
                        onClick = {
                            accept = false
                            decline = false
                        },
                        shape = RoundedCornerShape(
                            15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarsColors.Accent1,
                            contentColor = StarsColors.OnAccent
                        ),
                        border = BorderStroke(2.dp, StarsColors.OnBackground)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "LOTTERY NOT FOUND",
                    fontWeight = FontWeight.Bold,
                    fontSize = 50.sp,
                    color = StarsColors.OnBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 50.sp
                )
            }
        }
    }
}