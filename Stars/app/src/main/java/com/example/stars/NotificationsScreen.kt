package com.example.stars

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun NotificationsScreen(navController: NavController, currentUser: UserProfile, notificationRepository: NotificationRepository, userRepository: UserRepository) {
    // Start by getting the list of notifications
    val notifications = remember { mutableStateListOf<Notification>() }
    LaunchedEffect(Unit) {
        notifications.addAll(notificationRepository.getWithUserId(currentUser.id))
        notifications.sortBy { it.read }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StarsColors.Background)
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
                Text(
                    "No notifications found",
                    modifier = Modifier.fillMaxWidth(),
                    color = StarsColors.OnBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                // List of notifications
                LazyColumn {
                    items(notifications) { notification ->
                        // Each row can send the user to another screen on click
                        // Clicking also marks as read
                        // TODO: Make read status affect formatting
                        NotificationRow(navController, notificationRepository, notification, userRepository)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            }
        }
    }
}

@Composable
fun NotificationRow(navController: NavController, notificationRepository: NotificationRepository, notification: Notification, userRepository: UserRepository) {
    var collapsed by remember { mutableStateOf(notification.read) }

    var senders by remember { mutableStateOf(emptyList<String>()) }
    LaunchedEffect(notification.senderIds) {
        for (senderId in notification.senderIds) {
            senders += userRepository.getWithId(senderId)?.name ?: ""
        }
        Log.d("Notif Sender", "Launch:")
        Log.d("Notif Sender", senders.size.toString())
        for (sender in senders) {
            Log.d("Notif Sender", sender)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(20.0f))
            .background(StarsColors.Surface)
            .clickable {
                notificationRepository.markAsRead(notification)
                if (notification.navDestination.isNotBlank()) {
                    navController.navigate(notification.navDestination)
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Checkbox(
                checked = collapsed,
                onCheckedChange = { collapsed = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = StarsColors.OnSurface,
                    uncheckedColor = StarsColors.OnSurface,
                    checkmarkColor = StarsColors.OnBackground,
                    disabledCheckedColor = StarsColors.OnSurface,
                    disabledUncheckedColor = StarsColors.OnSurface
                ),
                modifier = Modifier.testTag("Notifications Checkbox")
            )

            if (!collapsed) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(start = 50.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = notification.content,
                        color = StarsColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "From: ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.Center
                        ) {
                            itemsIndexed(senders) { index, sender ->
                                NameTag(
                                    name = sender,
                                    id = notification.senderIds[index],
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )

                                Text(
                                    text = if (index != senders.size - 1) {
                                        ", "
                                    } else {
                                        ""
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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
        NotificationRepository(rememberCoroutineScope()), UserRepository(rememberCoroutineScope())
    )
}