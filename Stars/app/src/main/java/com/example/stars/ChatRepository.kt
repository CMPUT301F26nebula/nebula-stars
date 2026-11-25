/**
 * Class for getting user and owner chats from firebase
 */
package com.example.stars

import kotlinx.coroutines.CoroutineScope

class ChatRepository(private val scope: CoroutineScope):
    AbstractUserDataRepository<Chat>("chats", scope, Chat::class.java) {

    /**
     * Get a list of chats the user with [userId] is part of
     */
    override suspend fun getWithUserId(userId: String): List<Chat> {
        return getWhereFieldEquals("entrantId", userId) + getWhereFieldEquals("ownerId", userId)
    }
}