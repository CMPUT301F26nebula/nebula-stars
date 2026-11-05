package com.example.stars

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.apps.card.v1.Columns
import kotlinx.coroutines.launch

@Composable
fun CampsiteDetailsScreen(currentUser: UserProfile, campsite: Campsite, imageRepo: ImageRepository) {

    LaunchedEffect(Unit)

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        // white box in middle which holds all buttons
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(
                Color(0xFFFFFFFF),
                shape = RoundedCornerShape(
                    topStart = 10.dp,
                    topEnd = 30.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 10.dp
                )
            ).padding(10.dp).size(height = 500.dp, width = 310.dp)
        )
        {
            Column() {
                val scope = rememberCoroutineScope()
                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri: Uri? ->
                    scope.launch {
                        if (uri != null) {
                            val imageId = imageRepo.uploadImage(uri)
                            imageIds.add(imageId)
                        }
                    }
                }
                Text("Images")
                Text("${imageIds.size}")
                // Current images
                // This will show a delete button internally when editor is true
                ImageCarousel(
                    imageIds = imageIds,
                    imageRepo = imageRepo,
                    editor = true,
                    onDelete = { id ->
                        imageIds.remove(id)
                        imageRepo.deleteImage(id)
                    }
                )
            }
        }
    }
}