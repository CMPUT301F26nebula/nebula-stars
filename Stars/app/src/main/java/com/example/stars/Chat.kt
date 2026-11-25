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
 * @param id unique firebase document id
 * @param entrantId id of the user in the chat
 * @param ownerId id of the entrant in the chat
 * @param messages list of messages sent in the chat
 */
data class Chat (
    override val id: String = "",
    val entrantId: String = "",
    val ownerId: String = "",
    val messages: List<Message> = emptyList()
): UniqueIdentifier {

    /**
     * Add a [message] to the chat history
     *
     * If this causes the number of messages to exceed [ChatLimits.MAX_HISTORY_LIMIT], the oldest
     * message will be removed
     */
    fun addMessage(message: Message): Chat {
        return Chat(
            id,
            entrantId,
            ownerId,
            messages.takeLast(ChatLimits.MAX_HISTORY_LIMIT - 1) + listOf(message)
        )
    }

    companion object UniqueId {
        /**
         * Generates a unique document id from [entrantId] and [ownerId]
         */
        fun uniqueIdFrom(entrantId: String, ownerId: String): String {
            return "${entrantId}_${ownerId}"
        }

        /**
         * Message factory method
         *
         * Since [uniqueIdFrom] uses parameters from [Chat] to generate a value, this avoids
         * having to write arguments twice
         *
         * @param entrantId id of the user in the chat
         * @param ownerId id of the entrant in the chat
         * @param messages list of messages sent in the chat
         */
        fun new(entrantId: String, ownerId: String, messages: List<Message>): Chat {
            return Chat(
                uniqueIdFrom(entrantId, ownerId),
                entrantId,
                ownerId,
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
 * @param localPFP profile picture of [local]
 * @param remotePFP profile picture of [remote]
 */
data class ChatParticipants(
    val local: UserProfile?,
    val remote: UserProfile?,
    val localPFP: @Composable () -> Unit,   // if it works it works lol
    val remotePFP: @Composable () -> Unit
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