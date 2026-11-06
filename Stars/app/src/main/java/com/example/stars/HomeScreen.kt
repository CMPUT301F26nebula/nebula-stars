package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * Home screen to navigate to everything else
 */
@Composable
fun HomeScreen(navController: NavController, currentUser: UserProfile) {
    var tab: UserPermission by remember { mutableStateOf(UserPermission.ENTRANT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(starsPurple)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        //Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(starsWhite)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title
            Text(
                text = "Welcome ${currentUser.name}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = starsPurple,
            )
        }



        // Change to fancier layout later
        // Contents

        HomeLayout(
            rememberNavController(),
            when (tab) {
                UserPermission.ENTRANT -> arrayOf(
                    HomeButtonData("Campsites", "campsiteButton", "campsites"),
                    HomeButtonData("Waitlists", "WaitListsButton", "waitlists"),
                    HomeButtonData("Reservations", "ReservationsButton", "reservations"),
                    HomeButtonData("Messages", "MessagesButton", "messages"),
                    HomeButtonData("Notifications", "NotificationsButton", "notifications"),
                    HomeButtonData("Profile", "ProfileButton", "profile"),
                    HomeButtonData("Settings", "SettingsButton", "settings")
                )

                UserPermission.OWNER -> arrayOf(
                    HomeButtonData("Add a Campsite", "uploadCampsiteButton", "upload campsite"),
                    HomeButtonData("Create a Lottery", "uploadLotteryButton", "upload lottery")
                )

                UserPermission.ADMIN -> arrayOf(
                    HomeButtonData("Users List", "UsersButton", "users list"),
                )
            },
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .padding(10.dp))

    // User/Owner/Admin tabs? Admin has to be able to switch back from User to Admin
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        if (currentUser.permission != UserPermission.ENTRANT) {
            Button(
                onClick = {
                    tab = UserPermission.ENTRANT
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = starsBrightPurple,
                    contentColor = starsWhite
                )
            ) {
                Text(
                    text = "Entrant"
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                onClick = {
                    tab = UserPermission.OWNER
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = starsBrightPurple,
                    contentColor = starsWhite
                )
            ) {
                Text(
                    text = "Owner"
                )
            }

            if (currentUser.permission == UserPermission.ADMIN) {
                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        tab = UserPermission.ADMIN
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = starsBrightPurple,
                        contentColor = starsWhite
                    )
                ) {
                    Text(
                        text = "Admin"
                    )
                }
            }
        }
    }
}
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController(), UserProfile(id="4", name="John Doe", permission = UserPermission.ADMIN))
}