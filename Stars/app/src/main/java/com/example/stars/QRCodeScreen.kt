
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * displays a campsites qr code so its owner can use it for promotion.
 *
 * @param navController navigation controller for returning to the previous screen
 * @param campsiteId unique identifier of the campsite
 */
@Composable
fun QRCodeScreen(
    navController: NavController,
    campsiteId: String
) {
    val generator = remember { QRCodeGenerator() }

    val qrBitmap: Bitmap? = remember(campsiteId) {
        if (campsiteId.isNotBlank()) {
            generator.generate(campsiteId)
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Campsite QR Code")

        Spacer(modifier = Modifier.size(24.dp))

        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR code for campsite $campsiteId",
                modifier = Modifier
                    .size(280.dp)
                    .testTag("campsiteQRCode")
            )

            Spacer(modifier = Modifier.size(16.dp))

            Text("Scan this QR code to view campsite details.")
        } else {
            Text("No valid campsite ID provided.")
        }

        Spacer(modifier = Modifier.size(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.testTag("qrBackButton")
        ) {
            Text("Back")
        }
    }
}
