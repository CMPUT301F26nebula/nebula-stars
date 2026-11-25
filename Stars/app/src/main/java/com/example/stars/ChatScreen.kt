/**
 * Composables for entrant-owner direct messages
 */
package com.example.stars

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil3.compose.AsyncImage

// profile picture image consts
private object PFPSize {
    val width = 64.dp
    val height = 64.dp
}

/**
 * Screen for sending and viewing messages
 *
 * @param navController navigation controller
 * @param userProfile current device user
 * @param chatId unique id of the chat to display
 */
@Composable
fun ChatScreen(
    navController: NavController,
    userProfile: UserProfile,
    chatId: String
) {
    var currentChat: Chat by remember { mutableStateOf(Chat()) }

    // cache some values so we dont make a db access for every individual chat message
    var participants: ChatParticipants? by remember { mutableStateOf(null) }

    // ownerId and entrantId of a chat should not change after it is created
    LaunchedEffect(Unit) {
        Log.d("chats", chatId)
        // if for whatever reason it cant get from db, loading forever
        Databases.chatRepository.getWithId(chatId)?.let { chat ->
            Log.d("chats", "$chat")

            currentChat = chat

            val entrant = Databases.userRepository.getWithId(chat.entrantId)
            val owner = Databases.userRepository.getWithId(chat.ownerId)
            var localUser: UserProfile? = null
            var remoteUser: UserProfile? = null

            // figure out who should go on what side
            if (entrant?.id == userProfile.id) {
                localUser = entrant
                remoteUser = owner
            } else if (owner?.id == userProfile.id) {
                localUser = owner
                remoteUser = entrant
            }

            // feels like some real code abuse here lol
            participants = ChatParticipants(
                localUser,
                remoteUser,
                localUser?.let { user ->
                    Databases.imageRepository.getImageUri(user.profilePic)?.toUri()?.let { uri ->
                        @Composable {
                            AsyncImage(
                                model = uri,
                                contentDescription = "users profile picture",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(PFPSize.width, PFPSize.height),
                                contentScale = ContentScale.Crop,
                                fallback = painterResource(R.drawable.default_pfp),
                                error = painterResource(R.drawable.default_pfp)
                            )
                        }
                    }
                } ?: {},
                localUser?.let { user ->
                    Databases.imageRepository.getImageUri(user.profilePic)?.toUri()?.let { uri ->
                        @Composable {
                            AsyncImage(
                                model = uri,
                                contentDescription = "other users profile picture",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(PFPSize.width, PFPSize.height),
                                contentScale = ContentScale.Crop,
                                fallback = painterResource(R.drawable.default_pfp),
                                error = painterResource(R.drawable.default_pfp)
                            )
                        }
                    }
                } ?: {}
            )
        }
    }

    DisposableEffect(Unit) {
        val chatUpdateListener = Databases.chatRepository.addListenerToId(currentChat.id) { newChat ->
            if (newChat != null && newChat != currentChat) {
                currentChat = newChat
            }
        }

        onDispose {
            chatUpdateListener.remove()
        }
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
            title = participants?.remote?.name ?: "CHATTING"
        )

        Spacer(Modifier.height(12.dp))

        // show loading screen until users have been fetched from db
        if (participants != null) {
            ChatLog(participants!!, currentChat.messages)
            ChatInput { msg ->
                participants?.local?.let { sender ->
                    val currentTime = System.currentTimeMillis()
                    val newMessage = Message.new(
                        sender.id,
                        msg,
                        currentTime
                    )
                    val newChat = currentChat.addMessage(newMessage)

                    // the chat will recompose when the database gets the message, so there might be some lag
                    Databases.chatRepository.addOrUpdate(newChat)
                }
            }
        } else {
            LoadingText()
        }
    }
}

/**
 * Chat text input field
 *
 * @param onSend callback for when the user sends a message
 */
@Composable
fun ChatInput(onSend: (String) -> Unit) {
    var messageContent by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(StarsColors.White),
        verticalAlignment = Alignment.Bottom
    ) {
        TextField(
            value = messageContent,
            onValueChange = {
                if (messageContent.length <= ChatLimits.MAX_CHAR_LIMIT) {
                    messageContent = it
                }
            },
            supportingText = { Text("${messageContent.length}/${ChatLimits.MAX_CHAR_LIMIT}") },
            placeholder = {
                Text(
                    text = "Message",
                    color = StarsColors.Purple,
                    fontSize = 20.sp
                )
            },
            textStyle = TextStyle(
                color = if (messageContent.length > ChatLimits.MAX_CHAR_LIMIT) StarsColors.Magenta else StarsColors.Purple,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = StarsColors.Purple,
                unfocusedIndicatorColor = StarsColors.Purple.copy(alpha = 0.6f),
                cursorColor = StarsColors.Purple
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .testTag("messageTextField")
        )

        if (messageContent.length <= ChatLimits.MAX_CHAR_LIMIT) {
            Button(
                onClick = {
                    onSend(messageContent)
                    messageContent = ""
                },
                shape = RoundedCornerShape(10.dp, 0.dp, 0.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(StarsColors.Cyan),
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
            ) {
                Text(
                    text = ">",
                    color = StarsColors.White,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // if the message is too long, still fill the space the button would have occupied
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .background(StarsColors.Transparent)
            ) {}
        }
    }
}

/**
 * List of chat messages
 *
 * @param participants chat participants
 * @param messages list of messages to show
 */
@Composable
fun ChatLog(participants: ChatParticipants, messages: List<Message>) {
    val dateFormat = FuzzyDateFormat(LocalLocale.current.platformLocale)
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        reverseLayout = true
    ) {
        items(messages) {
            ChatMessage(participants, it, dateFormat)
        }
    }
}

/**
 * A single chat message
 *
 * @param participants chat participants
 * @param message message to display
 * @param dateFormat used to format the message timestamp
 */
@Composable
fun ChatMessage(participants: ChatParticipants, message: Message, dateFormat: FuzzyDateFormat) {
    val senderIsCurrentUser = participants.local?.id == message.senderId

    val senderName = if (senderIsCurrentUser) {
        participants.local.name
    } else {
        participants.remote?.name ?: "null"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (senderIsCurrentUser) Arrangement.End else Arrangement.Start,
    ) {
        // dont show pfp if didnt send message
        if (!senderIsCurrentUser) {
            Box(
                modifier = Modifier
                    .width(PFPSize.width)
                    .height(PFPSize.height)
            ) {
                participants.remotePFP
            }
        }

        val textCardShape = if (senderIsCurrentUser) {
            RoundedCornerShape(10.dp, 0.dp, 10.dp, 10.dp)
        } else {
            RoundedCornerShape(0.dp, 10.dp, 10.dp, 10.dp)
        }

        Card(
            modifier = Modifier
                .padding(12.dp)
                .background(
                    StarsColors.White,
                    textCardShape
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(senderName)
                    Text(dateFormat.format(message.timestamp))
                }
                Spacer(Modifier.height(8.dp))
                Text(message.text)
            }
        }

        if (senderIsCurrentUser) {
            Box(
                modifier = Modifier
                    .width(PFPSize.width)
                    .height(PFPSize.height)
            ) {
                participants.localPFP
            }
        }
    }
}

