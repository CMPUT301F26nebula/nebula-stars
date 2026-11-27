package com.example.stars

import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import kotlin.collections.plus

/**
 * A screen that only admins have access to from (currently) the home screen.
 * Shows a list of all notifications sent in the app
 */
@Composable
fun AdminViewNotifications(navController: NavController, notificationRepository: NotificationRepository, userRepository: UserRepository) {
    // Start by getting the list of notifications
    val notifications = remember { mutableStateListOf<Notification>() }
    LaunchedEffect(Unit) {
        notifications.addAll(notificationRepository.getGlobalNotifications().sortedByDescending { it.timestamp })
    }

    // column to hold things and make background purple
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        ScreenHeader(
            navController = navController,
            title = "Notification Log"
        )

        // column for each individual user card
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {

            items(notifications) { notification ->
                Spacer(modifier = Modifier.height(12.dp))
                AdminNotificationRow(navController, notification, userRepository)
            }
        }
    }
}

/**
 * these rows shows all the notifications that exist, their sender, recipient, content, and timestamp
 */
@Composable
fun AdminNotificationRow(navController: NavController, notification: Notification, userRepository: UserRepository) {
    var recipient by remember { mutableStateOf("") }
    var senders by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(notification.userId) {
        recipient = userRepository.getWithId(notification.userId)?.name ?: ""
    }

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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Content
            Text(
                text = notification.content,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Recipient
            NameTag(
                label = "Recipient: ",
                name = recipient,
                id = Uri.encode(notification.userId),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            /*
            Text(
                text = "Recipient: ${notification.userId}",
                modifier = Modifier.clickable(onClick = { navController.navigate("user profile/${Uri.encode(notification.userId)}") }),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
                )
                */

            //Senders
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(
                    text = "From: ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
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

            // Timestamp
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                LocalLocale.current.platformLocale
            )
            Text(
                text = formatter.format(notification.timestamp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}