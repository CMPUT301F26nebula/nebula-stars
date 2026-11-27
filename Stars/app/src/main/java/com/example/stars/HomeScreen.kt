package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    var tab: UserPermission by rememberSaveable { mutableStateOf(UserPermission.ENTRANT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        //header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StarsColors.Surface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Welcome back,",
                color = StarsColors.OnSurface,
                fontSize = 20.sp
            )
            Text(
                text = currentUser.name,
                color = StarsColors.OnSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // entrant/owner/admin switch that is only shown to owners and admins
            if (currentUser.permission != UserPermission.ENTRANT) {
                Spacer(Modifier.height(12.dp))
                RoleSwitch(
                    selected = tab,
                    maxPermission = currentUser.permission,
                    onSelect = { tab = it }
                )
            }
        }

        // tiles
        HomeLayout(
            navController,
            when (tab) {
                UserPermission.ENTRANT -> listOf(
                    listOf(
                        HomeButtonData("Campsites", "campsiteButton", "campsites", Icons.Default.Place)
                    ),
                    listOf(
                        HomeButtonData("Reservations", "ReservationsButton", "reservations", Icons.Default.DateRange),
                        HomeButtonData("Waitlists", "WaitListsButton", "waitlists", Icons.AutoMirrored.Filled.List)
                    ),
                    listOf(
                        HomeButtonData("Messages", "MessagesButton", "messages", Icons.Default.Email),
                        HomeButtonData("Notifications", "NotificationsButton", "notifications", Icons.Default.Notifications)
                    ),
                    listOf(
                        HomeButtonData("Profile", "ProfileButton", "profile", Icons.Default.Person),
                        HomeButtonData("Settings", "SettingsButton", "settings", Icons.Default.Settings)
                    )
                )

                UserPermission.OWNER -> listOf(
                    listOf(HomeButtonData("Add a Campsite", "uploadCampsiteButton", "upload campsite", Icons.Default.Add)),
                    listOf(HomeButtonData("Create a Lottery", "uploadLotteryButton", "upload lottery", Icons.Default.Star))
                )

                UserPermission.ADMIN -> listOf(
                    listOf(HomeButtonData("Users List", "UsersButton", "users list", Icons.Default.AccountCircle),),
                    listOf(HomeButtonData("Messages Log", "MessageLogButton", "admin messages", Icons.Default.MailOutline)),
                    listOf(HomeButtonData("Notification Log", "NotificationLogButton", "notification log", Icons.Default.Notifications)),
                    listOf(HomeButtonData("Images", "images view", "admin images", Icons.Default.Face)),
                )
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        )
    }
}

@Composable
private fun RoleSwitch(
    selected: UserPermission,
    maxPermission: UserPermission,
    onSelect: (UserPermission) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, StarsColors.Highlight, shape)
    ) {
        // owners and admins both see these two
        RoleButton("Entrant", "entrantHome", selected == UserPermission.ENTRANT, Modifier.weight(1f)) {
            onSelect(UserPermission.ENTRANT)
        }
        RoleButton("Owner", "ownerHome", selected == UserPermission.OWNER, Modifier.weight(1f)) {
            onSelect(UserPermission.OWNER)
        }

        // only admins see this one
        if (maxPermission == UserPermission.ADMIN) {
            RoleButton("Admin", "adminHome", selected == UserPermission.ADMIN, Modifier.weight(1f)) {
                onSelect(UserPermission.ADMIN)
            }
        }
    }
}

/**
 * a button in the role switch
 * filled with the highlight color when selected.
 */
@Composable
private fun RoleButton(
    label: String,
    tag: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) StarsColors.Highlight else StarsColors.Transparent
    val textColor = if (isSelected) StarsColors.OnHighlight else StarsColors.OnSurface

    Text(
        text = label,
        color = textColor,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(tag)
    )
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController(), UserProfile(id = "4", name = "John Doe", permission = UserPermission.ADMIN))
}