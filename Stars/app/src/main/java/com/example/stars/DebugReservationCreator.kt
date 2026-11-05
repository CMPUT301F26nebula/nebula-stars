package com.example.stars

import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.window.Dialog
import java.util.Date

// TODO for testing purposes so remove this from the app later, or reuse it for something idk
@Composable
fun DebugReservationCreator(
    onClose: () -> Unit,
    onConfirm: (EpochRange) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()
    var newStart by remember { mutableLongStateOf(0L) }
    var newEnd by remember { mutableLongStateOf(0L) }
    val sdf = SimpleDateFormat("MMM dd yyyy", LocalLocale.current.platformLocale)

    Dialog(
        onDismissRequest = onClose
    ) {
        Scaffold(
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onClose()
                        }
                    ) {
                        Text("nvm")
                    }
                    TextButton(
                        onClick = {
                            newStart = dateRangePickerState.selectedStartDateMillis ?: 0L
                            newEnd = dateRangePickerState.selectedEndDateMillis ?: 0L
                            if (newStart > 0L && newEnd > 0L) {
                                //onConfirm(EpochRange(newStart, newEnd))
                                Log.d(
                                    "datepicker",
                                    "(${sdf.format(Date(newStart))}) - (${sdf.format(Date(newEnd))})"
                                )
                                newStart = 0L
                                newEnd = 0L
                            }
                            onClose()
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        ) { padding ->
            DateRangePicker(
                state = dateRangePickerState,
                showModeToggle = true, // Allows switching between calendar and text input
                modifier = Modifier.fillMaxWidth().padding(padding)
            )
        }
    }
}