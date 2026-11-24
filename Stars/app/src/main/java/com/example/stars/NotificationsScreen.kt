package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun NotificationsScreen(navController: NavController, currentUser: UserProfile, notificationRepository: NotificationRepository) {
    // Start by getting the list of notifications
    val notifications = remember { mutableStateListOf<Notification>() }
    LaunchedEffect(Unit) {
        notifications.addAll(notificationRepository.getWithUserId(currentUser.id))
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
            if (false) {
                Text("No campsites found", color = StarsColors.White)
            } else {
                // List of notifications
                LazyColumn {
                    items(notifications) { notification ->
                        // Each row can send the user to another screen on click
                        // Clicking also marks as read
                        // TODO: Make read status affect formatting
                        Text(
                            text = notification.content,
                            modifier = Modifier.clickable {
                                notificationRepository.markAsRead(notification)
                                if (notification.navDestination.isNotBlank()) {
                                    navController.navigate(notification.navDestination)
                                }
                            })
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            }
        }
    }
}

@Preview
@Composable
fun NotificationScreenPreview() {
    NotificationsScreen(rememberNavController(), UserProfile(id="4", name="John Doe"),
        NotificationRepository(rememberCoroutineScope()))
}