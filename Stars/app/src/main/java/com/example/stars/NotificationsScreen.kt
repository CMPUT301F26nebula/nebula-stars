package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun NotificationsScreen(navController: NavController, currentUser: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(starsPurple)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(starsWhite)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            BackButton(navController)

            Spacer(modifier = Modifier.weight(1.0f))

            // Title
            Text(
                text = "Notifications",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = starsPurple,
            )

            Spacer(modifier = Modifier.weight(1.0f))
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /*
            if (false) {
                Text("No campsites found", color = starsWhite)
            } else {
                // List of campsites
                LazyColumn {
                    itemsIndexed() { index, campsite ->
                        NotificationRow
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

             */
        }
    }
}

@Preview
@Composable
fun NotificationScreenPreview() {
    NotificationsScreen(rememberNavController(), UserProfile(id="4", name="John Doe"))
}