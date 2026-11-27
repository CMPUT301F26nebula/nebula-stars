/*
Popup that lets users select filters and sort the campsite list
 */

package com.example.stars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date


// helper for section titles
@Composable
private fun FilterSectionTitle(text: String) {
    Text(
        text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = StarsColors.OnSurface)
}

/**
 * Popup dialog to choose filters and sorting for the campsite list
 *
 * @param currentFilter filter currently applied
 * @param currentSort sort currently applied
 * @param onApply called with the new filter and sort when the user presses Apply
 * @param onDismiss called when the dialog is closed without applying
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilter: CampsiteFilter,
    currentSort: CampsiteSort,
    onApply: (CampsiteFilter, CampsiteSort) -> Unit,
    onDismiss: () -> Unit
) {
    //only applied when the user presses 'apply'
    var location by remember { mutableStateOf(currentFilter.location) }
    var radiusText by remember { mutableStateOf(currentFilter.radius.toString()) }
    val radius = radiusText.toDoubleOrNull()
    val radiusValid = radius != null && radius > 0
    var dateRange by remember { mutableStateOf(currentFilter.dateRange) }
    var guestsText by remember { mutableStateOf(currentFilter.guests?.toString() ?:  "") }
    var sortField by remember { mutableStateOf(currentSort.field) }
    var ascending by remember { mutableStateOf(currentSort.ascending) }

    var showMap by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = StarsColors.White,
        ) {
            if (showMap) {
                Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                    // from LocationPicker.kt
                    OpenStreetLocationPicker(
                        // edmonton
                        initialLatitude = location?.latitude ?: 53.5461,
                        initialLongitude = location?.longitude ?: -113.4938,
                        initialZoom = 10.0,
                        onLocationConfirmed = { result ->
                            location = result
                            showMap = false
                        }
                    )
                }
            }
            else {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    //header
                    Text(
                        "Filter and Sort",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarsColors.OnSurface
                    )

                    //filter by location
                    FilterSectionTitle("Location")
                    Text(
                        text = location?.let { "%.4f, %.4f".format(it.latitude, it.longitude) } ?: "Any location",
                        color = StarsColors.OnSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showMap = true },
                            colors = ButtonDefaults.buttonColors(StarsColors.Cyan, StarsColors.OnAccent),
                        ) {
                            Text(if (location == null) "Pick location" else "Change location")
                        }
                    }
                    if (location != null) {
                        OutlinedTextField(
                            value = radiusText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                                    radiusText = input
                                }
                            },
                            label = { Text("Search radius (km)") },
                            singleLine = true,
                            isError = !radiusValid,
                            supportingText = {
                                if (!radiusValid)
                                    Text("Distance must be greater than 0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            )
                        }

                    //filter by date
                    FilterSectionTitle("Date")
                    Text(
                        text = dateRange?.let { "${formatter.format(Date(it.start))} – ${formatter.format(Date(it.end))}" } ?: "Any dates",
                        color = StarsColors.OnSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showDatePicker = true },
                            colors = ButtonDefaults.buttonColors(StarsColors.Cyan, StarsColors.OnAccent)
                        ) {
                            Text(if (dateRange == null) "Pick dates" else "Change dates")
                        }
                        if (dateRange != null) {
                            TextButton(
                                onClick = { dateRange = null }
                            ) {
                                Text("Clear")
                            }
                        }
                    }

                    // filter by guests
                    FilterSectionTitle("Guests")
                    OutlinedTextField(
                        value = guestsText,
                        onValueChange = { guestsText = it.filter(Char::isDigit) },
                        label = {Text("Number of guests") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // sort
                    FilterSectionTitle("Sort By")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CampsiteSort.SortField.entries.forEach { field ->
                            //https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-filter-chip.html
                            FilterChip(
                                selected = sortField == field,
                                onClick = { sortField = if (sortField == field) null else field },
                                label = { Text(field.label) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = ascending,
                            onClick = { ascending = true },
                            label = { Text("Ascending") }
                        )
                        FilterChip(
                            selected = !ascending,
                            onClick = { ascending = false },
                            label = { Text("Descending") }
                        )
                    }

                    //buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //clear all
                        TextButton(
                            onClick = {
                                location = null
                                dateRange = null
                                guestsText = ""
                                sortField = null
                                ascending = true
                            }
                        ) {
                            Text("Clear all")
                        }
                        Row {
                            //cancel
                            TextButton(onClick = onDismiss) { Text("Cancel") }

                            //apply
                            Button(
                                onClick = {
                                    onApply(
                                        CampsiteFilter(
                                            location = location,
                                            radius = radius ?: 50.0,
                                            dateRange = dateRange,
                                            guests = guestsText.toIntOrNull()?.takeIf { it > 0 }
                                        ),
                                        CampsiteSort(sortField, ascending)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(StarsColors.Purple, StarsColors.OnBackground)
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
            // date range picker, implemented with the help of Gemini 11/24/2026
            if (showDatePicker) {
                val dateState = rememberDateRangePickerState(
                    initialSelectedStartDateMillis = dateRange?.start,
                    initialSelectedEndDateMillis = dateRange?.end,
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                            utcTimeMillis >= System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                    }
                )
                DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val start = dateState.selectedStartDateMillis
                        val end = dateState.selectedEndDateMillis
                        if (start != null) dateRange = EpochRange(start, end ?: start)
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
                ) {
                    DateRangePicker(
                        state = dateState,
                        title = {
                            Text(
                                "Select your stay",
                                modifier = Modifier.padding(12.dp),
                                color = StarsColors.OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.height(500.dp)
                    )
                }
            }
        }
    }
}
