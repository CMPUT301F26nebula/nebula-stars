/*
Contains the location picker composable for campsites and its associated types
 */
package com.example.stars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay

/**
 * Container for a set of coordinates
 *
 * @param latitude the latitude coordinate
 * @param longitude the longitude coordinate
 */
data class LocationCoordinates(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

/**
 * Picks and returns a geolocation from the center pin placed in a view of OpenStreetMap
 * From Gemini "implement a location picker in jetpack compose using OpenStreetMap that returns a set of coordinates", 2026-11-02
 *
 * @param initialLatitude starting latitude of the camera
 * @param initialLongitude starting longitude of the camera
 * @param initialZoom default camera zoon
 * @param onLocationConfirmed function to return location result
 * @param modifier
 */
@Composable
fun OpenStreetLocationPicker(
    initialLatitude: Double = 51.5074, // Default center (e.g., London)
    initialLongitude: Double = -0.1278,
    initialZoom: Double = 15.0,
    onLocationConfirmed: (LocationCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State for the selected coordinates
    var selectedLocation by remember {
        mutableStateOf(LocationCoordinates(initialLatitude, initialLongitude))
    }

    // Remember MapView instance across recompositions
    val mapView = remember {
        val config = Configuration.getInstance()
        config.userAgentValue = "STARS/0.1 (${context.packageName}; franchuk.amy@ualberta.ca)"
        config.osmdroidBasePath = context.cacheDir
        config.osmdroidTileCache = context.cacheDir

        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(initialZoom)
            controller.setCenter(GeoPoint(initialLatitude, initialLongitude))

            overlays.add(CopyrightOverlay(context))
        }
    }

    // Handle MapView lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Embed the OSM MapView
        AndroidView(
            factory = {
                mapView.apply {
                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val center = mapCenter
                            selectedLocation = LocationCoordinates(center.latitude, center.longitude)
                            return false
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            val center = mapCenter
                            selectedLocation = LocationCoordinates(center.latitude, center.longitude)
                            return false
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Fixed Pin Indicator at the Center of the Map
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Selected Location Pin",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                // Offset by -24.dp so the bottom tip of the marker points exactly to center
                .offset(y = (-24).dp)
        )

        // Coordinate Display & Confirmation Button Bar
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Latitude: ${"%.6f".format(selectedLocation.latitude)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Longitude: ${"%.6f".format(selectedLocation.longitude)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onLocationConfirmed(selectedLocation) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Location")
                }
            }
        }
    }
}