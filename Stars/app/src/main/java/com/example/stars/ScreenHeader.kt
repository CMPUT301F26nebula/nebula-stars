package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Generic screen header with a back button
 *
 * @param modifier Composable modifier, will set its own width, background, and padding
 * @param navController NavController for the back button
 * @param title name of the screen
 * @param contentAfter Composable content placed to the right of the title
 */
@Composable
fun ScreenHeader(
    modifier: Modifier = Modifier,
    navController: NavController,
    title: String = "",
    contentAfter: @Composable (() -> Unit) = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(StarsColors.White)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Back button
        BackButton(navController)

        Spacer(Modifier.width(12.dp))

        // Title
        Text(
            text = title,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = StarsColors.Purple,
        )

        Spacer(Modifier.weight(1.0f))

        // for the filter button or something
        contentAfter()
    }
}