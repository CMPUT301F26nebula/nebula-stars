/*
 *  Displays a users profile information
 */
package com.example.stars

import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import kotlinx.coroutines.async
import coil3.compose.AsyncImage
import kotlinx.coroutines.runBlocking

/**
 * Circular user profile picture
 *
 * @param uri profile picture [Uri], if null will use a default image
 * @param size width and height of the circle
 * @param username name of the user for image description
 */
@Composable
fun UserProfilePicture(
    uri: Uri?,
    size: Dp,
    username: String = "user"
) {
    AsyncImage(
        model = uri,
        contentDescription = "$username profile picture",
        modifier = Modifier
            .clip(CircleShape)
            .size(size, size),
        contentScale = ContentScale.Crop,
        fallback = painterResource(R.drawable.default_pfp),
        error = painterResource(R.drawable.default_pfp)
    )
}

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
    var userPFP by remember { mutableStateOf<Uri?>(null) }
    var ownedCampsites by remember { mutableStateOf<List<Campsite>>(emptyList<Campsite>()) }

    LaunchedEffect(true) {
        userProfile = userRepository.getWithId(userID)
        userProfile?.let {
            userPFP = imageRepository.getImageUri(it.profilePic)?.toUri()
        }
        ownedCampsites = campsiteRepository.getWithUserId(userID)
    }

    Column(modifier = Modifier.fillMaxSize()){
        ScreenHeader(navController = navController, title = "User Profile")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 30.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .background(StarsColors.Surface),
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
                            UserProfilePicture(
                                userPFP,
                                100.dp,
                                userProfile?.name ?: "user"
                            )
                            /*
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
                             */
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = userProfile?.name ?: "USER NOT FOUND",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StarsColors.OnSurface
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
                            color = StarsColors.OnSurface
                        )

                        //ID
                        if (currentUser.permission == UserPermission.ADMIN) {
                            Text(
                                text = userProfile?.id ?: "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Light,
                                color = StarsColors.OnSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        userProfile?.let { user ->
                            // entrants shouldnt be able to message other entrants, or themselves lol
                            if (!(currentUser.permission == UserPermission.ENTRANT && user.permission == UserPermission.ENTRANT) &&
                                (currentUser.id != user.id)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            // duplicated check in case this gets copy pasted
                                            if (currentUser.id != user.id) {
                                                val chat = runBlocking {
                                                    Databases.chatRepository.getOrCreate(currentUser to user)
                                                }
                                                navController.navigate("messages/${chat.id}")
                                            }
                                        },
                                        colors = RandomStarsColor.buttonColorsFromId(user.id)
                                    ) {
                                        Text(
                                            "Message User",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        if (currentUser.permission != UserPermission.ENTRANT) {

                            Text(
                                text = "Contact Info:",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StarsColors.OnSurface
                            )

                            Text(
                                text = userProfile?.email ?: "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = StarsColors.OnSurface
                            )

                            Text(
                                text = userProfile?.phone ?: "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = StarsColors.OnSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if ((userProfile?.permission
                                ?: UserPermission.ENTRANT) >= UserPermission.OWNER
                        ) {
                            Text(
                                text = "Owned Campsites:",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StarsColors.OnSurface)
                            if (ownedCampsites.isEmpty()) {
                                Text(
                                    text = "No campsites found",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = StarsColors.OnSurface
                                )
                            } else {
                                // List of campsites
                                LazyColumn (modifier = Modifier.clip(RoundedCornerShape(5.dp))
                                    .padding(12.dp)
                                    .background(StarsColors.OnSurface)
                                ) {
                                    itemsIndexed(ownedCampsites) { index, campsite ->
                                        Button(onClick = {
                                                navController.navigate("campsite/${
                                                        Uri.encode(
                                                            campsite.id
                                                        )
                                                    }"
                                                )
                                            },
                                            shape = RoundedCornerShape(
                                                topStart = 5.dp,
                                                topEnd = 5.dp,
                                                bottomStart = 5.dp,
                                                bottomEnd = 5.dp
                                            ),
                                            colors = ButtonDefaults.buttonColors(StarsColors.Accent1),
                                            modifier = Modifier.fillMaxWidth()
                                        ){
                                            Text(campsite.title,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Start,
                                                maxLines = 1, autoSize = TextAutoSize.StepBased(
                                                    minFontSize = 1.sp,
                                                    maxFontSize = 20.sp,
                                                    stepSize = 1.sp
                                                ),
                                                color = StarsColors.OnAccent
                                            )
                                        }

                                    }
                                }
                            }
                        }
                    }

                    if (currentUser.permission == UserPermission.ADMIN) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
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
                                    onCancel = { confirmDeletion = false },
                                    Modifier.weight(1f)
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
                                    onCancel = { confirmBan = false },
                                    Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}