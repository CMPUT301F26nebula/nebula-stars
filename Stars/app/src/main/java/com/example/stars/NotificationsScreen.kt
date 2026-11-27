package com.example.stars

import android.R
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun NotificationsScreen(navController: NavController, currentUser: UserProfile, notificationRepository: NotificationRepository, campsiteRepository: CampsiteRepository) {
    // Start by getting the list of notifications
    val notifications = remember { mutableStateListOf<Notification>() }
    LaunchedEffect(Unit) {
        notifications.addAll(notificationRepository.getWithUserId(currentUser.id))
        notifications.sortBy { it.read }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StarsColors.Purple)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        ScreenHeader(
            navController = navController,
            title = "NOTIFICATIONS"
        )

        StarsBackground {
            // Content
            Column(
                modifier = Modifier
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            if (notifications.isEmpty()) {
                Text("No notifications found", color = StarsColors.OnBackground)
            } else {
                // List of notifications
                LazyColumn {
                    items(notifications) { notification ->
                        // Each row can send the user to another screen on click
                        // Clicking also marks as read
                        // TODO: Make read status affect formatting
                        NotificationRow(navController, notificationRepository, notification, campsiteRepository)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            }
        }
    }
}

@Composable
fun NotificationRow(navController: NavController, notificationRepository: NotificationRepository, notification: Notification, campsiteRepository: CampsiteRepository) {
    /*
    val senderField = notification.sender.split("/")
    val senderType = senderField[0]
    val senderId = senderField.getOrNull(1) ?: ""

    //val senderName: String
    val senderNav = when (senderType) {
        "user" -> "user profile/${Uri.encode(senderId)}"
        "campsite" -> "campsite/${Uri.encode(senderId)}"
        else -> ""
    }
     */

    var campsiteName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        campsiteName = campsiteRepository.getWithId(notification.sender)?.title ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(20.0f))
            .background(StarsColors.White)
            .clickable {
                notificationRepository.markAsRead(notification)
                if (notification.navDestination.isNotBlank()) {
                    navController.navigate(notification.navDestination)
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = notification.content,
                color = StarsColors.OnSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )

            Text(
                text = "For $campsiteName",
                modifier = Modifier.clickable { 
                    navController.navigate("campsite/${Uri.encode(notification.sender)}")
                },
                color = StarsColors.OnSurface,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp
            )
        }
    }
}

@Preview
@Composable
fun NotificationScreenPreview() {
    NotificationsScreen(rememberNavController(), UserProfile(id="4", name="John Doe"),
        NotificationRepository(rememberCoroutineScope()), CampsiteRepository(rememberCoroutineScope()))
}