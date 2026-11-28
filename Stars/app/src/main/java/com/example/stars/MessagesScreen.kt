/**
 * Screen to select from a list of messages
 */
package com.example.stars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFilterNotNull
import androidx.compose.ui.util.fastLastOrNull
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.runBlocking
import kotlin.to

/**
 * Shows a list of chats the user is in
 *
 * @param navController navigation controller
 * @param
 */
@Composable
fun MessagesScreen(
    navController: NavController,
    userProfile: UserProfile,
    adminView: Boolean = false
) {
    var userChats: List<Chat> by remember { mutableStateOf(emptyList()) }
    var filter by remember { mutableStateOf("") }
    var chatsReady by remember { mutableStateOf(false) }

    DisposableEffect(userProfile) {
        // punished for not storing info in the user profile...
        val chatUpdateListener = Databases.chatRepository.addListener { newChats ->
            if (adminView) {
                userChats = newChats.fastFilterNotNull()
            } else {
                val newUserChats = newChats.fastFilterNotNull().filter {
                    it.users.contains(userProfile.id) && it.messages.isNotEmpty()
                }
                if (userChats != newUserChats) {
                    userChats = newUserChats
                }
            }
        }

        onDispose {
            chatUpdateListener.remove()
        }
    }

    LaunchedEffect(Unit) {
        userChats = if (adminView) {
            Databases.chatRepository.getAll()
        } else {
            Databases.chatRepository.getWithUserId(userProfile.id).filter { it.messages.isNotEmpty() }
        }
        chatsReady = true
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            // Header
            ScreenHeader(
                navController = navController,
                title = "MESSAGES"
            )
        },
        bottomBar = {
            // search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(StarsColors.Surface),
                verticalAlignment = Alignment.Bottom
            ) {
                TextField(
                    value = filter,
                    onValueChange = {
                        filter = it
                    },
                    placeholder = {
                        Text(
                            text = "Search Users",
                            color = StarsColors.OnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Thin
                        )
                    },
                    textStyle = TextStyle(
                        color = StarsColors.OnSurface,
                        fontSize = 16.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = StarsColors.OnSurface,
                        unfocusedIndicatorColor = StarsColors.OnSurface.copy(alpha = 0.6f),
                        cursorColor = StarsColors.OnSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .navigationBarsPadding()
                        .testTag("chatSearchTextField")
                )
            }
        }
    ) { innerPadding ->
        StarsBackground(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(12.dp))

                // show loading screen until chats have been fetched from db
                if (chatsReady) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // most recent chats first
                        items(userChats.sortedByDescending { it.messages.lastOrNull()?.timestamp ?: 0L }) { chat ->
                            val participants = runBlocking {
                                Databases.chatRepository.getChatParticipants(
                                    userProfile,
                                    chat,
                                    { user, uri ->
                                        UserProfilePicture(
                                            uri,
                                            60.dp,
                                            user?.name ?: "chatter"
                                        )
                                    }
                                )
                            }
                            // search through recipient names
                            val remoteName = participants.remote?.name ?: ""
                            if (remoteName.contains(filter, true)) {
                                ChatCard(participants, chat, adminView) { chatInfo ->
                                    navController.navigate(
                                        if (adminView) {
                                            "admin messages/${chatInfo.id}"
                                        } else {
                                            "messages/${chatInfo.id}"
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LoadingText()
                }
            }
        }
    }
}