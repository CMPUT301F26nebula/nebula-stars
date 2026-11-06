package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
        Column(
            modifier = Modifier.weight(1.0f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (tab) {
                UserPermission.ENTRANT -> {
                    Button(
                        onClick = {
                            navController.navigate("campsites")
                        },
                        modifier = Modifier.testTag("campsiteButton") // for testing
                    ) {
                        Text("Campsites")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            navController.navigate("waitlists")
                        },
                        modifier = Modifier.testTag("WaitListsButton") // for testing
                    ) {
                        Text("Waitlists")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            navController.navigate("reservations")
                        },
                        modifier = Modifier.testTag("ReservationsButton") // for testing
                    ) {
                        Text("Reservations")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            navController.navigate("messages")
                        },
                        modifier = Modifier.testTag("MessagesButton") // for testing
                    ) {
                        Text("Messages")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            navController.navigate("notifications")
                        },
                        modifier = Modifier.testTag("NotificationsButton") // for testing
                    ) {
                        Text("Notifications")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            navController.navigate("profile")
                        },
                        modifier = Modifier.testTag("ProfileButton") // for testing
                    ) {
                        Text("Profile")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            navController.navigate("settings")
                        },
                        modifier = Modifier.testTag("SettingsButton") // for testing
                    ) {
                        Text("Settings")
                    }

                    // ... more tabs here
                }
                UserPermission.OWNER -> {
                    Button(
                        onClick = {
                            navController.navigate("upload campsite")
                        },
                        modifier = Modifier.testTag("uploadCampsiteButton") // for testing
                    ) {
                        Text("Add a Campsite")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            navController.navigate("upload lottery")
                        },
                        modifier = Modifier.testTag("uploadLotteryButton") // for testing
                    ) {
                        Text("Create a Lottery")
                    }
                }
                UserPermission.ADMIN -> {
                    Button(
                        onClick = {
                            navController.navigate("users list")
                        },
                        modifier = Modifier.testTag("UsersButton") // for testing
                    ) {
                        Text("Users List")
                    }
                }
            }
        }

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
                        containerColor = starsPurple,
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
                        containerColor = starsPurple,
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
                            containerColor = starsPurple,
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