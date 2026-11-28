/**
 * Composables for entrant-owner direct messages
 */
package com.example.stars

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// profile picture image consts
private object PFPSize {
    val width = 52.dp
    val height = 52.dp
}

/**
 * Screen for sending and viewing messages
 *
 * @param navController navigation controller
 * @param userProfile current device user
 * @param chatId unique id of the chat to display
 * @param readOnly if true the input bar will be hidden, by default it decides based on whether the current user is a chat participant
 */
@Composable
fun ChatScreen(
    navController: NavController,
    userProfile: UserProfile,
    chatId: String,
    readOnly: Boolean? = null
) {
    var currentChat: Chat by remember { mutableStateOf(Chat()) }
    var canSendMessage by remember { mutableStateOf(false) }

    // cache some values so we dont make a db access for every individual chat message
    var participants: ChatParticipants? by remember { mutableStateOf(null) }

    DisposableEffect(chatId) {
        val chatUpdateListener = Databases.chatRepository.addListenerToId(chatId) { newChat ->
            if (newChat != null && newChat != currentChat) {
                currentChat = newChat
            }
        }

        onDispose {
            chatUpdateListener.remove()
        }
    }

    // ownerId and entrantId of a chat should not change after it is created
    LaunchedEffect(chatId) {
        // if for whatever reason it cant get from db, loading forever
        Databases.chatRepository.getWithId(chatId)?.let { chat ->
            currentChat = chat
            canSendMessage = readOnly?.let {
                !it
            } ?: (userProfile.id in chat.users)
            participants = Databases.chatRepository.getChatParticipants(
                userProfile,
                currentChat,
                { user, uri ->
                    UserProfilePicture(
                        uri,
                        PFPSize.width,
                        user?.name ?: "chatter")
                }
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StarsColors.Background,
        contentColor = StarsColors.OnBackground,
        topBar = {
            ScreenHeader(
                navController = navController,
                title = if (readOnly == true) {
                    "CHAT LOG"
                } else {
                    participants?.remote?.name ?: "CHATTING"
                }
            )
        },
        bottomBar = {
            if (canSendMessage) {
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
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // show loading screen until users have been fetched from db
            participants?.let {
                Spacer(Modifier.weight(1.0f))
                ChatLog(it, currentChat.messages)
            } ?: LoadingText()
        }
    }
}

/**
 * Chat text input field
 *
 * @param onSend callback for when the user sends a message, receives the chat contents and a
 * callback for clearing the [TextField] to prevent clearing before the database updates
 */
@Composable
fun ChatInput(onSend: (String) -> Unit) {
    var messageContent by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(StarsColors.OnBackground).imePadding(),
        verticalAlignment = Alignment.Bottom
    ) {
        TextField(
            value = messageContent,
            onValueChange = {
                messageContent = it
            },
            supportingText = { Text("${messageContent.length}/${ChatLimits.MAX_CHAR_LIMIT}") },
            placeholder = {
                Text(
                    text = "Message",
                    color = StarsColors.OnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Thin
                )
            },
            textStyle = TextStyle(
                color = if (messageContent.length > ChatLimits.MAX_CHAR_LIMIT) StarsColors.Confirm else StarsColors.OnSurface,
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
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),

            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .testTag("messageTextField")
        )

        if (messageContent.length in 1..ChatLimits.MAX_CHAR_LIMIT) {
            Button(
                onClick = {
                    onSend(messageContent)
                    messageContent = ""
                },
                shape = RoundedCornerShape(10.dp, 0.dp, 0.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(StarsColors.Accent1),
                modifier = Modifier
                    .width(PFPSize.width)
                    .height(PFPSize.height)
            ) {
                Text(
                    text = ">",
                    fontSize = 32.sp,
                    color = StarsColors.OnAccent,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // if the message is too long, still fill the space the button would have occupied
            Box(
                modifier = Modifier
                    .width(PFPSize.width)
                    .height(PFPSize.height)
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
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        reverseLayout = true
    ) {
        items(messages.reversed()) { msg ->
            ChatMessage(participants, msg) { dateFormat.format(it) }
        }
    }
}

/**
 * A single chat message
 *
 * @param participants chat participants
 * @param message message to display
 * @param dateFormat callback that takes a timestamp in epoch millis and returns a string to display
 */
@Composable
fun ChatMessage(participants: ChatParticipants, message: Message, dateFormat: (Long) -> String) {
    val senderIsCurrentUser = participants.local?.id == message.senderId
    val senderName = if (senderIsCurrentUser) {
        participants.local.name
    } else {
        participants.remote?.name ?: ""
    }
    val cardColors = RandomStarsColor.cardColorsFromId(message.senderId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (senderIsCurrentUser) Arrangement.End else Arrangement.Start,
    ) {
        // dont show pfp if didnt send message
        if (!senderIsCurrentUser) {
            Box(
                modifier = Modifier
                    .requiredWidth(PFPSize.width)
                    .requiredHeight(PFPSize.height)
            ) {
                participants.remotePFP()
            }
            Spacer(Modifier.width(8.dp))
        }

        val textCardShape = if (senderIsCurrentUser) {
            RoundedCornerShape(10.dp, 0.dp, 10.dp, 10.dp)
        } else {
            RoundedCornerShape(0.dp, 10.dp, 10.dp, 10.dp)
        }

        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = if (senderIsCurrentUser) {
                Alignment.End
            } else {
                Alignment.Start
            },
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.padding(0.dp, 2.dp)
            ) {
                Text(
                    text = senderName,
                    maxLines = 1,
                    softWrap = false,
                    color = StarsColors.OnBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = dateFormat(message.timestamp),
                    maxLines = 1,
                    softWrap = false,
                    color = StarsColors.OnBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Thin
                )
            }
            Card(
                modifier = Modifier
                    .background(
                        cardColors.containerColor,
                        textCardShape
                    )
                    .width(IntrinsicSize.Max),
                colors = cardColors
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = message.text,
                    fontSize = 16.sp
                )
            }
        }

        if (senderIsCurrentUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .requiredWidth(PFPSize.width)
                    .requiredHeight(PFPSize.height)
            ) {
                participants.localPFP()
            }
        }
    }
}

/**
 * Summary card showing the chat recipient and last message if there is one
 *
 * @param participants the [ChatParticipants]
 * @param chat the [Chat]
 * @param adminView if the admin buttons should be visible
 * @param onClick click callback that receives this cards [chat] instance as an argument
 */
@Composable
fun ChatCard(participants: ChatParticipants, chat: Chat, adminView: Boolean = false, onClick: (Chat) -> Unit) {
    Card(
        onClick = {
            onClick(chat)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = if (adminView) {
            CardColors(
                StarsColors.Surface,
                StarsColors.OnSurface,
                StarsColors.Surface,
                StarsColors.OnSurface
            )
        } else {
            participants.remote?.let { remote ->
                RandomStarsColor.cardColorsFromId(remote.id)
            } ?: CardDefaults.cardColors()
        }
    ) {
        val lastMessage = chat.messages.lastOrNull()
        val fuzzyDate = FuzzyDateFormat(LocalLocale.current.platformLocale)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (adminView) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        participants.localPFP()

                        Spacer(Modifier.width(8.dp))

                        participants.local?.let {
                            NameTag(
                                it.name,
                                it.id,
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        participants.remotePFP()

                        Spacer(Modifier.width(8.dp))

                        participants.remote?.let {
                            NameTag(
                                it.name,
                                it.id,
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    participants.remotePFP()

                    Spacer(Modifier.width(8.dp))

                    Text(
                        participants.remote?.name ?: "Unknown",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
                    participants.local?.id -> {
                        participants.local.name
                    }
                    participants.remote?.id -> {
                        participants.remote.name
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
            } ?: Text(
                "No messages",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (adminView) {
                Spacer(Modifier.height(8.dp))
                AdminActionOnChat("Delete Chat") {
                    Databases.chatRepository.removeWithId(chat.id)
                }
            }
        }
    }
}