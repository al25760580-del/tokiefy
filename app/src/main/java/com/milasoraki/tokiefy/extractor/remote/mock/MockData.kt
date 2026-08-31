package com.milasoraki.tokiefy.extractor.remote.mock

import com.milasoraki.tokiefy.extractor.model.messaging.Conversation
import com.milasoraki.tokiefy.extractor.model.messaging.ConversationListResponse
import com.milasoraki.tokiefy.extractor.model.messaging.DirectMessage
import com.milasoraki.tokiefy.extractor.model.messaging.MessageListResponse
import com.milasoraki.tokiefy.extractor.model.messaging.MessageType
import com.milasoraki.tokiefy.extractor.model.sticker.Sticker
import com.milasoraki.tokiefy.extractor.model.sticker.StickerPack
import com.milasoraki.tokiefy.extractor.model.sticker.StickerStoreResponse
import com.milasoraki.tokiefy.extractor.model.user.ImageUrl
import com.milasoraki.tokiefy.extractor.model.user.User

/**
 * Static canned data returned by [MockResponseInterceptor].
 *
 * Why it exists:
 * Until native signing (`X-Argus`/`X-Bogus`/`X-Gorgon`/`Ladon`, per
 * section 04.3 of the API docs) is implemented, the app must stay
 * demo-able. All responses keep the **exact snake_case** shape used on
 * the wire so Moshi adapters exercise the same code path they would
 * against the real backend.
 *
 * Maintenance:
 * When a new field or route is added to the extractor DTOs, add the
 * matching canned response here and keep keys in sync with `@Json`
 * names.
 */
internal object MockData {

    // ------------------------------------------------------------------
    // Typed envelopes consumed by repository fallbacks.
    // Declared FIRST so later `val`s can safely reference them; property
    // initialization inside a Kotlin `object` is top-to-bottom.
    // ------------------------------------------------------------------

    data class ConversationEnvelope(
        val conversations: List<Conversation> = emptyList(),
    )
    data class MessagesEnvelope(
        val messages: List<DirectMessage> = emptyList(),
    )

    private fun user(uid: String, uniqueId: String, nickname: String, followers: Int, avatarSeed: String) = User(
        uid = uid,
        uniqueId = uniqueId,
        nickname = nickname,
        followerCount = followers,
        avatarThumb = ImageUrl(urlList = listOf("https://i.pravatar.cc/200?u=$avatarSeed")),
    )

    private fun dm(id: String, sender: String, type: String, content: String, stickerId: String? = null,
                   mediaUrl: String? = null, timeSec: Long) = DirectMessage(
        messageId = id,
        senderUid = sender,
        type = type,
        content = content,
        stickerId = stickerId,
        mediaUrl = mediaUrl,
        createTimeEpochSeconds = timeSec,
    )

    private fun conv(id: String, user: User, lastMsg: DirectMessage, unread: Int, online: Boolean, lastSec: Long) =
        Conversation(
            conversationId = id,
            otherUser = user,
            lastMessage = lastMsg,
            unreadCount = unread,
            isOnline = online,
            lastMsgSeconds = lastSec,
        )

    val CONVERSATIONS_ENVELOPE: ConversationEnvelope = ConversationEnvelope(
        conversations = listOf(
            conv(
                id = "c_mielo",
                user = user("u_mielo", "MieloVT", "MieloVT", 455, "mielo"),
                lastMsg = dm("m_share1", "u_mielo", MessageType.AWEME_SHARE, "aweme:mielo1", timeSec = 120),
                unread = 0, online = true, lastSec = 120,
            ),
            conv(
                id = "c_misky",
                user = user("u_misky", "misky", "` ｡", 87, "misky"),
                lastMsg = dm("m_misky1", "u_misky", MessageType.AWEME_SHARE, "shared a video", timeSec = 7200),
                unread = 0, online = false, lastSec = 7200,
            ),
            conv(
                id = "c_ray",
                user = user("u_ray", "ray", "ray", 12, "ray"),
                lastMsg = dm("m_ray1", "u_ray", MessageType.TEXT, "Active today", timeSec = 14400),
                unread = 0, online = false, lastSec = 14400,
            ),
            conv(
                id = "c_joak",
                user = user("u_joak", "joakoqwaanw1", "joakoqwaanw1", 3, "joak"),
                lastMsg = dm("m_joak1", "u_joak", MessageType.TEXT, "Seen", timeSec = 28800),
                unread = 0, online = false, lastSec = 28800,
            ),
            conv(
                id = "c_dheyns",
                user = user("u_dheyns", "Dheyns", "Dheyns", 230, "dheyns"),
                lastMsg = dm("m_dheyns1", "u_dheyns", MessageType.AWEME_SHARE, "shared a video", timeSec = 75600),
                unread = 2, online = false, lastSec = 75600,
            ),
            conv(
                id = "c_ice",
                user = user("u_ice", "ICE", "ICE", 512, "ice"),
                lastMsg = dm("m_ice1", "u_ice", MessageType.TEXT, "Liked a post you commented on", timeSec = 90000),
                unread = 1, online = false, lastSec = 90000,
            ),
        ),
    )

