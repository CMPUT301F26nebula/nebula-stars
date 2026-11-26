/*
Contains composable to display images in a scrollable carousel
 */
package com.example.stars

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlin.collections.emptyList
import kotlin.emptyArray

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
    var imageUris by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(imageIds) {
        val updatedList = mutableListOf<String>()
        for (imageId in imageIds) {
            try {
                val imageUri = imageRepo.getImageUri(imageId)

                if (imageUri != null) {
                    updatedList.add(imageUri)
                }
            }
            catch (e:Exception){
                Log.e("Invalid Image", "ImageID Removed From Carousel: $imageId")
            }
        }
        imageUris = updatedList
    }

    if (imageUris.isNotEmpty()) {

        val carouselState = rememberCarouselState(itemCount = { imageUris.size })

        Column(verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalCenteredHeroCarousel(
                state = carouselState,
                //preferredItemWidth = 280.dp, // Sets target size for fully visible images
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) { pageIndex ->

                // button disappears
                AsyncImage(
                    model = imageUris[pageIndex],
                    contentDescription = "Carousel Image Item",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

            }
            if (editor) {
                Button(
                    onClick = { onDelete(imageIds[carouselState.currentItem]) },
                    content = { Text("Delete Image",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = StarsColors.White,)
                    },
                    modifier = Modifier.height(50.dp).fillMaxWidth().offset(y = 3.dp).testTag("Delete Image"),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    ),
                    colors = ButtonDefaults.buttonColors(StarsColors.Magenta)
                )
            }
        }
    }
}