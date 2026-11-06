package com.example.stars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Detailed view of a reservation
 */
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

/**
 * Modal dialogue showing the details of a reservation
 */
@Composable
fun ReservationDetailsModal(reservation: Reservation, onClose: () -> Unit) {
    var campsite: Campsite? by remember { mutableStateOf(null) }
    var campOwner: UserProfile? by remember { mutableStateOf(null) }
    var confirmLeaveDialogState by remember { mutableStateOf(false) }
    val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)

    LaunchedEffect(Unit) {
        campsite = Databases.campsiteRepository.getCampsite(reservation.campsiteId)
        campsite?.let { campOwner = Databases.userRepository.getUser(it.ownerId) }
    }

    // confirm before leaving a reservation
    if (confirmLeaveDialogState) {
        ConfirmOrDenyModal(
            onConfirm = {
                Databases.reservationRepository.removeUserReservation(reservation.id)
                confirmLeaveDialogState = false
                onClose()
            },
            onCancel = {
                confirmLeaveDialogState = false
            },
            title = {
                Text(campsite?.title ?: "MissingNo.")
            },
        ) {
            Text(
                "Cancel reservation from\n${
                    simpleDateFormat.format(Date(reservation.timeRange.start))
                } - ${
                    simpleDateFormat.format(Date(reservation.timeRange.end))
                }?"
            )
        }
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
            },
            topBar = {
                Box(
                    modifier = Modifier
                        .padding(12.dp, 6.dp)
                        .fillMaxWidth()
                ) {
                    if (campsite != null && campOwner != null) {
                        FilledTonalButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                confirmLeaveDialogState = true
                            }
                        ) {
                            Text(
                                text = "Leave Reservation",
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                LoadingText()
            }
        }
    }
}