package com.example.stars

import android.icu.util.GregorianCalendar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import android.icu.util.Calendar

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
@OptIn(ExperimentalGridApi::class)
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

    var startDate by remember { mutableStateOf(GregorianCalendar.getInstance()) }
    var endDate by remember { mutableStateOf(GregorianCalendar.getInstance()) }

    var lottery by remember { mutableStateOf(Lottery()) }
    val campsites = remember { mutableListOf<Campsite>() }
    var selectedCampsiteIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        lottery = lotteryRepository.getWithId(lotteryId) ?: Lottery()

        lottery.campsiteIDs.forEach {
            campsiteRepository.getWithId(it)?.let { element -> campsites.add(element) }
        }

        startDate.timeInMillis = 0
        endDate.timeInMillis = 0
    }


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(Modifier, navController, "Confirm Reservation")

        if (lottery.pendingWinnerIDs.contains(currentUser.id)) {
            // Calendar
            val colors = listOf(StarsColors.Cyan, StarsColors.Magenta, StarsColors.Yellow)
            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

            val lotteryReservation = Reservation(timeRange = lottery.timePeriod)
            val reservations = mutableListOf(lotteryReservation)

            // make sure reservation colour if consistent for all days
            val reservationColors = buildMap {
                var i = 0
                for (res in reservations) {
                    put(res.id, colors[i])
                    i = (i + 1) % colors.size
                }
            }

            val calendarManager by remember { mutableStateOf(CalendarManager(reservations)) }
            var calendarDays by remember { mutableStateOf(calendarManager.getMonthDisplayDays().iterator()) }
            var currentMonth by remember { mutableStateOf(calendarManager.currentMonthName()) }
            var currentYear by remember { mutableIntStateOf(calendarManager.currentYear()) }

            // if reservations change a new calendarManager wont be created so we must update it if needed
            LaunchedEffect(reservations, currentMonth) {
                calendarManager.updateReservations(reservations)
                val refreshCalender = {
                    calendarDays = calendarManager.getMonthDisplayDays().iterator()
                    currentMonth = calendarManager.currentMonthName()
                    currentYear = calendarManager.currentYear()
                }
                refreshCalender()
            }

            val changeMonthButtonModifier = Modifier
                .border(1.dp, StarsColors.White, shape = RoundedCornerShape(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = currentYear.toString(),
                        fontSize = 32.sp,
                        color = StarsColors.White
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = currentMonth,
                        fontSize = 38.sp,
                        color = StarsColors.White
                    )
                    Spacer(Modifier.weight(1.0f))
                    // change month buttons
                    Button(
                        modifier = changeMonthButtonModifier,
                        onClick = {
                            currentMonth = calendarManager.lastMonth()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarsColors.Transparent,
                            contentColor = StarsColors.White
                        )
                    ) {
                        Text(
                            text = "<",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        modifier = changeMonthButtonModifier,
                        onClick = {
                            currentMonth = calendarManager.nextMonth()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarsColors.Transparent,
                            contentColor = StarsColors.White
                        )
                    ) {
                        Text(
                            text = ">",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.background(StarsColors.White)
                ) {
                    items(dayNames) { dayName ->
                        Box(
                            modifier = Modifier
                                .background(color = StarsColors.White)
                                .height(40.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = StarsColors.Purple
                            )
                        }
                    }
                }
                Grid(
                    config = {
                        for (week in 1..5) {
                            row(1.fr)
                        }
                        for (day in 1..7) {
                            column(1.fr)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.0f)
                ) {
                    for (week in 1..5) {
                        for (day in 1..7) {
                            Box(
                                modifier = Modifier
                                    .gridItem(row = week, column = day)
                                    .fillMaxSize()
                                    .background(StarsColors.White)
                            ) {
                                Box(modifier = Modifier
                                    .padding(1.dp)
                                    .background(StarsColors.Purple)) {
                                    if (calendarDays.hasNext()) {
                                        ReservationCalendarDay(
                                            calendarDays.next(),
                                            eventColors = reservationColors,
                                            onClick = {

                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
                        enabled = (startDate.timeInMillis > 0 && endDate.timeInMillis > 0),
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
                                            EpochRange(startDate.timeInMillis, endDate.timeInMillis)
                                        ),
                                        EpochRange(startDate.timeInMillis, endDate.timeInMillis),
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