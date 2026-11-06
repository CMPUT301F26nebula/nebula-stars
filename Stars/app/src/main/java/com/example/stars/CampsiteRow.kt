package com.example.stars

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.runBlocking

/**
 * Campsite card displayed in the campsite list
 */
@Composable
fun CampsiteRow(
    campsite: Campsite,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    imageRepository: ImageRepository
) {
    Card(
        onClick = onClick,
        colors  = CardDefaults.cardColors(
            containerColor = color,
            contentColor = StarsColors.White
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val imageID = campsite.imageIds.firstOrNull()

                if (!imageID.isNullOrBlank()) {

                    val imageUri =runBlocking { imageRepository.getImageUri(imageID) }
                    AsyncImage(
                        model = imageUri,
                        contentDescription = campsite.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // crops oversized image
                    )
                } else { Text("No image",) }
            }
            // Campsite text
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title
                    Text(
                        text = campsite.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    // Price
                    Text(
                        text = "$${campsite.price}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                // Rating (TODO: Implement)
                Text(
                    text = "(star rating)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                // Description
                Text(
                    text = campsite.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}