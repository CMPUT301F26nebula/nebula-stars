package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// TODO add tap/swipe behavior that opens the reservation info
@Composable
fun ReservationCalendarDay(day: CalendarDay, eventColors: Map<String, Color> = emptyMap(), onClick: () -> Unit = {}) {
    val defaults = ButtonDefaults.buttonColors()
    var fg = defaults.containerColor
    var modifier = Modifier
        .fillMaxSize()
        .background(Color.Transparent)
        .clickable(onClick = onClick)

    if (day.isCurrent) {
        fg = defaults.contentColor
        modifier = modifier.background(defaults.containerColor)
    } else if (day.isFiller) {
        modifier = modifier.background(Color.LightGray)
    }
    if (day.events.isNotEmpty()) {
        val colorStops = buildList {
            val stopInc = 1.0f / day.events.size
            var stop = 0.0f
            for (res in day.events) {
                add(stop to (eventColors[res.id] ?: defaults.containerColor))
                stop += stopInc
                add(stop to (eventColors[res.id] ?: defaults.containerColor))
            }
            add(1.0f to (eventColors[day.events.last().id] ?: defaults.containerColor))
        }.toTypedArray()
        //fg = defaults.contentColor
        modifier = modifier.background(Brush.verticalGradient(colorStops = colorStops))
        if (day.isCurrent) {
            modifier = modifier.border(2.dp, defaults.containerColor)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${day.dayNumber()}",
            fontSize = 18.sp,
            //fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@OptIn(ExperimentalGridApi::class)
@Composable
fun ReservationCalendar(reservations: List<Reservation>) {
    // TODO vvv these colours are VERY VERY placeholder
    val colors = listOf(Color.Cyan, Color.Magenta, Color.Yellow, Color.Red, Color.Green, Color.Blue)
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
    // if reservations change a new calendarManager wont be created so we must update it if needed
    calendarManager.updateReservations(reservations)
    var calendarDays by remember { mutableStateOf(calendarManager.getMonthDisplayDays().iterator()) }
    var currentMonth by remember { mutableStateOf(calendarManager.currentMonthName()) }
    var currentYear by remember { mutableIntStateOf(calendarManager.currentYear()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.width(12.dp))
            Text(
                text = currentYear.toString(),
                fontSize = 32.sp
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.width(12.dp))
            Text(
                text = currentMonth,
                fontSize = 32.sp
            )
            Spacer(Modifier.weight(1.0f))
            Button(
                onClick = {
                    calendarManager.lastMonth()
                    // force text to recompose as well
                    calendarDays = calendarManager.getMonthDisplayDays().iterator()
                    currentMonth = calendarManager.currentMonthName()
                    currentYear = calendarManager.currentYear()
                }
            ) {
                Text("<")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    calendarManager.nextMonth()
                    calendarDays = calendarManager.getMonthDisplayDays().iterator()
                    currentMonth = calendarManager.currentMonthName()
                    currentYear = calendarManager.currentYear()
                }
            ) {
                Text(">")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7)
        ) {
            items(dayNames) { dayName ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = dayName,
                        fontSize = 22.sp
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
            modifier = Modifier.fillMaxWidth().aspectRatio(1.0f)
        ) {
            for (week in 1..5) {
                for (day in 1..7) {
                    Box(
                        modifier = Modifier
                            .gridItem(row = week, column = day)
                            .fillMaxSize()
                    ) {
                        if (calendarDays.hasNext()) {
                            ReservationCalendarDay(calendarDays.next(), eventColors = reservationColors)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCalendarScreen(
    navController: NavController,
    currentUser: UserProfile,
    reservationRepository: ReservationRepository
) {
    var userReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var calendarReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userReservations = reservationRepository.getUserReservations(currentUser.id)
        calendarReady = true
    }

    // TODO remove at some point
    /*
    var debugDialogState by remember { mutableStateOf(false) }
    if (debugDialogState) {
        DebugReservationCreator(
            onClose = {
                debugDialogState = false
            },
            onConfirm = { epochRange ->
                reservationRepository.addUserReservation(
                    entrantId = currentUser.id,
                    campsiteId = "test1",
                    epochRange = epochRange
                )
            }
        )
    }
    */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.testTag("backButton") // for testing
            ) {
                Text("<-")
            }

            // Title
            Text(
                text = "Reservations",
                fontSize = 32.sp
            )

            // TODO another debug thing
            /*
            Button(
                onClick = {
                    debugDialogState = true
                }
            ) {
                Text("debug")
            }
            */

            Spacer(Modifier.width(12.dp))
        }
        Spacer(Modifier.weight(1.0f))

        // the thingy doesnt like to recompose properly after the reservations are found so loading screen it is :P
        if (calendarReady) {
            ReservationCalendar(userReservations)
        } else {
            Text(
                text = "Loading...",
                fontSize = 48.sp
            )
        }

        Spacer(Modifier.weight(1.0f))
    }
}