package com.example.stars

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

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
    imageRepo: ImageRepository,
    userRepository: UserRepository
) {

    var confirmDeletion by remember { mutableStateOf(false) }
    var confirmBan by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(true) {
        userProfile = userRepository.getWithId(userID)
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
                            modifier = Modifier.clip(CircleShape).size(100.dp, 100.dp),
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

                    Text(
                        text = userProfile?.id ?: "",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Light,
                        color = StarsColors.Purple
                    )

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
                }
            }
        }
    }
}


/*
@Composable
fun AdminUserRow(userProfile: UserProfile, userRepository: UserRepository){

                    // permission level
                    Text(
                        text = permissionLevel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )


                    Text(
                        text = userProfile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // used for phone number formating
                    val nanpVisualTransformation = NanpVisualTransformation()


                    // phone number
                    Text(
                        text = nanpVisualTransformation.filter(AnnotatedString(userProfile.phone)).text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                }

                AsyncImage(
                    model = userProfile.profilePic,
                    contentDescription = "profile picture",
                    modifier = Modifier.clip(CircleShape).size(100.dp, 100.dp),
                    contentScale = ContentScale.Crop,
                    fallback = painterResource(R.drawable.default_pfp), // shown when model is null
                    error = painterResource(R.drawable.default_pfp)
                )
            }

            // buttons to delete and ban
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {

                // if not trying to delete user, show the ban button
                if (!confirmBan) {
                    AdminActionOnUser(
                        confirmDeletion, "Delete User",
                        onRequest = {
                            confirmDeletion = true
                            confirmBan = false
                        },
                        onConfirm = {
                            runBlocking {
                                userRepository.deleteUser(userProfile)
                                confirmDeletion = false
                                deleted = true
                            }
                        },
                        onCancel = { confirmDeletion = false }
                    )
                }

                // if neither button is expanded put a space between them
                if (!confirmBan && !confirmDeletion) {
                    Spacer(modifier = Modifier.size(10.dp))
                }

                // if not trying to ban userProfileshow the delete button
                if (!confirmDeletion && userProfile.permission >= UserPermission.OWNER) {
                    AdminActionOnUser(
                        confirmBan, "Ban Owner",
                        onRequest = {
                            confirmDeletion = false
                            confirmBan = true
                        },
                        onConfirm = {
                            runBlocking { userRepository.banOwner(userProfile) }
                            confirmDeletion = false
                            deleted = true
                        },
                        onCancel = { confirmBan = false }
                    )
                }
            }
        }
    }
}
 */