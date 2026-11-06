/*
Contains composable to display images in a scrollable carousel
 */
package com.example.stars

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Displays a horizontally scrollable gallery of images
 * Based on Gemini "Display scrolling list of images Jetpack Compose" 2026-11-01
 *
 * @param imageIds the IDs of the images to display
 * @param imageRepo the ImageRepository instance used to get download URIs from those IDs
 * @param modifier
 * @param editor true if we want to be able to delete images (admin or owner), false otherwise (viewer)
 * @param onDelete callback function to delete an image
 */
@ExperimentalMaterial3Api
@Composable
fun ImageCarousel(
    imageIds: List<String>,
    imageRepo: ImageRepository,
    modifier: Modifier = Modifier,
    editor: Boolean = false,
    onDelete: (String) -> Unit = {}
) {
    // Grab the download URIs from the repo for the images we've listed
    val imageUris = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) {
        for (imageId in imageIds) {
            try {
                val imageUri = imageRepo.getImageUri(imageId)

                if (imageUri != null) {
                    imageUris.add(imageUri)
                }
            }
            catch (e:Exception){
                Log.e("Invalid Image", "ImageID Removed From Carousel: $imageId")
            }
        }
    }

    if (imageUris.isNotEmpty()) {

        val carouselState = rememberCarouselState(itemCount = { imageUris.size })

        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = 280.dp, // Sets target size for fully visible images
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
        ) { pageIndex ->
            AsyncImage(
                model = imageUris[pageIndex],
                contentDescription = "Carousel Image Item",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (editor) {
                Button(
                    onClick = { onDelete(imageIds[pageIndex]) },
                    content = { Text("Delete Image") }
                )
            }
        }
    }
}