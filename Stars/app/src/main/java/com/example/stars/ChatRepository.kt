/**
 * Class for getting user and owner chats from firebase
 */
package com.example.stars

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

/**
 * Firebase accessor for user direct messages
 */
class ChatRepository(private val scope: CoroutineScope):
    AbstractUserDataRepository<Chat>("chats", scope, Chat::class.java) {

    /**
     * Get a [List] of [Chat]s the user with [userId] is part of
     */
    override suspend fun getWithUserId(userId: String): List<Chat> {
        return collection.whereArrayContains("users", userId).get().await().toObjects(dataClass)
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
}