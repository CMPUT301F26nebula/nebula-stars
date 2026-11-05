package com.example.stars

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Generic alert for double checking that the user actually wants to do something
 *
 * @param onConfirm callback function called when the user presses "Confirm"
 * @param onCancel callback function called when the user taps outside the dialogue or on "Back"
 * @param title alert title
 * @param content body of the alert
 */
@Composable
fun ConfirmOrDenyModal(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    title: @Composable (() -> Unit) = {},
    content: @Composable (() -> Unit)
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel
            ) {
                Text("Back")
            }
        },
        title = title,
        text = content
    )
}