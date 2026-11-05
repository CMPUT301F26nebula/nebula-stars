package com.example.stars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ReservationCard(
    reservation: Reservation,
    campsite: Campsite,
    onClick: (Reservation) -> Unit = {}
) {
    val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
    ElevatedCard(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        onClick = {
            onClick(reservation)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = campsite.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1.0f))
            Text(
                text = "${reservation.status}"
            )
        }
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
fun ReservationList(
    modifier: Modifier = Modifier,
    reservations: List<Reservation>,
    campsites: Map<String, Campsite>,
    onClick: (Reservation) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(reservations) { res ->
            if (res.id in campsites) {
                ReservationCard(res, campsites[res.id] ?: Campsite(), onClick = onClick)
            }
        }
    }
}

@Composable
fun ReservationListModal(
    reservations: List<Reservation>,
    onClick: (Reservation) -> Unit = {},
    onClose: () -> Unit = {}
) {
    var campsites: Map<String, Campsite> by remember { mutableStateOf(emptyMap()) }
    var listReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        campsites = buildMap {
            for (res in reservations) {
                Databases.campsiteRepository.getCampsite(res.campsiteId)?.let {
                    put(res.id, it)
                }
            }
        }
        listReady = true
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
                        onClick = onClose
                    ) {
                        Text("Back")
                    }
                }
            }
        ) { padding ->
            if (listReady) {
                ReservationList(
                    modifier = Modifier.padding(padding),
                    reservations = reservations,
                    campsites = campsites,
                    onClick = onClick
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