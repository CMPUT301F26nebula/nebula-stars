/**
 * Screen for user reservations
 */
package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Calendar

/**
 * A single calendar day that will change its background colour if there is a campsite reservation on that day
 *
 * @param day CalendarDay object to display
 * @param eventColors map of reservation IDs to their display colour
 * @param onClick callback function that receives the list of reservations occurring on this day
 */
@Composable
fun ReservationCalendarDay(
    day: CalendarDay,
    eventColors: Map<String, Color> = emptyMap(),
    onClick: (CalendarDay) -> Unit = {}
) {
    var fg = StarsColors.White
    var circleColor = StarsColors.Transparent
    var modifier = Modifier
        .fillMaxSize()
        .background(StarsColors.Purple)
        .border(1.dp, StarsColors.White)


    if (day.isCurrent) {
        fg = StarsColors.Purple
        circleColor = StarsColors.White
        modifier = modifier.background(StarsColors.White)
    } else   if (day.isFiller) {
        modifier = modifier.background(StarsColors.DullPurple)
    }
    if (day.events.isNotEmpty()) {
        val colorStops = buildList {
            val stopInc = 1.0f / day.events.size
            var stop = 0.0f
            for ((id) in day.events) {
                add(stop to (eventColors[id] ?: StarsColors.Purple))
                stop += stopInc
                add(stop to (eventColors[id] ?: StarsColors.Purple))
            }
            add(1.0f to (eventColors[day.events.last().id] ?: StarsColors.Purple))
        }.toTypedArray()
        modifier = modifier
            .background(Brush.verticalGradient(colorStops = colorStops))
            .clickable(onClick = {
                if (day.events.isNotEmpty()) {
                    onClick(day)
                }
            }
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .padding(12.dp)
                .background(circleColor, shape = RoundedCornerShape(38.dp))
                .aspectRatio(1.0f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${day.dayNumber()}",
                fontSize = 18.sp,
                fontWeight = if (day.isFiller) FontWeight.Normal else FontWeight.Bold,
                color = fg,
            )
        }
    }
}

/**
 * Calendar widget for displaying user reservations
 *
 * @param reservations list of user reservations
 * @param onDayClick callback function for when a day with reservations is tapped
 */
@OptIn(ExperimentalGridApi::class)
@Composable
fun ReservationCalendar(reservations: List<Reservation>, onDayClick: (CalendarDay) -> Unit = {}) {
    val colors = listOf(StarsColors.Cyan, StarsColors.Magenta, StarsColors.Yellow)
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

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
                        Box(modifier = Modifier.padding(1.dp).background(StarsColors.Purple)) {
                            if (calendarDays.hasNext()) {
                                ReservationCalendarDay(
                                    calendarDays.next(),
                                    eventColors = reservationColors,
                                    onClick = onDayClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen showing the reservations of the [currentUser]
 *
 * Displays reservations month by month and days can be clicked to view more info about the reservation
 */
@Composable
fun ReservationCalendarScreen(
    navController: NavController,
    currentUser: UserProfile,
    reservationRepository: ReservationRepository,
) {
    var userReservations: List<Reservation> by remember { mutableStateOf(emptyList()) }
    var calendarReady by remember { mutableStateOf(false) }
    var selectedReservations: List<Reservation> by remember { mutableStateOf(emptyList()) }
    var selectedReservation: Reservation? by remember { mutableStateOf(null) }

    // if we dont call it async itll readd/restart the listener everytime this recomposes, which is everytime the listener fires...
    DisposableEffect(Unit) {
        // TERRIBLY inefficient but we dont store reservations with their user so :P TODO maybe change that later
        val listener = reservationRepository.addListener {
            calendarReady = false
            userReservations = buildList {
                for (reservation in it) {
                    if (reservation != null && reservation.entrantId == currentUser.id) {
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
            selectedReservation!!,
            onClose = {
                selectedReservation = null
            }
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
                selectedReservations = it.events
            }
        } else {
            LoadingText()
        }

        Spacer(Modifier.weight(1.0f))
    }
}