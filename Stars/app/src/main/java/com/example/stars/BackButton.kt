package com.example.stars

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun BackButton(navController: NavController) {
    Button(
        onClick = { navController.popBackStack() },
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = StarsColors.Purple,
            contentColor = StarsColors.White
        ),
        modifier = Modifier
            .testTag("backButton"), // for testing
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = "◀",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}