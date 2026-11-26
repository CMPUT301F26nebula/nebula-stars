/**
 * Composables for entrant-owner direct messages
 */
package com.example.stars

import android.R.attr.textColor
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil3.compose.AsyncImage

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

            // was written before both users got merged into a Pair so thats why theyre named that
            val entrant = Databases.userRepository.getWithId(chat.userIdA())
            val owner = Databases.userRepository.getWithId(chat.userIdB())
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
                    var uri: Uri? = null
                    if (user.profilePic != "") {
                        uri = Databases.imageRepository.getImageUri(user.profilePic)?.toUri()
                    }
                    {
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
                } ?: {},
                remoteUser?.let { user ->
                    var uri: Uri? = null
                    if (user.profilePic != "") {
                        uri = Databases.imageRepository.getImageUri(user.profilePic)?.toUri()
                    }
                    {
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
                } ?: {}
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StarsColors.Purple,
        contentColor = StarsColors.White,
        topBar = {
            ScreenHeader(
                navController = navController,
                title = participants?.remote?.name ?: "CHATTING"
            )
        },
        bottomBar = {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // show loading screen until users have been fetched from db
            if (participants != null) {
                Spacer(Modifier.weight(1.0f))
                ChatLog(participants!!, currentChat.messages)
            } else {
                LoadingText()
            }
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
                messageContent = it
            },
            supportingText = { Text("${messageContent.length}/${ChatLimits.MAX_CHAR_LIMIT}") },
            placeholder = {
                Text(
                    text = "Message",
                    color = StarsColors.Purple,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Thin
                )
            },
            textStyle = TextStyle(
                color = if (messageContent.length > ChatLimits.MAX_CHAR_LIMIT) StarsColors.Magenta else StarsColors.Purple,
                fontSize = 16.sp
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
                    .width(PFPSize.width)
                    .height(PFPSize.height)
            ) {
                Text(
                    text = ">",
                    fontSize = 32.sp,
                    color = StarsColors.White,
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

    val msgColor = RandomStarsColor.fromId(message.senderId)
    val textColor = RandomStarsColor.contrast(msgColor)

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
                    color = StarsColors.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = dateFormat(message.timestamp),
                    maxLines = 1,
                    softWrap = false,
                    color = StarsColors.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Thin
                )
            }
            Card(
                modifier = Modifier
                    .background(
                        msgColor,
                        textCardShape
                    )
                    .width(IntrinsicSize.Max),
                    colors = CardColors(
                        msgColor,
                        textColor,
                        msgColor,
                        textColor
                    )
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