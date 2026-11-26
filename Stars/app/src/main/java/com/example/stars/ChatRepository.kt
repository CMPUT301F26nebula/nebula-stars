/**
 * Class for getting user and owner chats from firebase
 */
package com.example.stars

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

/**
 * Firebase accessor for user direct messages
 */
class ChatRepository(private val scope: CoroutineScope):
    AbstractUserDataRepository<Chat>("chats", scope, Chat::class.java) {

    val userRepository = UserRepository(scope)
    val imageRepository = ImageRepository(scope)

    /**
     * Get a [List] of [Chat]s the user with [userId] is part of
     */
    override suspend fun getWithUserId(userId: String): List<Chat> {
        return collection
            .whereArrayContains("users", userId)
            .get().await().toObjects(dataClass)
    }

    /**
     * Gets the [Chat] between a [Pair] of [users] or creates one if it does not exist
     */
    suspend fun getOrCreate(users: Pair<UserProfile, UserProfile>): Chat {
        getWithId(Chat.uniqueIdFrom(users))?.let {
            return@getOrCreate it
        }
        // yknow what if theres anything kotlin got right its the Pair `to'
        val newChat = Chat.new(users)
        addOrUpdate(newChat)
        return newChat
    }

    /**
     * Gets [ChatParticipants] based on the [currentUser]
     *
     * @param currentUser device user
     * @param chat [Chat] to get participants from
     * @param imageHandler composable that renders a profile picture
     */
    suspend fun getChatParticipants(
        currentUser: UserProfile,
        chat: Chat,
        imageHandler: @Composable ((UserProfile?, Uri?) -> Unit)
    ): ChatParticipants {
        val userA = userRepository.getWithId(chat.userIdA())
        val userB = userRepository.getWithId(chat.userIdB())
        var pfpA: Uri? = null
        var pfpB: Uri? = null
        userA?.let {
            pfpA = imageRepository.getImageUri(it.profilePic)?.toUri()
        }
        userB?.let {
            pfpB = imageRepository.getImageUri(it.profilePic)?.toUri()
        }

        if (currentUser.id == userB?.id) {
            return ChatParticipants(
                local = userB,
                remote = userA,
                localPFP = { imageHandler(userB, pfpB) },
                remotePFP = { imageHandler(userA, pfpA) }
            )
        }
        return ChatParticipants(
            local = userA,
            remote = userB,
            localPFP = { imageHandler(userA, pfpA) },
            remotePFP = { imageHandler(userB, pfpB) }
        )
    }
}