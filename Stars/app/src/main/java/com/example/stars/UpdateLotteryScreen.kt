/*
Screen to add a new lottery, or update the details for an existing one
 */
package com.example.stars

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

/**
 * Screen to update info for a new or existing campsite
 *
 * @param navController
 * @param currentUser The ID of the currently logged in user
 * @param campsiteRepo The global campsite repository
 * @param lotteryRepo The global lottery repository
 * @param externalScope The scope needed to run the uploader asynchronously
 * @param lotteryId The ID of the lottery to update, or leave empty to make a new one
 */
@Composable
fun UpdateLotteryScreen(
    navController: NavController,
    currentUser: UserProfile,
    campsiteRepo: CampsiteRepository,
    lotteryRepo: LotteryRepository,
    externalScope: CoroutineScope,
    lotteryId: String = UUID.randomUUID().toString()
) {
    // Set up
    // Variables to store working copy of the lottery
    val campsiteIDs = remember { mutableStateListOf<String>() }
    var timePeriod by remember { mutableStateOf(EpochRange()) }
    var deadline by remember { mutableLongStateOf((-1.0).toLong()) }
    var numWinners by remember { mutableIntStateOf(0) }
    val entrantIDs = remember { mutableStateListOf<String>() }
    val pendingWinnerIDs = remember { mutableStateListOf<String>() }
    val confirmedIDs = remember { mutableStateListOf<String>() }

    val userCampsites = remember { mutableStateListOf<Campsite>() }

    // If a lottery matching the ID exists, get its info and fill in
    LaunchedEffect(Unit) {
        val oldLottery = lotteryRepo.getLottery(lotteryId)
        if (oldLottery != null) {
            campsiteIDs.addAll(oldLottery.campsiteIDs)
            timePeriod = oldLottery.timePeriod
            deadline = oldLottery.deadline
            numWinners = oldLottery.numWinners
            entrantIDs.addAll(oldLottery.entrantIDs)
            pendingWinnerIDs.addAll(oldLottery.pendingWinnerIDs)
            confirmedIDs.addAll(oldLottery.confirmedIDs)
        }
    }

    // We need to know what campsites the user owns; get this too
    // Keep the entire object because we need names and IDs
    LaunchedEffect(Unit) {
        userCampsites.addAll(campsiteRepo.getUserCampsites(currentUser.id))
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
                text = "Add a lottery",
                fontSize = 32.sp
            )

        }

        // Content
        if (deadline <= 0.toLong() || deadline > System.currentTimeMillis()) {
            // Input fields
            // Number of winners
            OutlinedTextField(
                value = numWinners.toString(),
                onValueChange = { newValue: String ->
                    if (newValue.toIntOrNull() != null) {
                        numWinners = newValue.toInt()
                    }
                },
                label = { Text("Number of Winners") },
                modifier = Modifier.fillMaxWidth().testTag("numWinnersInput"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // Deadline date
            // We use this date formatter for both deadline and availability period
            val formatter = SimpleDateFormat("yyyy-MM-dd", LocalLocale.current.platformLocale)
            Text("Entry Deadline: ${formatter.format(Date(deadline))}")
            val datePickerState = rememberDatePickerState(
                initialDisplayMode = DisplayMode.Input,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        // Block all dates prior to the current system clock time
                        if (utcTimeMillis <= System.currentTimeMillis()) {
                            return false
                        }
                        return true
                    }
                }
            )
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select deadline date",
                        modifier = Modifier.padding(24.dp)
                    )
                },
                headline = {
                    Text(
                        text = "Select lottery entry deadline date",
                        modifier = Modifier.padding(12.dp)
                    )
                },
                showModeToggle = true, // Allows switching between calendar and text input
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(
                onClick = {
                    val newDate = datePickerState.selectedDateMillis ?: 0.0.toLong()
                    if (newDate > 0.0.toLong()) {
                        deadline = newDate
                    }
                },
                content = { Text("Set reservation dates") }
            )

            // Campsites (checkbox of campsites owned by user)
            Text("Campsites to include: ")
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Each row consists of a campsite name and its checkbox
                items(userCampsites) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.title)
                        Checkbox(
                            checked = item.id in campsiteIDs,
                            onCheckedChange = {
                                if (item.id in campsiteIDs) {
                                    campsiteIDs.remove(item.id)
                                }
                                else {
                                    campsiteIDs.add(item.id)
                                }
                            }
                        )
                    }
                }
            }
            // Availability date range
            Text("Reservation dates")
            Text("Start: ${formatter.format(Date(timePeriod.start))}")
            Text("End: ${formatter.format(Date(timePeriod.end))}")
            // Picker to change dates
            val dateRangePickerState = rememberDateRangePickerState(
                initialDisplayMode = DisplayMode.Input,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        // Block all dates prior to the current system clock time
                        if (utcTimeMillis <= System.currentTimeMillis()) {
                            return false
                        }
                        // Block all dates prior to the deadline, if set
                        if (deadline > 0.0.toLong() && utcTimeMillis <= deadline) {
                            return false
                        }
                        // Block any dates outside the set availability of every selected campsite
                        for (campsite in userCampsites) {
                            if (campsite.id in campsiteIDs) {
                                for (timeBlock in campsite.availabilitySchedule) {
                                    if (utcTimeMillis > timeBlock.end || utcTimeMillis < timeBlock.start) {
                                        return false
                                    }
                                }
                            }
                        }
                        return true
                    }
                }
            )
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = "Select reservation dates",
                        modifier = Modifier.padding(24.dp)
                    )
                },
                headline = {
                    Text(
                        text = "Select start and end dates for reservations",
                        modifier = Modifier.padding(12.dp)
                    )
                },
                showModeToggle = true, // Allows switching between calendar and text input
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(
                onClick = {
                    val newStart = dateRangePickerState.selectedStartDateMillis ?: 0.0.toLong()
                    val newEnd = dateRangePickerState.selectedEndDateMillis ?: 0.0.toLong()
                    if (newStart > 0.0.toLong() && newEnd > 0.0.toLong()) {
                        timePeriod = EpochRange(newStart, newEnd)
                    }
                },
                content = { Text("Set reservation dates") }
            )

            // Submission
            Button(
                onClick = {
                    externalScope.launch {
                        lotteryRepo.addOrUpdateLottery(
                            Lottery(
                                id = lotteryId,
                                ownerID = currentUser.id,
                                campsiteIDs = campsiteIDs,
                                timePeriod = timePeriod,
                                deadline = deadline,
                                numWinners = numWinners,
                                entrantIDs = entrantIDs,
                                pendingWinnerIDs = pendingWinnerIDs,
                                confirmedIDs = confirmedIDs
                            )
                        )
                        Log.d("Lottery Update Submission", "Lottery ${lotteryId} Uploaded")
                    }
                    navController.popBackStack()
                },
                content = { Text("Submit") },
                modifier = Modifier.testTag("submitLottery")
            )
        }
        // If the lottery is in the past, error out
        else {
            Text("Sorry, this lottery has already finished and can no longer be edited.")
        }
    }
}