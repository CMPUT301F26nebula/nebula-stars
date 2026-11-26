/**
 * Screen to select from a list of messages
 */
package com.example.stars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage

/**
 * Shows a list of chats the user is in
 *
 * @param navController navigation controller
 * @param
 */
@Composable
fun MessagesScreen(
    navController: NavController,
    userProfile: UserProfile
) {
    var userChats: List<Pair<Chat, ChatParticipants>> by remember { mutableStateOf(emptyList()) }
    var chatsReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val chats = Databases.chatRepository.getWithUserId(userProfile.id)
        userChats = buildList {
            for (chat in chats) {
                add(
                    chat to
                    Databases.chatRepository.getChatParticipants(
                        userProfile,
                        chat,
                        { user, uri ->
                            UserProfilePicture(
                                uri,
                                60.dp,
                                user?.name ?: "chatter"
                            )
                            /*
                            AsyncImage(
                                model = uri,
                                contentDescription = "${user?.name ?: "chatter"} profile picture",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(60.dp, 60.dp),
                                contentScale = ContentScale.Crop,
                                fallback = painterResource(R.drawable.default_pfp),
                                error = painterResource(R.drawable.default_pfp)
                            )
                             */
                        }
                    )
                )
            }
        }
        chatsReady = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Header
        ScreenHeader(
            navController = navController,
            title = "MESSAGES"
        )

        Spacer(Modifier.height(12.dp))

        // show loading screen until chats have been fetched from db
        if (chatsReady) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(userChats) { chatInfo ->
                    ChatCard(chatInfo.second, chatInfo.first) { chat ->
                        navController.navigate("messages/${chat.id}")
                    }
                }
            }
        } else {
            LoadingText()
        }
    }
}