package com.example.stars

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

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