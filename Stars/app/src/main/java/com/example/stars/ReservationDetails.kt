package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.R
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Detailed view of a reservation
 */
@Composable
fun ReservationDetails(
    modifier: Modifier = Modifier,
    navController: NavController,
    reservation: Reservation,
    campsite: Campsite,
    campOwner: UserProfile,
    onClickOtherPerson: (String, String) -> Unit,
    otherPersonText: (String, String) -> String,
    onDelete: () -> Unit
) {
    val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
    var confirmLeaveDialogState by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(color = StarsColors.Purple).padding(12.dp)
        ) {
            Text("RESERVATION",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 1.sp,
                    maxFontSize = 24.sp,
                    stepSize = 1.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = StarsColors.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {navController.navigate("campsite/${campsite.id}")},
            colors = ButtonDefaults.buttonColors(StarsColors.Transparent),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(text = "Campsite: " + campsite.title,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 1.sp,
                    maxFontSize = 16.sp,
                    stepSize = 1.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = StarsColors.Black
            )
        }

        Button(
            onClick = {onClickOtherPerson(reservation.entrantId, campOwner.id)},
            colors = ButtonDefaults.buttonColors(StarsColors.Transparent),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(text = otherPersonText(reservation.entrantId, campOwner.name),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 1.sp,
                    maxFontSize = 16.sp,
                    stepSize = 1.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = StarsColors.Black
            )
        }

        Text("Status: ${reservation.status}",
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 1.sp,
                maxFontSize = 14.sp,
                stepSize = 1.sp
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            textAlign = TextAlign.Start,
            color = StarsColors.Black,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,

        )
        Text(
            "Dates: ${
                simpleDateFormat.format(Date(reservation.timeRange.start))
            } - ${
                simpleDateFormat.format(Date(reservation.timeRange.end))
            }",
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 1.sp,
                maxFontSize = 14.sp,
                stepSize = 1.sp
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            textAlign = TextAlign.Start,
            color = StarsColors.Black,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )

        Box(
            modifier = Modifier
                .padding(12.dp, 6.dp)
                .fillMaxWidth()
        ) {

            AdminActionOnUser(
                confirm = confirmLeaveDialogState,
                action = "Leave Reservation",
                onRequest = {confirmLeaveDialogState = !confirmLeaveDialogState},
                onConfirm = {
                    Databases.reservationRepository.removeWithId(reservation.id)
                    confirmLeaveDialogState = false
                    onDelete()
                },
                onCancel = {confirmLeaveDialogState = false},
                Modifier
            )
        }
    }
}

/**
 * Modal dialogue showing the details of a reservation
 */
@Composable
fun ReservationDetailsModal(navController: NavController,
                            reservation: Reservation,
                            onClose: () -> Unit,
                            onClickOtherPerson: (String, String) -> Unit,
                            otherPersonText: (String, String) -> String
) {
    var campsite: Campsite? by remember { mutableStateOf(null) }
    var campOwner: UserProfile? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        campsite = Databases.campsiteRepository.getWithId(reservation.campsiteId)
        campsite?.let { campOwner = Databases.userRepository.getWithId(it.ownerId) }
    }

    Dialog(
        onDismissRequest = onClose
    ) {
        Card(colors  = CardDefaults.cardColors(
            containerColor = StarsColors.White
        ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (campsite != null && campOwner != null) {
                ReservationDetails(
                    navController = navController,
                    reservation = reservation,
                    campsite = campsite!!,
                    campOwner = campOwner!!,
                    onClickOtherPerson = onClickOtherPerson,
                    otherPersonText = otherPersonText,
                    onDelete = {onClose()},

                )
            }
        }
    }
}