    val CONVERSATIONS_RESPONSE_TYPED: ConversationListResponse =
        ConversationListResponse(conversations = CONVERSATIONS_ENVELOPE.conversations)

    fun conversations(): ConversationEnvelope = CONVERSATIONS_ENVELOPE

    fun messages(conversationId: String): MessagesEnvelope = MessagesEnvelope(
        messages = listOf(
            dm("${conversationId}_1", conversationId, MessageType.AWEME_SHARE, "aweme:share1", timeSec = 7200),
            dm("${conversationId}_2", "me", MessageType.TEXT, "Haha what's up", timeSec = 3600),
            DirectMessage(
                messageId = "${conversationId}_3",
                conversationId = conversationId,
                senderUid = conversationId,
                type = MessageType.STICKER,
                content = "https://i.pravatar.cc/200?sticker=1",
                stickerId = "stk_boom",
                createTimeEpochSeconds = 600,
            ),
        ),
    )

    fun messagesTyped(conversationId: String): MessageListResponse =
        MessageListResponse(messages = messages(conversationId).messages)

    val STICKER_ENVELOPE: StickerStoreResponse = StickerStoreResponse(
        recentStickers = listOf(
            Sticker("stk_boom", "boom", "https://i.pravatar.cc/200?sticker=1"),
            Sticker("stk_cat", "cat", "https://i.pravatar.cc/200?sticker=2"),
            Sticker("stk_pizza", "pizza", "https://i.pravatar.cc/200?sticker=4"),
            Sticker("stk_ham", "hamster", "https://i.pravatar.cc/200?sticker=5"),
        ),
        savedPacks = listOf(
            StickerPack(
                packId = "p_recent",
                name = "Recent",
                coverUrl = "https://i.pravatar.cc/100?sticker=1",
                stickers = listOf(
                    Sticker("stk_boom", "boom", "https://i.pravatar.cc/200?sticker=1"),
                    Sticker("stk_cat", "cat", "https://i.pravatar.cc/200?sticker=2"),
                    Sticker("stk_pizza", "pizza", "https://i.pravatar.cc/200?sticker=4"),
                    Sticker("stk_ham", "hamster", "https://i.pravatar.cc/200?sticker=5"),
                ),
            ),
        ),
    )

    fun stickerStore(): StickerStoreResponse = STICKER_ENVELOPE

    // ------------------------------------------------------------------
    // JSON strings returned over the wire by MockResponseInterceptor.
    // These must keep the exact snake_case keys Moshi expects, including
    // fields (sec_uid, etc.) the real server sends even though our DTOs
    // don't consume them.
    // ------------------------------------------------------------------

    const val CONVERSATIONS_RESPONSE: String = """
    {
      "conversations":[
        {"conversation_id":"c_mielo","unread_count":0,"is_online":true,"last_msg_time":120,
          "user":{"uid":"u_mielo","unique_id":"MieloVT","nickname":"MieloVT","sec_uid":"sec_mielo",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=mielo"]},"follower_count":455},
          "last_message":{"message_id":"m_share1","sender_uid":"u_mielo","message_type":"aweme_share",
            "content":"aweme:mielo1","create_time":120}},
        {"conversation_id":"c_misky","unread_count":0,"is_online":false,"last_msg_time":7200,
          "user":{"uid":"u_misky","unique_id":"misky","nickname":"` ｡","sec_uid":"sec_misky",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=misky"]},"follower_count":87},
          "last_message":{"message_id":"m_misky1","sender_uid":"u_misky","message_type":"aweme_share",
            "content":"shared a video","create_time":7200}},
        {"conversation_id":"c_ray","unread_count":0,"is_online":false,"last_msg_time":14400,
          "user":{"uid":"u_ray","unique_id":"ray","nickname":"ray","sec_uid":"sec_ray",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=ray"]},"follower_count":12},
          "last_message":{"message_id":"m_ray1","sender_uid":"u_ray","message_type":"text",
            "content":"Active today","create_time":14400}},
        {"conversation_id":"c_joak","unread_count":0,"is_online":false,"last_msg_time":28800,
          "user":{"uid":"u_joak","unique_id":"joakoqwaanw1","nickname":"joakoqwaanw1","sec_uid":"sec_joak",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=joak"]},"follower_count":3},
          "last_message":{"message_id":"m_joak1","sender_uid":"u_joak","message_type":"text",
            "content":"Seen","create_time":28800}},
        {"conversation_id":"c_dheyns","unread_count":2,"is_online":false,"last_msg_time":75600,
          "user":{"uid":"u_dheyns","unique_id":"Dheyns","nickname":"Dheyns","sec_uid":"sec_dheyns",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=dheyns"]},"follower_count":230},
          "last_message":{"message_id":"m_dheyns1","sender_uid":"u_dheyns","message_type":"aweme_share",
            "content":"shared a video","create_time":75600}},
        {"conversation_id":"c_ice","unread_count":1,"is_online":false,"last_msg_time":90000,
          "user":{"uid":"u_ice","unique_id":"ICE","nickname":"ICE","sec_uid":"sec_ice",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=ice"]},"follower_count":512},
          "last_message":{"message_id":"m_ice1","sender_uid":"u_ice","message_type":"text",
            "content":"Liked a post you commented on","create_time":90000}}
      ],
      "has_more":0,"cursor":0
    }
    """

