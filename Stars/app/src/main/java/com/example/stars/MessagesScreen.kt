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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        //Chat.addDummyChat("9b48653a52e2154c", "bb256b36d6d6efc7")
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
                    ChatCard(it) {
                        // TODO nav to dms screen
                    }
                }
            }
        } else {
            LoadingText()
        }
    }
}

// TODO add colour to this
/**
 * Summary card showing the chat recipient and last message if there is one
 *
 * @param chat chat to display
 * @param onClick click callback that receives this cards Chat instance as an argument
 */
@Composable
fun ChatCard(chat: Chat, onClick: (Chat) -> Unit) {
    var entrant: UserProfile? by remember { mutableStateOf(null) }
    var owner: UserProfile? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        entrant = Databases.userRepository.getWithId(chat.entrantId)
        owner = Databases.userRepository.getWithId(chat.ownerId)
    }

    if (entrant != null && owner != null) {
        Card(
            onClick = {
                onClick(chat)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // TODO maybe just show the not current user name?
                    Text(
                        "${entrant?.name}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${owner?.name}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                val lastMessage = chat.messages.lastOrNull()
                if (lastMessage != null) {
                    val lastSender = if (lastMessage.senderId == chat.entrantId) {
                        entrant?.name
                    } else if (lastMessage.senderId == chat.ownerId) {
                        owner?.name
                    } else {
                        ""
                    }
                    // TODO maybe show with coloured nametag?
                    Text(
                        "$lastSender: ${lastMessage.text}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}