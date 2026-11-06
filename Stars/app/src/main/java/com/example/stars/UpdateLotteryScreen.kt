/*
Screen to add a new lottery, or update the details for an existing one
 */
package com.example.stars

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

/**
 * Screen to update info for a new or existing campsite
 * Restyled by Gemini, "Working on this android app in jetpack compose.
 * Please set up the layout of UpdateLotteryScreen such that all elements are visible and usable
 * in a scrollable column and everything is styled/themed to match ProfileScreen. " 2026-11-05
 *
 * @param navController
 * @param currentUser The ID of the currently logged in user
 * @param campsiteRepo The global campsite repository
 * @param lotteryRepo The global lottery repository
 * @param lotteryId The ID of the lottery to update, or leave empty to make a new one
 */
@Composable
fun UpdateLotteryScreen(
    navController: NavController,
    currentUser: UserProfile,
    campsiteRepo: CampsiteRepository,
    lotteryRepo: LotteryRepository,
    lotteryId: String = UUID.randomUUID().toString()
) {
    // Set up
    // Variables to store working copy of the lottery
    val campsiteIDs = remember { mutableStateListOf<String>() }
    var timePeriod by remember { mutableStateOf(EpochRange()) }
    var deadline by remember { mutableLongStateOf((-1.0).toLong()) }
    var numWinners by remember { mutableIntStateOf(0) }
    val entrantIDs = remember { mutableStateListOf<String>() }
    var waitlistCapacity by remember { mutableIntStateOf(0) }
    val pendingWinnerIDs = remember { mutableStateListOf<String>() }
    val confirmedIDs = remember { mutableStateListOf<String>() }

    val userCampsites = remember { mutableStateListOf<Campsite>() }

    // Shapes matching theme
    val fieldShape = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 10.dp,
        bottomStart = 10.dp,
        bottomEnd = 30.dp
    )
    val buttonShape = RoundedCornerShape(5.dp)
    val cardShape = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 30.dp,
        bottomStart = 10.dp,
        bottomEnd = 10.dp
    )

    // If a lottery matching the ID exists, get its info and fill in
    LaunchedEffect(Unit) {
        val oldLottery = lotteryRepo.getLottery(lotteryId)
        if (oldLottery != null) {
            campsiteIDs.addAll(oldLottery.campsiteIDs)
            timePeriod = oldLottery.timePeriod
            deadline = oldLottery.deadline
            numWinners = oldLottery.numWinners
            entrantIDs.addAll(oldLottery.entrantIDs)
            waitlistCapacity = oldLottery.waitlistCapacity
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.testTag("backButton"),
                colors = ButtonDefaults.buttonColors(starsCyan),
                shape = buttonShape
            ) {
                Text("<-")
            }
        }

        // White Card Container (matching ProfileScreen Box)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(starsWhite, shape = cardShape)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Title Header
                Text(
                    text = "ADD A LOTTERY",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = starsPurple
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (deadline <= 0.toLong() || deadline > System.currentTimeMillis()) {
                    // Input fields
                    Text("NUMBER OF WINNERS", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                    OutlinedTextField(
                        value = if (numWinners == 0) "" else numWinners.toString(),
                        onValueChange = { newValue: String ->
                            if (newValue.isEmpty()) {
                                numWinners = 0
                            } else {
                                newValue.toIntOrNull()?.let { numWinners = it }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = starsPurple,
                            focusedContainerColor = starsPurple,
                            unfocusedTextColor = starsWhite,
                            focusedTextColor = starsWhite,
                            unfocusedLabelColor = starsWhite,
                            focusedLabelColor = starsWhite
                        ),
                        shape = fieldShape,
                        modifier = Modifier.fillMaxWidth().testTag("numWinnersInput"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("MAX NUMBER OF ENTRANTS", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                    OutlinedTextField(
                        value = if (waitlistCapacity == 0) "" else waitlistCapacity.toString(),
                        onValueChange = { newValue: String ->
                            if (newValue.isEmpty()) {
                                waitlistCapacity = 0
                            } else {
                                newValue.toIntOrNull()?.let { parsed ->
                                    waitlistCapacity = if (parsed < entrantIDs.size) entrantIDs.size else parsed
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = starsPurple,
                            focusedContainerColor = starsPurple,
                            unfocusedTextColor = starsWhite,
                            focusedTextColor = starsWhite,
                            unfocusedLabelColor = starsWhite,
                            focusedLabelColor = starsWhite
                        ),
                        shape = fieldShape,
                        modifier = Modifier.fillMaxWidth().testTag("waitlistCapacityInput"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Deadline date
                    Text("ENTRY DEADLINE", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                    val formatter = SimpleDateFormat("yyyy-MM-dd", LocalLocale.current.platformLocale)
                    Text(
                        "Entry Deadline: ${if (deadline > 0) formatter.format(Date(deadline)) else "Not set"}",
                        color = starsPurple,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val datePickerState = rememberDatePickerState(
                        initialDisplayMode = DisplayMode.Input,
                        selectableDates = object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                return utcTimeMillis > System.currentTimeMillis()
                            }
                        }
                    )

                    DatePicker(
                        state = datePickerState,
                        title = {
                            Text(
                                text = "Select deadline date",
                                modifier = Modifier.padding(12.dp),
                                color = starsPurple,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        showModeToggle = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val newDate = datePickerState.selectedDateMillis ?: 0L
                            if (newDate > 0L) {
                                deadline = newDate
                            }
                        },
                        colors = ButtonDefaults.buttonColors(starsCyan),
                        shape = buttonShape,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Set deadline date", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campsites to include
                    Text("CAMPSITES TO INCLUDE", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = starsPurple)

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        userCampsites.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = starsPurple
                                )
                                Checkbox(
                                    checked = item.id in campsiteIDs,
                                    onCheckedChange = {
                                        if (item.id in campsiteIDs) {
                                            campsiteIDs.remove(item.id)
                                        } else {
                                            campsiteIDs.add(item.id)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = starsCyan,
                                        uncheckedColor = starsPurple
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Reservation dates
                    Text("RESERVATION DATES", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                    Text(
                        "Start: ${if (timePeriod.start > 0) formatter.format(Date(timePeriod.start)) else "N/A"} | End: ${if (timePeriod.end > 0) formatter.format(Date(timePeriod.end)) else "N/A"}",
                        color = starsPurple,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val dateRangePickerState = rememberDateRangePickerState(
                        initialDisplayMode = DisplayMode.Input,
                        selectableDates = object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                if (utcTimeMillis <= System.currentTimeMillis()) return false
                                if (deadline > 0L && utcTimeMillis <= deadline) return false
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
                                modifier = Modifier.padding(12.dp),
                                color = starsPurple,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        showModeToggle = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val newStart = dateRangePickerState.selectedStartDateMillis ?: 0L
                            val newEnd = dateRangePickerState.selectedEndDateMillis ?: 0L
                            if (newStart > 0L && newEnd > 0L) {
                                timePeriod = EpochRange(newStart, newEnd)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(starsCyan),
                        shape = buttonShape,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Set reservation dates", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submission
                    Button(
                        onClick = {
                            lotteryRepo.addOrUpdateLottery(
                                Lottery(
                                    id = lotteryId,
                                    ownerID = currentUser.id,
                                    campsiteIDs = campsiteIDs,
                                    timePeriod = timePeriod,
                                    deadline = deadline,
                                    numWinners = numWinners,
                                    entrantIDs = entrantIDs,
                                    waitlistCapacity = waitlistCapacity,
                                    pendingWinnerIDs = pendingWinnerIDs,
                                    confirmedIDs = confirmedIDs
                                )
                            )
                            Log.d("Lottery Update Submission", "Lottery ${lotteryId} Uploaded")
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(starsCyan),
                        shape = buttonShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(height = 50.dp, width = 300.dp)
                            .testTag("submitLottery")
                    ) {
                        Text("Submit", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Sorry, this lottery has already finished and can no longer be edited.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = starsPurple
                    )
                }
            }
        }
    }
}