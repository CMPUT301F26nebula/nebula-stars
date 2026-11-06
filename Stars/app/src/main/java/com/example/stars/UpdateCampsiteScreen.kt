/*
Screen to add a new campsite, or update the details for an existing one
 */
package com.example.stars

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

/**
 * Screen to update info for a new or existing campsite
 * Restyled by Gemini, "Working on this android app in jetpack compose.
 * Please set up the layout of UpdateCampsiteScreen such that all elements are visible and usable
 * in a scrollable column and everything is styled/themed to match ProfileScreen. " 2026-11-05
 *
 * @param navController
 * @param currentUser The ID of the currently logged in user
 * @param campsiteRepo The global campsite repository
 * @param imageRepo The global image repository
 * @param campsiteId The ID of the campsite to update, or leave empty to make a new one
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCampsiteScreen(
    navController: NavController,
    currentUser: UserProfile,
    campsiteRepo: CampsiteRepository,
    imageRepo: ImageRepository,
    campsiteId: String = UUID.randomUUID().toString()
) {
    // Set up
    // Variables to store working copy of the campsite
    var title by remember { mutableStateOf("") }
    var price by remember { mutableDoubleStateOf(0.0) }
    var capacity by remember { mutableIntStateOf(0) }
    var description by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    val imageIds = remember { mutableStateListOf<String>() }
    val reservations = remember { mutableStateListOf<String>() }
    val availabilitySchedule = remember { mutableStateListOf<EpochRange>() }
    var location by remember { mutableStateOf(LocationCoordinates(0.0, 0.0)) }
    var campsiteExists by remember(campsiteId) { mutableStateOf(false) }

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

    // If a campsite exists, get its info and fill in
    LaunchedEffect(Unit) {
        val oldCampsite = campsiteRepo.getCampsite(campsiteId)
        if (oldCampsite != null) {
            title = oldCampsite.title
            price = oldCampsite.price
            capacity = oldCampsite.capacity
            description = oldCampsite.description
            rules = oldCampsite.rules
            imageIds.addAll(oldCampsite.imageIds)
            reservations.addAll(oldCampsite.reservationIds)
            availabilitySchedule.addAll(oldCampsite.availabilitySchedule)
            location = oldCampsite.location
        }
    }
    
    // display QR code only for an existing, saved campsite
    LaunchedEffect(campsiteId) {
        val savedCampsite = campsiteRepo.getCampsite(campsiteId)
        campsiteExists = savedCampsite != null &&
                savedCampsite.ownerId == currentUser.id

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
                    text = "CAMPSITE DETAILS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = starsPurple
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Basic Input fields
                Text("TITLE", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = starsPurple,
                        focusedContainerColor = starsPurple,
                        unfocusedTextColor = starsWhite,
                        focusedTextColor = starsWhite,
                        unfocusedLabelColor = starsWhite,
                        focusedLabelColor = starsWhite
                    ),
                    shape = fieldShape,
                    modifier = Modifier.fillMaxWidth().testTag("titleInput")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("DESCRIPTION", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = starsPurple,
                        focusedContainerColor = starsPurple,
                        unfocusedTextColor = starsWhite,
                        focusedTextColor = starsWhite,
                        unfocusedLabelColor = starsWhite,
                        focusedLabelColor = starsWhite
                    ),
                    shape = fieldShape,
                    modifier = Modifier.fillMaxWidth().testTag("descriptionInput")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("PRICE ($)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                OutlinedTextField(
                    value = if (price == 0.0) "" else price.toString(),
                    onValueChange = { newValue ->
                        if (newValue.isEmpty()) {
                            price = 0.0
                        } else {
                            newValue.toDoubleOrNull()?.let { price = it }
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
                    modifier = Modifier.fillMaxWidth().testTag("priceInput"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("CAPACITY", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                OutlinedTextField(
                    value = if (capacity == 0) "" else capacity.toString(),
                    onValueChange = { newValue ->
                        if (newValue.isEmpty()) {
                            capacity = 0
                        } else {
                            newValue.toIntOrNull()?.let { capacity = it }
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
                    modifier = Modifier.fillMaxWidth().testTag("capacityInput"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("RULES", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                OutlinedTextField(
                    value = rules,
                    onValueChange = { rules = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = starsPurple,
                        focusedContainerColor = starsPurple,
                        unfocusedTextColor = starsWhite,
                        focusedTextColor = starsWhite,
                        unfocusedLabelColor = starsWhite,
                        focusedLabelColor = starsWhite
                    ),
                    shape = fieldShape,
                    modifier = Modifier.fillMaxWidth().testTag("rulesInput")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Images Section
                Text("IMAGES", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                Text("Count: ${imageIds.size}", color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                val scope = rememberCoroutineScope()
                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri: Uri? ->
                    scope.launch {
                        if (uri != null) {
                            val imageId = imageRepo.uploadImage(uri)
                            imageIds.add(imageId)
                        }
                    }
                }

                ImageCarousel(
                    imageIds = imageIds,
                    imageRepo = imageRepo,
                    editor = true,
                    onDelete = { id ->
                        imageIds.remove(id)
                        imageRepo.deleteImage(id)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(starsCyan),
                    shape = buttonShape,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Add Image", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Availability (dates) Section
                Text("AVAILABILITY", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = starsPurple)

                val formatter = SimpleDateFormat("yyyy-MM-dd", LocalLocale.current.platformLocale)

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    availabilitySchedule.forEach { dateRange ->
                        Text(
                            "• Start: ${formatter.format(Date(dateRange.start))} | End: ${formatter.format(Date(dateRange.end))}",
                            color = starsPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                var newStart by remember { mutableLongStateOf(0L) }
                var newEnd by remember { mutableLongStateOf(0L) }
                val dateRangePickerState = rememberDateRangePickerState(
                    initialDisplayMode = DisplayMode.Input,
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            if (utcTimeMillis <= System.currentTimeMillis()) return false
                            for (currentRange in availabilitySchedule) {
                                if (utcTimeMillis > currentRange.start && utcTimeMillis < currentRange.end) {
                                    return false
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
                            text = "Select availability dates",
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
                        newStart = dateRangePickerState.selectedStartDateMillis ?: 0L
                        newEnd = dateRangePickerState.selectedEndDateMillis ?: 0L
                        if (newStart > 0L && newEnd > 0L) {
                            availabilitySchedule.add(EpochRange(newStart, newEnd))
                            newStart = 0L
                            newEnd = 0L
                        }
                    },
                    colors = ButtonDefaults.buttonColors(starsCyan),
                    shape = buttonShape,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Add new availability date", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Geolocation Section
                Text("LOCATION", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = starsPurple)
                Text("Lat: ${location.latitude}", color = Color.Gray, fontSize = 14.sp)
                Text("Lng: ${location.longitude}", color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                OpenStreetLocationPicker(
                    initialLatitude = if (location.latitude != 0.0) location.latitude else 53.5344,
                    initialLongitude = if (location.longitude != 0.0) location.longitude else 113.4903,
                    onLocationConfirmed = { result -> location = result }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // changed this because this now ensures the button is only displayed to the campsites owner
                if (campsiteExists) {
                    Button(
                        onClick = {
                            navController.navigate("qr code/$campsiteId")
                        },
                        modifier = Modifier.testTag("viewQRCodeButton")
                    ) {
                        Text("View QR Code")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submission Button
                Button(
                    onClick = {
                        campsiteRepo.addOrUpdateCampsite(
                            Campsite(
                                id = campsiteId,
                                title = title,
                                price = price,
                                capacity = capacity,
                                description = description,
                                rules = rules,
                                ownerId = currentUser.id,
                                imageIds = imageIds.toList(),
                                reservationIds = reservations.toList(),
                                availabilitySchedule = availabilitySchedule.toList(),
                                location = location
                            )
                        )
                        Log.d("Campsite Update Submission", "Campsite Uploaded")
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(starsCyan),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(height = 50.dp, width = 300.dp)
                        .testTag("submitCampsite")
                ) {
                    Text("Submit", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}