package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat

/**
 * A screen that only admins have access to from (currently) the home screen.
 * Shows a list of all notifications sent in the app
 */
@Composable
fun AdminViewNotifications(navController: NavController, notificationRepository: NotificationRepository) {
    // Start by getting the list of notifications
    val notifications = remember { mutableStateListOf<Notification>() }
    LaunchedEffect(Unit) {
        notifications.addAll(notificationRepository.getGlobalNotifications().sortedByDescending { it.timestamp })
    }

    // column to hold things and make background purple
    Column(modifier = Modifier.fillMaxSize().background(color = StarsColors.Purple)) {
        // Header
        ScreenHeader(
            navController = navController,
            title = "Notification Log"
        )

        // column for each individual user card
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {

            items(notifications) { notification ->
                Spacer(modifier = Modifier.height(12.dp))
                AdminNotificationRow(notification)
            }
        }
    }
}

/**
 * these rows shows all the notifications that exist, their sender, recipient, content, and timestamp
 */
@Composable
fun AdminNotificationRow(notification: Notification) {
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
            Text(
                text = "Recipient: ${notification.userId}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
                )

            // Sender
            Text(
                text = "Sender: ${notification.sender}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

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