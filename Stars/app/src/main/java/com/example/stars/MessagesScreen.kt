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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
    var userChats: List<Chat> by remember { mutableStateOf(emptyList()) }
    var chatsReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userChats = Databases.chatRepository.getWithUserId(userProfile.id)
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
                items(userChats) {
                    ChatCard(userProfile, it) { chat ->
                        navController.navigate("messages/${chat.id}")
                    }
                }
            }
        } else {
            LoadingText()
        }
    }
}

/**
 * Summary card showing the chat recipient and last message if there is one
 *
 * @param chat chat to display
 * @param onClick click callback that receives this cards Chat instance as an argument
 */
@Composable
fun ChatCard(userProfile: UserProfile, chat: Chat, onClick: (Chat) -> Unit) {
    var localUser: UserProfile? by remember { mutableStateOf(null) }
    var remoteUser: UserProfile? by remember { mutableStateOf(null) }
    var cardColour: Color by remember { mutableStateOf(StarsColors.White) }

    LaunchedEffect(Unit) {
        val entrant = Databases.userRepository.getWithId(chat.userIdA())
        val owner = Databases.userRepository.getWithId(chat.userIdB())
        if (entrant?.id == userProfile.id) {
            localUser = entrant
            remoteUser = owner
        } else if (owner?.id == userProfile.id) {
            localUser = owner
            remoteUser = entrant
        }
        remoteUser?.let {
            cardColour = RandomStarsColor.fromId(it.id)
        }
    }

    if (localUser != null && remoteUser != null) {
        val contentColor = RandomStarsColor.contrast(cardColour)
        Card(
            onClick = {
                onClick(chat)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardColors(
                cardColour,
                contentColor,
                cardColour,
                contentColor
            )
        ) {
            val lastMessage = chat.messages.lastOrNull()
            val fuzzyDate = FuzzyDateFormat(LocalLocale.current.platformLocale)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        remoteUser?.name ?: "",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                lastMessage?.let {
                    Text(
                        fuzzyDate.format(lastMessage.timestamp),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                lastMessage?.let {
                    val lastSender = when (it.senderId) {
                        localUser?.id -> {
                            localUser?.name
                        }
                        remoteUser?.id -> {
                            remoteUser?.name
                        }
                        else -> {
                            ""
                        }
                    }
                    Text(
                        "$lastSender: ${lastMessage.text}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } ?:
                Text(
                    "No messages",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}