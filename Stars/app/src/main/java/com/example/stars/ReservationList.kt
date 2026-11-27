package com.example.stars

import android.R
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Brief view of a reservation
 *
 * @param reservation Reservation object to display
 * @param campsite campsite the reservation is for
 * @param onClick callback function for when the card is tapped that receives the displayed reservation
 */
@Composable
fun ReservationCard(
    reservation: Reservation,
    campsite: Campsite,
    onClick: (Reservation) -> Unit = {}
) {
    var colour: androidx.compose.ui.graphics.Color
    val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)


    // change the shown status text to the correct word to the status and the colour to match
    //TODO change the colours to correct
    val text = when(reservation.status){
        ReservationStatus.INVITED -> {
            colour = StarsColors.Background
            "Invited"
        }
        ReservationStatus.ACCEPTED -> {
            colour = StarsColors.Accent1
            "Accepted"
        }
        ReservationStatus.CANCELLED_BY_OWNER -> {
            colour = StarsColors.Accent2
            "Cancelled"
        }
        ReservationStatus.CANCELLED_BY_ENTRANT -> {
            colour = StarsColors.Accent2
            "Cancelled"
        }
        ReservationStatus.DECLINED -> {
            colour = StarsColors.Accent3
            "Declined"
        }
    }

    ElevatedCard(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        onClick = {
            onClick(reservation)
        },
        shape = RoundedCornerShape(
            10.dp
        ),
        colors = CardDefaults.cardColors(colour)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = campsite.title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 1.sp,
                    maxFontSize = 24.sp,
                    stepSize = 1.sp
                ),
                modifier = Modifier.weight(0.7f).padding(4.dp),
                color = StarsColors.OnBackground
            )

            Text(text = text,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 1.sp,
                    maxFontSize = 20.sp,
                    stepSize = 1.sp
                ),
                modifier = Modifier.weight(0.3f).padding(4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = StarsColors.OnBackground
            )
        }
        Text(
            "Dates: ${
                simpleDateFormat.format(Date(reservation.timeRange.start))
            } - ${
                simpleDateFormat.format(Date(reservation.timeRange.end))
            }",
            modifier = Modifier.padding(4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = StarsColors.OnBackground

        )
    }
}

/**
 * Displays a list of reservation cards
 *
 * @param reservations list of reservations to display
 * @param campsites map of reservation IDs to their respective campsite
 * @param onClick callback function for when a card is tapped that receives its displayed reservation
 */
@Composable
fun ReservationList(
    modifier: Modifier = Modifier,
    reservations: List<Reservation>,
    campsites: Map<String, Campsite>,
    onClick: (Reservation) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(4.dp)
    ) {
        items(reservations) { res ->
            if (res.id in campsites) {
                ReservationCard(res, campsites[res.id] ?: Campsite(), onClick = onClick)
            }
        }
    }
}

/**
 * Modal dialogue showing a list of reservations the user can select from
 *
 * @param reservations list of reservations to display
 * @param onClick callback function for when a card is tapped that receives its displayed reservation
 * @param onClose callback function for when the user taps outside the dialogue or on "Back"
 */
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
                Databases.campsiteRepository.getWithId(res.campsiteId)?.let {
                    put(res.id, it)
                }
            }
        }
        listReady = true
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(colors  = CardDefaults.cardColors(
                            containerColor = StarsColors.Surface
                        ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp))
        {
            Column(modifier = Modifier.fillMaxWidth()) {

                Box(modifier = Modifier.fillMaxWidth().background(color = StarsColors.Background).padding(12.dp),
                    ){
                    Text("CAMPSITES", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = StarsColors.OnBackground,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth())
                }

                if (listReady) {
                    ReservationList(
                        reservations = reservations,
                        campsites = campsites,
                        onClick = onClick
                    )
                }
            }
        }
    }
}