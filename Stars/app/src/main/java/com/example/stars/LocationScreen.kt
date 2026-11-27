/*
A screen to show the location of a campsite in its details
 */
package com.example.stars

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * displays a location on a map
 *
 * @param navController navigation controller for returning to the previous screen
 * @param initialLatitude Latitude coordinate of location
 * @param initialLongitude Longitude coordinate of location
 */
@Composable
fun LocationScreen(
    navController: NavController,
    initialLatitude: Double,
    initialLongitude: Double,
) {

    ScreenHeader(
        navController = navController,
        title = "LOCATION"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.size(24.dp))

        OpenStreetLocationPicker(
            selectable = false,
            initialLatitude = initialLatitude,
            initialLongitude = initialLongitude,
            initialZoom = 10.0,
            onlyZoom = true
        )

        Spacer(modifier = Modifier.size(24.dp))

    }
}


