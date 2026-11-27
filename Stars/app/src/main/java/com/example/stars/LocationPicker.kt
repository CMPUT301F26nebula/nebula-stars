/*
Contains the location picker composable for campsites and its associated types
 */
package com.example.stars

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

data class LocationCoordinates(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

/*
All below code written by Gemini https://gemini.google.com/share/d/1QlViE8pcIrwnri9vOBtLyUEivtndgQj3?usp=sharing 2026-11-26
Apologies for the monster chat log but it's all relevant
 */
private const val OPEN_FREE_MAP_LIBERTY_STYLE = "https://tiles.openfreemap.org/styles/liberty"
private const val EDMONTON_LATITUDE = 53.5461
private const val EDMONTON_LONGITUDE = -113.49028
private const val DEFAULT_CITY_ZOOM = 11.0

@Composable
fun OpenStreetLocationPicker(
    onLocationConfirmed: (LocationCoordinates) -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(360.dp),
    initialLatitude: Double = EDMONTON_LATITUDE,
    initialLongitude: Double = EDMONTON_LONGITUDE,
    initialZoom: Double = DEFAULT_CITY_ZOOM,
    styleUrl: String = OPEN_FREE_MAP_LIBERTY_STYLE
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnLocationConfirmed by rememberUpdatedState(onLocationConfirmed)

    var selectedLocation by remember(initialLatitude, initialLongitude) {
        mutableStateOf(LocationCoordinates(initialLatitude, initialLongitude))
    }

    val mapView = remember(context) {
        MapLibre.getInstance(context.applicationContext)
        val startTarget = LatLng(latitude = initialLatitude, longitude = initialLongitude)
        val options = MapLibreMapOptions.createFromAttributes(context, null)
            .textureMode(true)
            .camera(
                CameraPosition.Builder()
                    .target(startTarget)
                    .zoom(initialZoom)
                    .build()
            )

        val view = MapView(context, options)
        view.onCreate(Bundle())
        view.getMapAsync { mapLibreMap: MapLibreMap ->
            mapLibreMap.setStyle(
                Style.Builder().fromUri(styleUrl),
                object : Style.OnStyleLoaded {
                    override fun onStyleLoaded(style: Style) {
                        val cameraPosition = CameraPosition.Builder()
                            .target(
                                LatLng(
                                    latitude = initialLatitude,
                                    longitude = initialLongitude
                                )
                            )
                            .zoom(initialZoom)
                            .build()
                        mapLibreMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                        mapLibreMap.addOnCameraMoveListener {
                            val target = mapLibreMap.cameraPosition.target
                            if (target != null) {
                                selectedLocation = LocationCoordinates(
                                    latitude = target.latitude,
                                    longitude = target.longitude
                                )
                            }
                        }
                    }
                }
            )
        }
        view
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onStart()
        }
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                factory = { mapView },
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
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Lat: ${"%.6f".format(selectedLocation.latitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Lng: ${"%.6f".format(selectedLocation.longitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Button(
            onClick = { currentOnLocationConfirmed(selectedLocation) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Location")
        }
    }
}