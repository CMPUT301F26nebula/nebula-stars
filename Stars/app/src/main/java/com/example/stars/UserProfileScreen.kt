package com.example.stars

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.runBlocking

/**
 * The expanded view of a user's profile
 *
 * Only meant for admin currently
 * We'll probably add a permissions thing so owners and users can view each other's profile info for messaging later
 */
@Composable
fun UserProfileScreen(
    navController: NavController,
    currentUser: UserProfile,
    userID: String,
    campsiteRepository: CampsiteRepository,
    imageRepository: ImageRepository,
    userRepository: UserRepository
) {

    var confirmDeletion by remember { mutableStateOf(false) }
    var confirmBan by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var ownedCampsites by remember { mutableStateOf<List<Campsite>>(emptyList<Campsite>()) }

    LaunchedEffect(true) {
        userProfile = userRepository.getWithId(userID)
        ownedCampsites = campsiteRepository.getWithUserId(userID)
    }

    Column(modifier = Modifier.fillMaxSize()){
        ScreenHeader(navController = navController, title = "User Profile")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(60.dp)
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 30.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .background(StarsColors.White),
            contentAlignment = Alignment.TopStart
        ) {
            if (userProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = userProfile!!.profilePic,
                                contentDescription = "profile picture",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(100.dp, 100.dp),
                                contentScale = ContentScale.Crop,
                                fallback = painterResource(R.drawable.default_pfp), // shown when model is null
                                error = painterResource(R.drawable.default_pfp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = userProfile?.name ?: "USER NOT FOUND",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StarsColors.Purple
                        )

                        Text(
                            text =
                                when (userProfile?.permission ?: UserPermission.ENTRANT) {
                                    UserPermission.ENTRANT -> "Entrant"
                                    UserPermission.OWNER -> "Owner"
                                    UserPermission.ADMIN -> "Admin"
                                },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StarsColors.Purple
                        )

                        //ID
                        if (currentUser.permission == UserPermission.ADMIN) {
                            Text(
                                text = userProfile?.id ?: "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Light,
                                color = StarsColors.Purple
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Contact Info:",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StarsColors.Purple
                        )

                        Text(
                            text = userProfile?.email ?: "",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = StarsColors.Purple
                        )

                        Text(
                            text = userProfile?.phone ?: "",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = StarsColors.Purple
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if ((userProfile?.permission
                                ?: UserPermission.ENTRANT) >= UserPermission.OWNER
                        ) {
                            Text(
                                text = "Owned Campsites:",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StarsColors.Purple)
                            if (ownedCampsites.isEmpty()) {
                                Text(
                                    text = "No campsites found",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = StarsColors.Purple
                                )
                            } else {
                                // List of campsites
                                LazyColumn (modifier = Modifier
                                    .height(290.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(StarsColors.Purple)
                                ) {
                                    itemsIndexed(ownedCampsites) { index, campsite ->
                                        CampsiteRow(
                                            campsite = campsite,
                                            color = RandomStarsColor.fromId(campsite.id),
                                            onClick = {
                                                navController.navigate(
                                                    "campsite/${
                                                        Uri.encode(
                                                            campsite.id
                                                        )
                                                    }"
                                                )
                                            },
                                            imageRepository = imageRepository
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (currentUser.permission == UserPermission.ADMIN) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            // if not trying to ban user, show the delete button
                            if (!confirmBan) {
                                AdminActionOnUser(
                                    confirmDeletion,
                                    action = "Delete User",
                                    onRequest = {
                                        confirmDeletion = true
                                        confirmBan = false
                                    },
                                    onConfirm = {
                                        runBlocking {
                                            userRepository.deleteUser(userProfile!!)
                                            confirmDeletion = false
                                            navController.popBackStack()
                                        }
                                    },
                                    onCancel = { confirmDeletion = false }
                                )
                            }

                            // if neither button is expanded put a space between them
                            if (!confirmBan && !confirmDeletion) {
                                Spacer(modifier = Modifier.size(10.dp))
                            }

                            // if not trying to delete userProfile show the ban button
                            if (!confirmDeletion && (userProfile?.permission
                                    ?: UserPermission.ENTRANT) >= UserPermission.OWNER
                            ) {
                                AdminActionOnUser(
                                    confirmBan, "Ban Owner",
                                    onRequest = {
                                        confirmDeletion = false
                                        confirmBan = true
                                    },
                                    onConfirm = {
                                        runBlocking { userRepository.banOwner(userProfile!!) }
                                        confirmDeletion = false
                                        navController.popBackStack()
                                    },
                                    onCancel = { confirmBan = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
