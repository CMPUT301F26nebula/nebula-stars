/**
 * Composables for entrant-owner direct messages
 */
package com.example.stars

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage

private object PFPSize {
    val width = 64.dp
    val height = 64.dp
}

/**
 * A single chat message
 */
@Composable
fun ChatMessage(userProfile: UserProfile, message: Message) {
    val senderIsCurrentUser = userProfile.id == message.senderId
    var profilePictureUri: Uri? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        profilePictureUri = Databases.imageRepository.getImageUri(userProfile.profilePic)?.toUri()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (senderIsCurrentUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier.width(PFPSize.width).height(PFPSize.height)
        ) {
            if (!senderIsCurrentUser) {
                AsyncImage(
                    model = profilePictureUri,
                    contentDescription = "other profile picture",
                    modifier = Modifier.clip(CircleShape).size(PFPSize.width, PFPSize.height),
                    contentScale = ContentScale.Crop,
                    fallback = painterResource(R.drawable.default_pfp), // shown when model is null
                    error = painterResource(R.drawable.default_pfp)
                )
            }
        }

        val textCardShape = if (senderIsCurrentUser) {
            RoundedCornerShape(10.dp, 0.dp, 10.dp, 10.dp)
        } else {
            RoundedCornerShape(0.dp, 10.dp, 10.dp, 10.dp)
        }

        Card(
            modifier = Modifier
                .padding(12.dp)
                .background(
                    StarsColors.White,
                    textCardShape
                )
        ) {
            Text(message.text)
        }

        Box(
            modifier = Modifier.width(PFPSize.width).height(PFPSize.height)
        ) {
            if (senderIsCurrentUser) {
                AsyncImage(
                    model = profilePictureUri,
                    contentDescription = "user profile picture",
                    modifier = Modifier.clip(CircleShape).size(PFPSize.width, PFPSize.height),
                    contentScale = ContentScale.Crop,
                    fallback = painterResource(R.drawable.default_pfp), // shown when model is null
                    error = painterResource(R.drawable.default_pfp)
                )
            }
        }
    }
}

