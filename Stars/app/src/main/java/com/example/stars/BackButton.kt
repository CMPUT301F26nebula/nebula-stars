package com.example.stars

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Generic back button. Transparent, so it blends with whatever it sits on.
 *
 * @param navController NavController for popping the ui stack
 * @param tint color of the arrow
 */

// https://chatgpt.com/share/6b08d456-c4f0-83e8-9715-d4edab7e83c4
// "android studio kotlin what are ways to do a back button symbol? Im using a special character right now
// but is there other ways?" Chatgpt, 2026-11-26.
@Composable
fun BackButton(navController: NavController, tint: Color = StarsColors.OnBackground) {
    IconButton(
        onClick = {
            if (!navController.popBackStack()) {
                navController.navigate("home")
            }
        },
        modifier = Modifier
            .testTag("backButton") // for testing
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = tint,
            modifier = Modifier.size(32.dp)
        )
    }
}
