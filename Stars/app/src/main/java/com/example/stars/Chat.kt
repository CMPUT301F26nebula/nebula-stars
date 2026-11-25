/**
 * Data classes representing a entrant-owner chat and the messages in it
 */
package com.example.stars

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
    companion object UniqueId {
        /**
         * Generates a unique document id from [entrantId] and [ownerId]
         */
        fun uniqueIdFrom(entrantId: String, ownerId: String): String {
            return "${entrantId}_${ownerId}"
        }

        /*
        // TODO for testing, plz remove later
        fun addDummyChat(entrantId: String, ownerId: String) {
            val dummyChat = Chat(
                id = uniqueIdFrom(entrantId, ownerId),
                entrantId = entrantId,
                ownerId = ownerId,
                messages = buildList {
                    for (i in 1..5) {
                        val senderId = if (i % 2 == 0) entrantId else ownerId
                        val msg = "H${"i".repeat(i * 2)}"
                        val timestamp = 1794441600000 + 10000 * i
                        add(
                            Message(
                                id = Message.uniqueIdFrom(
                                    senderId,
                                    msg,
                                    timestamp
                                ),
                                senderId,
                                msg,
                                timestamp
                            )
                        )
                    }
                }
            )
            Databases.chatRepository.addOrUpdate(dummyChat)
        }
         */
    }
}

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
    }
}