package com.example.stars

import android.media.MediaScannerConnection
import android.os.Environment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fitOutside
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

// Based on example from https://www.baeldung.com/kotlin/csv-files by Mikhail Rykov, last updated November 11, 2026
fun OutputStream.writeReservationToCsv(reservation: Reservation, campsiteName: String) {
    val writer = bufferedWriter()
    writer.write("\"Status\", \"Start\", \"End\", \"Campsite\", \"CampsiteID\"")
    writer.newLine()

    val status: String = when (reservation.status) {
        ReservationStatus.INVITED -> "Invited"
        ReservationStatus.ACCEPTED -> "Accepted"
        ReservationStatus.CANCELLED_BY_ENTRANT -> "Cancelled"
        ReservationStatus.CANCELLED_BY_OWNER -> "Cancelled"
    }
    writer.write("${status}, ${reservation.timeRange.start}, ${reservation.timeRange.end}, \"${campsiteName}\", \"${reservation.campsiteId}\"")
    writer.newLine()
    writer.flush()
}

@Composable
fun NotificationCSV(
    navController: NavController,
    currentUser: UserProfile,
    reservationId: String,
    reservationRepository: ReservationRepository,
    campsiteRepository: CampsiteRepository,
    userRepository: UserRepository,
    notificationRepository: NotificationRepository
) {

    var reservation by remember { mutableStateOf(Reservation()) }
    var campsite by remember { mutableStateOf(Campsite()) }
    var campOwner by remember { mutableStateOf(UserProfile()) }
    val context = LocalContext.current

    LaunchedEffect(reservationId) {
        reservation = reservationRepository.getWithId(reservationId) ?: Reservation()
        campsite = campsiteRepository.getWithId(reservation.campsiteId)?: Campsite()
        campOwner = userRepository.getWithId(campsite.ownerId) ?: UserProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(Modifier, navController, "Download CSV")

        Box(modifier = Modifier
            .padding(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(StarsColors.Surface)) {
            ReservationDetails(
                Modifier.clip(RoundedCornerShape(15.dp)).border(2.dp, StarsColors.Surface),
                navController,
                currentUser,
                reservation,
                campsite,
                campOwner,
                { user, owner -> navController.navigate("user profile/${owner}") },
                { user, owner -> "Owner: $owner" },
                { navController.popBackStack() },
                action = "Leave Reservation",
                notificationRepository,
            )
        }

        // should be noted that downloading a reservation you already have downloaded will overwrite it
        // this means a canceled notif csv will always overwrite the accepted one
        Button(
            onClick = {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(path, "reservation_${reservation.id}.csv")
                FileOutputStream(file).writeReservationToCsv(reservation, campsite.title)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.toString()),
                    // Multipurpose Internet Mail Extensions type, or media type is just the kind of file it is.
                    // It's just a big list of standardized file extensions I think with categories
                    arrayOf("text/csv"),
                    // This is for scanning results
                    // TODO: Check the file is actually there and make some kind of indication?
                    null
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = StarsColors.OnSurface, contentColor = StarsColors.OnBackground),
            border = BorderStroke(8.dp, StarsColors.OnBackground)
        ) {
            Text("Download", fontWeight = FontWeight.Bold, fontSize = 60.sp)
        }
    }
}