    fun messagesFor(conversationId: String): String = """
    {
      "messages":[
        {"message_id":"${conversationId}_1","conversation_id":"$conversationId",
          "sender_uid":"$conversationId","message_type":"aweme_share","content":"aweme:share1",
          "create_time":7200},
        {"message_id":"${conversationId}_2","conversation_id":"$conversationId",
          "sender_uid":"me","message_type":"text","content":"Haha what's up",
          "create_time":3600},
        {"message_id":"${conversationId}_3","conversation_id":"$conversationId",
          "sender_uid":"$conversationId","message_type":"sticker","sticker_id":"stk_boom",
          "content":"https://i.pravatar.cc/200?sticker=1","create_time":600}
      ],
      "has_more":0,"cursor":0
    }
    """

    const val STICKERS_RESPONSE: String = """
    {
      "recent_stickers":[
        {"sticker_id":"stk_boom","url":"https://i.pravatar.cc/200?sticker=1","name":"boom"},
        {"sticker_id":"stk_cat","url":"https://i.pravatar.cc/200?sticker=2","name":"cat"},
        {"sticker_id":"stk_pizza","url":"https://i.pravatar.cc/200?sticker=4","name":"pizza"},
        {"sticker_id":"stk_ham","url":"https://i.pravatar.cc/200?sticker=5","name":"hamster"}
      ],
      "saved_packs":[
        {"pack_id":"p_recent","name":"Recent","cover_url":"https://i.pravatar.cc/100?sticker=1",
          "stickers":[
            {"sticker_id":"stk_boom","url":"https://i.pravatar.cc/200?sticker=1","name":"boom"},
            {"sticker_id":"stk_cat","url":"https://i.pravatar.cc/200?sticker=2","name":"cat"},
            {"sticker_id":"stk_pizza","url":"https://i.pravatar.cc/200?sticker=4","name":"pizza"},
            {"sticker_id":"stk_ham","url":"https://i.pravatar.cc/200?sticker=5","name":"hamster"}
          ]
        },
        {"pack_id":"p_saved","name":"Saved","cover_url":"https://i.pravatar.cc/100?sticker=6",
          "stickers":[
            {"sticker_id":"stk_cat2","url":"https://i.pravatar.cc/200?sticker=7","name":"cat2"},
            {"sticker_id":"stk_cat3","url":"https://i.pravatar.cc/200?sticker=8","name":"cat3"},
            {"sticker_id":"stk_cat4","url":"https://i.pravatar.cc/200?sticker=9","name":"cat4"}
          ]
        }
      ]
    }
    """

    const val FEED_RESPONSE: String = """
    {
      "aweme_list":[
        {"aweme_id":"aw_fake1","desc":"#fyp fun moment 🔥","create_time":0,
          "author":{"uid":"a1","unique_id":"creator_01","nickname":"Creator 01",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?img=13"]},"follower_count":420},
          "video":{"play_addr":{"url_list":[]},
                  "cover":{"url_list":["https://picsum.photos/seed/t1/400/700"]}},
          "statistics":{"digg_count":12340,"comment_count":560,"share_count":780,
                        "collect_count":220,"play_count":980000}
        }
      ],
      "has_more":0,"max_cursor":0
    }
    """

    const val OK: String = """{"status_code":0,"status_msg":"ok"}"""

    fun sentMessageResponse(requestJson: String): String = """
    {"status_code":0,"status_msg":"ok","message":{
        "message_id":"local_${System.currentTimeMillis()}",
        "sender_uid":"me","message_type":"text","content":${escapeJsonForResponse(requestJson)},"create_time":0
    }}
    """
}

/** Escapes the input JSON text so it can be embedded as a string value. */
private fun escapeJsonForResponse(raw: String): String {
    val escaped = raw
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
    return "\"$escaped\""
}
