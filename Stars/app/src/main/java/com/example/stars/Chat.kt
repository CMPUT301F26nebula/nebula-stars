/**
 * Data classes representing a entrant-owner chat and the messages in it
 */
package com.example.stars

import androidx.compose.runtime.Composable

/**
 * Consts for chats and messages
 *
 * If i put this in the data class it gets added to firebase which we dont want
 */
object ChatLimits {
    // a new message past this number should remove the oldest message from the database
    val MAX_HISTORY_LIMIT: Int get() = 1000

    // largest number of chars in a message allowed
    val MAX_CHAR_LIMIT: Int get() = 255
}

/**
 * Representation of a chat between an entrant and an owner
 *
 * The [id] parameter exists so firebase can create it from a db query, but all new chats should be
 * created with [Chat.new] to ensure the id format is consistent and replicatable for any pair of
 * users
 *
 * @param id unique firebase document id
 * @param users the chat participants
 * @param messages list of messages sent in the chat
 */
data class Chat (
    override val id: String = "",
    val users: List<String> = emptyList(),  // firebase array is more searchable than map,
    val messages: List<Message> = emptyList()
): UniqueIdentifier {

    /**
     * Gets the of unique id [String] of one of the chat participants
     *
     * If both users have different ids, [userIdA] and [userIdB] will return different values
     */
    fun userIdA(): String {
        return users.getOrElse(0) { "" }
    }

    /**
     * Gets the of unique id [String] of the other chat participant
     *
     * If both users have different ids, [userIdA] and [userIdB] will return different values
     */
    fun userIdB(): String {
        return users.getOrElse(1) { "" }
    }

    /**
     * Add a [message] to the chat history
     *
     * If this causes the number of messages to exceed [ChatLimits.MAX_HISTORY_LIMIT], the oldest
     * message will be removed
     */
    fun addMessage(message: Message): Chat {
        return Chat(
            id,
            users,
            messages.takeLast(ChatLimits.MAX_HISTORY_LIMIT - 1) + listOf(message)
        )
    }

    companion object UniqueId {
        /**
         * Generates a unique document id from its [users]
         */
        fun uniqueIdFrom(users: Pair<UserProfile, UserProfile>): String {
            // chat id should be consistent so any given pair of users should have at most 1 chat
            val ordered = if (users.first.id > users.second.id) {
                users.second to users.first
            } else {
                users
            }
            return "${ordered.first.id}_${ordered.second.id}"
        }

        /**
         * Message factory method
         *
         * Since [uniqueIdFrom] uses parameters from [Chat] to generate a value, this avoids
         * having to write arguments twice
         *
         * @param users the chat participants
         * @param messages list of messages sent in the chat, empty by default
         */
        fun new(users: Pair<UserProfile, UserProfile>, messages: List<Message> = emptyList()): Chat {
            return Chat(
                uniqueIdFrom(users),
                listOf(users.first.id, users.second.id),
                messages
            )
        }
    }
}

/**
 * The participants in a chat should not change after it is created, so name and profile picture
 * information can be cached
 *
 * @param local the user on the device
 * @param remote the user not on the device
 * @param localPFP profile picture of the [local] user
 * @param remotePFP profile picture of the [remote] user
 */
data class ChatParticipants(
    val local: UserProfile?,
    val remote: UserProfile?,
    val localPFP: @Composable (() -> Unit),   // if it works it works lol
    val remotePFP: @Composable (() -> Unit)
)

/**
 * Representation of a message in a chat
 *
 * @param id unique firebase document id
 * @param senderId user id of the user who sent the message
 * @param text content of the message
 * @param timestamp when the message was sent, in millis from epoch
 */
data class Message (
    override val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
): UniqueIdentifier {

    companion object UniqueId {
        /**
         * Generates a unique document id from [senderId], [text], and [timestamp]
         */
        fun uniqueIdFrom(senderId: String, text: String, timestamp: Long): String {
            // if two messages are sent within 1ms then it shouldnt overwrite the earlier one
            // well, it still will if the message content is the same but thats less of an issue
            return "${senderId}_${text.hashCode()}_${timestamp}"
        }

        /**
         * Message factory method
         *
         * Since [uniqueIdFrom] uses parameters from [Message] to generate a value, this avoids
         * having to write arguments twice
         *
         * @param senderId user id of the user who sent the message
         * @param text content of the message
         * @param timestamp when the message was sent, in millis from epoch
         */
        fun new(senderId: String, text: String, timestamp: Long): Message {
            return Message(
                uniqueIdFrom(senderId, text, timestamp),
                senderId,
                text,
                timestamp
            )
        }
    }
}