package com.milasoraki.tokiefy.extractor.model.messaging

import com.milasoraki.tokiefy.extractor.model.user.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * One entry in the DM inbox.
 *
 * @property conversationId  stable conversation identifier.
 * @property otherUser       remote peer (the other side in a 1-to-1 chat).
 * @property lastMessage     most recent message; null if the conversation is empty.
 * @property unreadCount     unread count driving the red badge in the UI.
 * @property isOnline        true when the peer is currently online (green dot).
 * @property lastMsgSeconds  age (in seconds) of the most recent message; used
 *                           by relative-time formatting.
 */
@JsonClass(generateAdapter = true)
public data class Conversation(
    @Json(name = "conversation_id") val conversationId: String = "",
    @Json(name = "user") val otherUser: User? = null,
    @Json(name = "last_message") val lastMessage: DirectMessage? = null,
    @Json(name = "unread_count") val unreadCount: Int = 0,
    @Json(name = "is_online") val isOnline: Boolean = false,
    @Json(name = "last_msg_time") val lastMsgSeconds: Long = 0,
)

/**
 * A single message inside a conversation.
 *
 * Supported `message_type` values (per section 13 of the API docs):
 *  - "text"        plain text.
 *  - "sticker"     sticker from the sticker store.
 *  - "image"       attached image.
 *  - "aweme_share" a video shared inside the chat.
 *  - "system"      informational messages such as "X added you".
 */
@JsonClass(generateAdapter = true)
public data class DirectMessage(
    @Json(name = "message_id") val messageId: String = "",
    @Json(name = "conversation_id") val conversationId: String? = null,
    @Json(name = "sender_uid") val senderUid: String = "",
    @Json(name = "message_type") val type: String = MessageType.TEXT,
    @Json(name = "content") val content: String = "",
    @Json(name = "sticker_id") val stickerId: String? = null,
    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "create_time") val createTimeEpochSeconds: Long = 0,
)

/** Well-known message type constants to avoid scattered magic strings. */
public object MessageType {
    public const val TEXT: String = "text"
    public const val STICKER: String = "sticker"
    public const val IMAGE: String = "image"
    public const val AWEME_SHARE: String = "aweme_share"
}

/** Conversation list response envelope. */
@JsonClass(generateAdapter = true)
public data class ConversationListResponse(
    @Json(name = "conversations") val conversations: List<Conversation> = emptyList(),
    @Json(name = "has_more") val hasMore: Int = 0,
    @Json(name = "cursor") val nextCursor: Long = 0,
)

/** Message history response envelope. */
@JsonClass(generateAdapter = true)
public data class MessageListResponse(
    @Json(name = "messages") val messages: List<DirectMessage> = emptyList(),
    @Json(name = "has_more") val hasMore: Int = 0,
    @Json(name = "cursor") val nextCursor: Long = 0,
)

/** `POST /aweme/v1/im/message/send/` request body for a text message. */
@JsonClass(generateAdapter = true)
public data class SendMessageRequest(
    @Json(name = "conversation_id") val conversationId: String,
    @Json(name = "message_type") val type: String,
    @Json(name = "content") val content: String,
) {
    public constructor(conversationId: String, type: String, content: String, @Suppress("UNUSED_PARAMETER") stickerHack: Unit) :
        this(conversationId, type, content)
}

/** Sticker-send request body. */
@JsonClass(generateAdapter = true)
public data class SendStickerRequest(
    @Json(name = "conversation_id") val conversationId: String,
    @Json(name = "message_type") val type: String = MessageType.STICKER,
    @Json(name = "sticker_id") val stickerId: String,
    @Json(name = "sticker_url") val stickerUrl: String,
)

/** Send response envelope: includes the server-acknowledged message. */
@JsonClass(generateAdapter = true)
public data class SendMessageResponse(
    @Json(name = "message") val message: DirectMessage? = null,
    @Json(name = "status_code") val statusCode: Int = 0,
    @Json(name = "status_msg") val statusMessage: String = "",
)

/** Generic success envelope used by digg/follow/collect endpoints. */
@JsonClass(generateAdapter = true)
public data class ActionResponse(
    @Json(name = "status_code") val statusCode: Int = 0,
    @Json(name = "status_msg") val statusMessage: String = "",
)
