package com.example.stars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ReservationDetails(
    modifier: Modifier = Modifier,
    reservation: Reservation,
    campsite: Campsite,
    campOwner: UserProfile
) {
    val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
    Column(
        modifier = modifier
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        // TODO these should bring u to their respective details pages
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Campsite:")
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = {}
            ) {
                Text(text = campsite.title)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Campsite Owner:")
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = {}
            ) {
                Text(campOwner.name)
            }
        }
        Text("Status: ${reservation.status}")
        Text(
            "Date: ${
                simpleDateFormat.format(Date(reservation.timeRange.start))
            } - ${
                simpleDateFormat.format(Date(reservation.timeRange.end))
            }"
        )
    }
}

@Composable
fun ReservationDetailsModal(reservation: Reservation, onClose: () -> Unit) {
    var campsite: Campsite? by remember { mutableStateOf(null) }
    var campOwner: UserProfile? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        campsite = Databases.campsiteRepository.getCampsite(reservation.campsiteId)
        campsite?.let { campOwner = Databases.userRepository.getUser(it.ownerId) }
    }

    Dialog(
        onDismissRequest = onClose
    ) {
        Scaffold(
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Spacer(Modifier.weight(1.0f))
                    FilledTonalButton(
                        onClick = {
                            onClose()
                        }
                    ) {
                        Text("Done")
                    }
                }
            }
        ) { padding ->
            if (campsite != null && campOwner != null) {
                ReservationDetails(
                    modifier = Modifier.padding(padding),
                    reservation = reservation,
                    campsite = campsite!!,
                    campOwner = campOwner!!
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Loading...",
                        fontSize = 32.sp
                    )
                }
            }
        }
    }
}