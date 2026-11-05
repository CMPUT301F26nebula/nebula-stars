/*
Screen to add a new campsite, or update the details for an existing one
 */
package com.example.stars

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
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
    var price by remember { androidx.compose.runtime.mutableDoubleStateOf(0.0) }
    var capacity by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var description by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    val imageIds = remember { mutableStateListOf<String>() }
    val reservations = remember { mutableStateListOf<String>() }
    val availabilitySchedule = remember { mutableStateListOf<EpochRange>() }
    var location by remember { mutableStateOf(LocationCoordinates(0.0, 0.0)) }

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
                text = "Add a campsite",
                fontSize = 32.sp
            )

        }

        // Content
        // Basic Input fields
        OutlinedTextField(
            value = title,
            onValueChange = { newValue: String -> title = newValue },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth().testTag("titleInput")
        )
        OutlinedTextField(
            value = description,
            onValueChange = { newValue: String -> description = newValue },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().testTag("descriptionInput")
        )
        OutlinedTextField(
            value = price.toString(),
            onValueChange = { newValue: String ->
                if (newValue.toDoubleOrNull() != null) {
                    price = newValue.toDouble()
                }
            },
            label = { Text("Price ($)") },
            modifier = Modifier.fillMaxWidth().testTag("priceInput"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        OutlinedTextField(
            value = capacity.toString(),
            onValueChange = { newValue: String ->
                if (newValue.toIntOrNull() != null) {
                    capacity = newValue.toInt()
                }
            },
            label = { Text("Capacity") },
            modifier = Modifier.fillMaxWidth().testTag("capacityInput"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = rules,
            onValueChange = { newValue: String -> rules = newValue },
            label = { Text("Rules") },
            modifier = Modifier.fillMaxWidth().testTag("rulesInput")
        )
        // Images
        Row() {
            Column() {
                // Set up photo picker for image uploads if needed
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
                Text("Images")
                Text("${imageIds.size}")
                // Current images
                // This will show a delete button internally when editor is true
                ImageCarousel(
                    imageIds = imageIds,
                    imageRepo = imageRepo,
                    editor = true,
                    onDelete = { id ->
                        imageIds.remove(id)
                        imageRepo.deleteImage(id)
                    }
                )
                // Necessary for uploading new images
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    content = { Text("Add Image") }
                )
            }
        }
        // Availability (dates)
        Row() {
            Column() {
                // Set up timestamp parser
                val formatter =
                    SimpleDateFormat("yyyy-MM-dd", LocalLocale.current.platformLocale)
                // Show list of dates
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availabilitySchedule) { dateRange ->
                        Text("Start: ${formatter.format(Date(dateRange.start))}")
                        Text("End: ${formatter.format(Date(dateRange.end))}")
                    }
                }
                // Picker to add new date
                var newStart by remember { mutableLongStateOf(0.0.toLong()) }
                var newEnd by remember { mutableLongStateOf(0.0.toLong()) }
                val dateRangePickerState = rememberDateRangePickerState(
                    initialDisplayMode = DisplayMode.Input,
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            var selectable = true
                            // Block all dates prior to the current system clock time
                            if (utcTimeMillis <= System.currentTimeMillis()) {
                                selectable = false
                            }
                            // Block any dates inside an already set availability period
                            for (currentRange in availabilitySchedule) {
                                if (utcTimeMillis > currentRange.start && utcTimeMillis < currentRange.end) {
                                    selectable = false
                                }
                            }
                            return selectable
                        }
                    }
                )
                DateRangePicker(
                    state = dateRangePickerState,
                    title = {
                        Text(
                            text = "Select availability dates",
                            modifier = Modifier.padding(24.dp)
                        )
                    },
                    showModeToggle = true, // Allows switching between calendar and text input
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Button(
                    onClick = {
                        newStart = dateRangePickerState.selectedStartDateMillis ?: 0.0.toLong()
                        newEnd = dateRangePickerState.selectedEndDateMillis ?: 0.0.toLong()
                        if (newStart > 0.0.toLong() && newEnd > 0.0.toLong()) {
                            availabilitySchedule.add(EpochRange(newStart, newEnd))
                            newStart = 0.0.toLong()
                            newEnd = 0.0.toLong()
                        }
                    },
                    content = { Text("Add new availability date") }
                )
            }
        }
        // Geolocation
        Row() {
            Column {
                Text(text = "Campsite Location")
                Text(text = "Lat: ${location.latitude}")
                Text(text = "Lng: ${location.longitude}")

                OpenStreetLocationPicker(
                    initialLatitude = if (location.latitude != 0.0) location.latitude else 53.5344,
                    initialLongitude = if (location.longitude != 0.0) location.longitude else 113.4903, // Edmonton Center
                    onLocationConfirmed = { result ->
                        location = result
                    }
                )
            }
        }

        // Submission
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
                        reservations = reservations.toList(),
                        availabilitySchedule = availabilitySchedule.toList(),
                        location = location
                    )
                )
                Log.d("Campsite Update Submission", "Campsite Uploaded")
                navController.popBackStack()
            },
            content = { Text("Submit") },
            modifier = Modifier.testTag("submitCampsite")
        )
    }
}