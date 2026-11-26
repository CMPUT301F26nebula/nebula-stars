package com.example.stars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Prompts the user with a calendar, campsite selector (if applicable), and accept/decline
 *
 * On accept:
 * moves user from pending winners to confirmed IDs
 * creates a reservation for selected campsite and date range
 * reservation status is [ReservationStatus.INVITED]
 * TODO: sends notif of reservation
 * optionally sends owner notif?
 *
 * On decline:
 * removes user from pending winners
 * removes user from entrant IDs
 * reservation status is [ReservationStatus.DECLINED] ?
 *  can't because you'd need to require a time to create the id
 * TODO: sends notif of cancellation confirmation
 * optionally sends owner notif?
 */
@Composable
fun NotificationLotteryConfirmation(
    navController: NavController,
    currentUser: UserProfile,
    lotteryId: String,
    lotteryRepository: LotteryRepository,
    reservationRepository: ReservationRepository,
    campsiteRepository: CampsiteRepository) {
    var decline by remember { mutableStateOf(false) }
    var accept by remember { mutableStateOf(false) }

    var startDate by remember { mutableLongStateOf(0L) }
    var endDate by remember { mutableLongStateOf(0L) }

    var lottery by remember { mutableStateOf(Lottery()) }
    val campsites = remember { mutableListOf<Campsite>() }
    var selectedCampsiteIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        lottery = lotteryRepository.getWithId(lotteryId) ?: Lottery()

        lottery.campsiteIDs.forEach {
            campsiteRepository.getWithId(it)?.let { element -> campsites.add(element) }
        }
    }


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(Modifier, navController, "Confirm Reservation")

        if (lottery.pendingWinnerIDs.contains(currentUser.id)) {


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
                        enabled = (startDate > 0 && endDate > 0),
                        shape = RoundedCornerShape(
                            15.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarsColors.Magenta,
                            contentColor = StarsColors.White,
                            disabledContainerColor = StarsColors.DullPurple,
                            disabledContentColor = StarsColors.Grey,
                        )
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
                            containerColor = StarsColors.Cyan,
                            contentColor = StarsColors.White
                        )
                    ) {
                        Text("Decline")
                    }
                } else {
                    Button(
                        onClick = {
                            if (accept) {
                                lotteryRepository.addOrUpdate(
                                    Lottery(
                                        id = lottery.id,
                                        ownerID = lottery.ownerID,
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
                                        Reservation.uniqueIdFrom(
                                            currentUser.id,
                                            campsites[selectedCampsiteIndex].id,
                                            EpochRange(startDate, endDate)
                                        ),
                                        EpochRange(startDate, endDate),
                                        ReservationStatus.INVITED,
                                        currentUser.id,
                                        campsites[selectedCampsiteIndex].id
                                    )
                                )
                            } else if (decline) {
                                lotteryRepository.addOrUpdate(
                                    Lottery(
                                        id = lottery.id,
                                        ownerID = lottery.ownerID,
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
                            containerColor = StarsColors.Magenta,
                            contentColor = StarsColors.White
                        )
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
                            containerColor = StarsColors.Cyan,
                            contentColor = StarsColors.White
                        )
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
                    color = StarsColors.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 50.sp
                )
            }
        }
    }
}