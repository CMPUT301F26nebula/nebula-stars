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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

// Helper functions provided by Gemini, https://gemini.google.com/share/d/1QlViE8pcIrwnri9vOBtLyUEivtndgQj3?usp=sharing 2026-11-27
private const val DEFAULT_CAMPSITE_LATITUDE = 53.5461
private const val DEFAULT_CAMPSITE_LONGITUDE = -113.49028

private fun formatCoordinateText(value: Double): String {
    return if (value == 0.0) "" else value.toString()
}

private fun isValidLatitude(lat: Double): Boolean {
    return lat.isFinite() && lat in -90.0..90.0
}

private fun isValidLongitude(lng: Double): Boolean {
    return lng.isFinite() && lng in -180.0..180.0
}

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
    var latInput by remember { mutableStateOf("") }
    var lngInput by remember { mutableStateOf("") }

    // Not editable on this screen, but kept so saving doesn't erase the campsite's ratings
    var ratingTotal by remember { mutableDoubleStateOf(0.0) }
    var ratingCount by remember { mutableIntStateOf(0) }
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

    val keyboardController = LocalSoftwareKeyboardController.current

    // If a campsite exists, get its info and fill in
    LaunchedEffect(campsiteId) {
        val oldCampsite = campsiteRepo.getWithId(campsiteId)
        if (oldCampsite != null) {
            title = oldCampsite.title
            price = oldCampsite.price
            capacity = oldCampsite.capacity
            description = oldCampsite.description
            rules = oldCampsite.rules
            imageIds.clear()
            imageIds.addAll(oldCampsite.imageIds)
            reservations.clear()
            reservations.addAll(oldCampsite.reservationIds)
            availabilitySchedule.clear()
            availabilitySchedule.addAll(oldCampsite.availabilitySchedule)
            location = oldCampsite.location
            latInput = formatCoordinateText(oldCampsite.location.latitude)
            lngInput = formatCoordinateText(oldCampsite.location.longitude)
            ratingTotal = oldCampsite.ratingTotal
            ratingCount = oldCampsite.ratingCount
        }
    }

    // Display QR code only for an existing, saved campsite
    LaunchedEffect(campsiteId) {
        val savedCampsite = campsiteRepo.getWithId(campsiteId)
        campsiteExists = savedCampsite != null &&
                savedCampsite.ownerId == currentUser.id
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize().imePadding()
        ) {
            // Header
            ScreenHeader(
                navController = navController
            )

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // White Card Container (matching ProfileScreen Box)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StarsColors.Surface, shape = cardShape)
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
                            color = StarsColors.OnSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Basic Input fields
                        Text(
                            "TITLE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.OnSurface
                        )
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StarsColors.OnSurface,
                                focusedContainerColor = StarsColors.OnSurface,
                                unfocusedTextColor = StarsColors.OnBackground,
                                focusedTextColor = StarsColors.OnBackground,
                                unfocusedLabelColor = StarsColors.OnBackground,
                                focusedLabelColor = StarsColors.OnBackground
                            ),
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth().testTag("titleInput"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "DESCRIPTION",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.OnSurface
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StarsColors.OnSurface,
                                focusedContainerColor = StarsColors.OnSurface,
                                unfocusedTextColor = StarsColors.OnBackground,
                                focusedTextColor = StarsColors.OnBackground,
                                unfocusedLabelColor = StarsColors.OnBackground,
                                focusedLabelColor = StarsColors.OnBackground
                            ),
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth().testTag("descriptionInput"),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "PRICE ($)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.OnSurface
                        )
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
                                unfocusedContainerColor = StarsColors.OnSurface,
                                focusedContainerColor = StarsColors.OnSurface,
                                unfocusedTextColor = StarsColors.OnBackground,
                                focusedTextColor = StarsColors.OnBackground,
                                unfocusedLabelColor = StarsColors.OnBackground,
                                focusedLabelColor = StarsColors.OnBackground
                            ),
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth().testTag("priceInput"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "CAPACITY",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.OnSurface
                        )
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
                                unfocusedContainerColor = StarsColors.OnSurface,
                                focusedContainerColor = StarsColors.OnSurface,
                                unfocusedTextColor = StarsColors.OnBackground,
                                focusedTextColor = StarsColors.OnBackground,
                                unfocusedLabelColor = StarsColors.OnBackground,
                                focusedLabelColor = StarsColors.OnBackground
                            ),
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth().testTag("capacityInput"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "RULES",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.OnSurface
                        )
                        OutlinedTextField(
                            value = rules,
                            onValueChange = { rules = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StarsColors.OnSurface,
                                focusedContainerColor = StarsColors.OnSurface,
                                unfocusedTextColor = StarsColors.OnBackground,
                                focusedTextColor = StarsColors.OnBackground,
                                unfocusedLabelColor = StarsColors.OnBackground,
                                focusedLabelColor = StarsColors.OnBackground
                            ),
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth().testTag("rulesInput")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Images Section
                        Text(
                            "IMAGES",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.OnSurface
                        )
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
                            colors = ButtonDefaults.buttonColors(StarsColors.Accent1, StarsColors.OnAccent),
                            shape = buttonShape,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Add Image", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Availability (dates) Section
                        Text(
                            "AVAILABILITY",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.OnSurface
                        )

                        val formatter = SimpleDateFormat(
                            "MMM dd, yyyy",
                            LocalLocale.current.platformLocale
                        )

                        // Display list of selected date ranges
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            availabilitySchedule.forEach { dateRange ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${formatter.format(Date(dateRange.start))} - " +
                                                "${formatter.format(Date(dateRange.end))}",
                                        color = StarsColors.OnSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Button(
                                        modifier = Modifier.width(IntrinsicSize.Min),
                                        onClick = {
                                            availabilitySchedule.remove(dateRange)
                                        }
                                    ) {
                                        Text(
                                            text = "X",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        var newStart by remember { mutableLongStateOf(0L) }
                        var newEnd by remember { mutableLongStateOf(0L) }
                        val dateRangePickerState = rememberDateRangePickerState(
                            initialDisplayMode = DisplayMode.Picker,
                            selectableDates = object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                    if (utcTimeMillis <= System.currentTimeMillis()) return false
                                    for ((start, end) in availabilitySchedule) {
                                        if (utcTimeMillis in start..end) {
                                            return false
                                        }
                                    }
                                    return true
                                }
                            }
                        )

                        var dateRangeDialogState by remember { mutableStateOf(false) }
                        val closeDialog = {
                            newStart = 0L
                            newEnd = 0L
                            dateRangePickerState.setSelection(null, null)
                            dateRangeDialogState = false
                        }
                        if (dateRangeDialogState) {
                            Dialog(
                                onDismissRequest = closeDialog
                            ) {
                                val dialogHeight = when (dateRangePickerState.displayMode) {
                                    DisplayMode.Picker -> 600.dp
                                    else -> 300.dp
                                }
                                Scaffold(
                                    modifier = Modifier
                                        .background(StarsColors.Transparent)
                                        .height(dialogHeight),
                                    bottomBar = {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            TextButton(
                                                onClick = closeDialog
                                            ) {
                                                Text("Cancel", fontSize = 18.sp)
                                            }
                                            Button(
                                                onClick = {
                                                    newStart =
                                                        dateRangePickerState.selectedStartDateMillis
                                                            ?: 0L
                                                    newEnd =
                                                        dateRangePickerState.selectedEndDateMillis ?: 0L
                                                    if (newStart > 0L && newEnd > 0L) {
                                                        val toRemove = mutableListOf<EpochRange>()
                                                        for (range in availabilitySchedule) {
                                                            if (newStart < range.start && range.end < newEnd) {
                                                                toRemove.add(range)
                                                            }
                                                        }
                                                        availabilitySchedule.add(
                                                            EpochRange(
                                                                newStart,
                                                                newEnd
                                                            )
                                                        )
                                                        availabilitySchedule.removeAll(toRemove)
                                                        availabilitySchedule.sortWith { a, b ->
                                                            if (a.start == b.start) {
                                                                if (a.end == b.end) {
                                                                    return@sortWith 0
                                                                }
                                                                return@sortWith (a.end - b.end).toInt()
                                                            }
                                                            (a.start - b.start).toInt()
                                                        }
                                                        closeDialog()
                                                    }
                                                }
                                            ) {
                                                Text("Add", fontSize = 18.sp)
                                            }
                                        }
                                    }
                                ) { innerPadding ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        DateRangePicker(
                                            state = dateRangePickerState,
                                            title = {
                                                Text(
                                                    text = "Select availability dates",
                                                    modifier = Modifier.padding(12.dp),
                                                    color = StarsColors.OnSurface,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            showModeToggle = true,
                                            modifier = Modifier.fillMaxSize(),
                                            focusRequester = null
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Opens date picker dialog
                        Button(
                            onClick = {
                                dateRangeDialogState = true
                            },
                            colors = ButtonDefaults.buttonColors(StarsColors.Accent1, StarsColors.OnAccent),
                            shape = buttonShape,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Add new availability date", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Geolocation Section
                        Text(
                            "Latitude",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.OnSurface
                        )
                        OutlinedTextField(
                            value = latInput,
                            onValueChange = { newValue ->
                                latInput = newValue
                                if (newValue.isEmpty()) {
                                    location = LocationCoordinates(0.0, location.longitude)
                                } else {
                                    val parsed = newValue.toDoubleOrNull()
                                    if (parsed != null && isValidLatitude(parsed)) {
                                        location = LocationCoordinates(parsed, location.longitude)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StarsColors.OnSurface,
                                focusedContainerColor = StarsColors.OnSurface,
                                unfocusedTextColor = StarsColors.OnBackground,
                                focusedTextColor = StarsColors.OnBackground,
                                unfocusedLabelColor = StarsColors.OnBackground,
                                focusedLabelColor = StarsColors.OnBackground
                            ),
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth().testTag("latitudeInput"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "Longitude",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsColors.OnSurface
                        )
                        OutlinedTextField(
                            value = lngInput,
                            onValueChange = { newValue ->
                                lngInput = newValue
                                if (newValue.isEmpty()) {
                                    location = LocationCoordinates(location.latitude, 0.0)
                                } else {
                                    val parsed = newValue.toDoubleOrNull()
                                    if (parsed != null && isValidLongitude(parsed)) {
                                        location = LocationCoordinates(location.latitude, parsed)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = StarsColors.OnSurface,
                                focusedContainerColor = StarsColors.OnSurface,
                                unfocusedTextColor = StarsColors.OnBackground,
                                focusedTextColor = StarsColors.OnBackground,
                                unfocusedLabelColor = StarsColors.OnBackground,
                                focusedLabelColor = StarsColors.OnBackground
                            ),
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth().testTag("longitudeInput"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OpenStreetLocationPicker(
                            initialLatitude = if (location.latitude != 0.0) location.latitude else DEFAULT_CAMPSITE_LATITUDE,
                            initialLongitude = if (location.longitude != 0.0) location.longitude else DEFAULT_CAMPSITE_LONGITUDE,
                            onLocationConfirmed = { result ->
                                location = result
                                latInput = formatCoordinateText(result.latitude)
                                lngInput = formatCoordinateText(result.longitude)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                                scope.launch {
                                    campsiteRepo.addOrUpdate(
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
                                            location = location,
                                            ratingTotal = ratingTotal,
                                            ratingCount = ratingCount
                                        )
                                    )
                                    Log.d("Campsite Update Submission", "Campsite Uploaded")
                                    navController.popBackStack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(StarsColors.Accent1, StarsColors.OnAccent),
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
    }
}