/*
Contains the location picker composable for campsites and its associated types
 */
package com.example.stars

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import java.io.File

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
    onLocationConfirmed: (LocationCoordinates) -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(300.dp),
    initialLatitude: Double = 51.5074,
    initialLongitude: Double = -0.1278,
    initialZoom: Double = 15.0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedLocation by remember {
        mutableStateOf(LocationCoordinates(initialLatitude, initialLongitude))
    }

    val mapView = remember(context) {
        val config = Configuration.getInstance()
        val prefs = context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)

        // 1. Load preferences FIRST (overwrites userAgentValue if called afterward)
        config.load(context, prefs)

        // 2. Set a real, non-placeholder User-Agent for OSM tile policy compliance
        config.userAgentValue = "STARS/1.1 (com.nebula.stars)"

        // 3. Separate base path and tile cache subdirectories to prevent SQLite lock errors
        val basePath = File(context.cacheDir, "osmdroid").apply { mkdirs() }
        val tileCache = File(basePath, "tiles").apply { mkdirs() }
        config.osmdroidBasePath = basePath
        config.osmdroidTileCache = tileCache

        MapView(context).apply {
            // 4. Explicitly assign the standard OSM tile source
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(initialZoom)
            controller.setCenter(GeoPoint(initialLatitude, initialLongitude))
            overlays.add(CopyrightOverlay(context))
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
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
                    onResume()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Selected Location Pin",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .offset(y = (-24).dp)
        )

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