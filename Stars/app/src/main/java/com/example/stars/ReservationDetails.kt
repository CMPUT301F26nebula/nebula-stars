package com.example.stars
/**
 * The popups on the calendar screen for reservations, both list and details
 */


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
 * Detailed view of a reservation, clicking on the campsite brings you there and same with the
 * owner/user.
 * Has onClickOtherPerson, otherPersonText, onDelete, and action so that it can be used for both
 * owner and user views.
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
    onDelete: () -> Unit,
    action: String
) {
    // date format
    val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
    // for confirm cancel reservation button
    var confirmLeaveDialogState by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(color = StarsColors.Background).padding(12.dp)
        ) {
            // header
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
                color = StarsColors.OnBackground,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // campsite name and button to go to details screen
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
                color = StarsColors.OnSurface
            )
        }

        // Owner or User button depending on who is looking. goes to details screen
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
                color = StarsColors.OnSurface
            )
        }


        // the status of the reservation
        Text(text = "Status: " +
            when(reservation.status){
                ReservationStatus.INVITED -> "Invited"
                ReservationStatus.ACCEPTED -> "Accepted"
                ReservationStatus.CANCELLED_BY_OWNER -> "Cancelled"
                ReservationStatus.CANCELLED_BY_ENTRANT -> "Cancelled"
                ReservationStatus.DECLINED -> "Declined"
            },
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 1.sp,
                maxFontSize = 14.sp,
                stepSize = 1.sp
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            textAlign = TextAlign.Start,
            color = StarsColors.OnSurface,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,

        )

        // the date range
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
            color = StarsColors.OnSurface,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.padding(6.dp))

        // if it is accepted or invited, have a delete button
        // it shouldn't show when only invited but just in case
        if (reservation.status <= ReservationStatus.ACCEPTED) {
            Box(
                modifier = Modifier
                    .padding(12.dp, 6.dp)
                    .fillMaxWidth()
            ) {

                // not actually admin, just confirm cancel buttons
                AdminActionOnUser(
                    confirm = confirmLeaveDialogState,
                    action = action,
                    onRequest = { confirmLeaveDialogState = !confirmLeaveDialogState },
                    onConfirm = {

                        // set cancelled base on who did it
                        // TODO: notify the person who this effects
                        if (action == "Delete Reservation") {
                            val newReservation =
                                reservation.copy(status = ReservationStatus.CANCELLED_BY_OWNER)
                            Databases.reservationRepository.addOrUpdate(newReservation)
                        } else if (action == "Leave Reservation") {
                            val newReservation =
                                reservation.copy(status = ReservationStatus.CANCELLED_BY_ENTRANT)
                            Databases.reservationRepository.addOrUpdate(newReservation)

                        }

                        confirmLeaveDialogState = false
                        onDelete()
                    },
                    onCancel = { confirmLeaveDialogState = false },
                    Modifier
                )
            }
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
                            otherPersonText: (String, String) -> String,
                            action: String
) {
    var campsite: Campsite? by remember { mutableStateOf(null) }
    var campOwner: UserProfile? by remember { mutableStateOf(null) }


    LaunchedEffect(Unit) {
        campsite = Databases.campsiteRepository.getWithId(reservation.campsiteId)
        campsite?.let { campOwner = Databases.userRepository.getWithId(it.ownerId) }
    }

    // the popup for the reservation list and details
    Dialog(
        onDismissRequest = onClose
    ) {
        Card(colors  = CardDefaults.cardColors(
            containerColor = StarsColors.Surface
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
                    action = action

                )
            }
        }
    }
}