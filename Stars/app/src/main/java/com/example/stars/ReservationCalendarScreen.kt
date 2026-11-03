package com.example.stars

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.TextUnitType.Companion.Sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// TODO add tap/swipe behavior
@Composable
fun ReservationCalendarDay(day: CalendarDay, onClick: () -> Unit = {}) {
    val defaults = ButtonDefaults.buttonColors()
    var fg = defaults.containerColor
    var bg = defaults.contentColor
    if (day.isCurrent) {
        bg = fg.also { fg = bg }
    } else if (day.isFiller) {
        bg = Color.LightGray
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${day.dayNumber()}",
            fontSize = 12.sp,
            color = fg
        )
    }
}

@OptIn(ExperimentalGridApi::class)
@Composable
fun ReservationCalendar(calendarManager: CalendarManager) {
    val calendarDays = calendarManager.getMonthDisplayDays().iterator()
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                            ReservationCalendarDay(calendarDays.next())
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
    var calendarManager by remember { mutableStateOf(CalendarManager()) }

    LaunchedEffect(Unit) {
        val userReservations = reservationRepository.getUserReservations(currentUser.id)
        calendarManager.updateReservations(userReservations)
    }

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

            Spacer(Modifier.width(12.dp))
        }
        Spacer(Modifier.weight(1.0f))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = calendarManager.currentMonthName(),
                fontSize = 32.sp
            )
            Spacer(Modifier.weight(1.0f))
            Button(
                onClick = {
                    calendarManager.lastMonth()
                }
            ) {
                Text("<")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    calendarManager.nextMonth()
                }
            ) {
                Text(">")
            }
        }

        ReservationCalendar(calendarManager)

        Spacer(Modifier.weight(1.0f))
    }
}