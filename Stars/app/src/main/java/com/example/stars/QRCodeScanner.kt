
package com.example.stars

import android.net.Uri
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

fun parseCampsiteQrCode(rawValue: String?): String? {
    if (rawValue.isNullOrBlank()) return null

    val uri = Uri.parse(rawValue)

    if (uri.scheme != "stars" || uri.host != "campsite") {
        return null
    }

    val segments = uri.pathSegments

    if (segments.size != 1 || segments[0].isBlank()) {
        return null
    }

    return segments[0]
}

@Composable
fun QRCodeScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val errorMessage = remember { mutableStateOf<String?>(null) }

    val options = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
    }

    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context, options)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Scan a Campsite QR Code")

        Button(
            onClick = {
                errorMessage.value = null

                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val campsiteId = parseCampsiteQrCode(barcode.rawValue)

                        if (campsiteId != null) {
                            navController.navigate(
                                "campsite/${Uri.encode(campsiteId)}"
                            )
                        } else {
                            errorMessage.value =
                                "This QR code is not a valid Nebula Stars campsite code."
                        }
                    }
                    .addOnCanceledListener {

                    }
                    .addOnFailureListener {
                        errorMessage.value =
                            "Unable to scan QR code. Please try again."
                    }
            }
        ) {
            Text("Open QR Scanner")
        }

        errorMessage.value?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Back")
        }
    }
}
