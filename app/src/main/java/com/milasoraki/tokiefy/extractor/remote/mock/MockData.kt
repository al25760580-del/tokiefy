package com.milasoraki.tokiefy.extractor.remote.mock

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

    const val CONVERSATIONS_RESPONSE: String = """
    {
      "conversations":[
        {
          "conversation_id":"c_mielo","unread_count":0,"is_online":true,"last_msg_time":120,
          "user":{"uid":"u_mielo","unique_id":"MieloVT","nickname":"MieloVT","sec_uid":"sec_mielo",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=mielo"]},"follower_count":455},
          "last_message":{"message_id":"m_share1","sender_uid":"u_mielo","message_type":"aweme_share",
            "content":"aweme:mielo1","create_time":120}
        },
        {
          "conversation_id":"c_misky","unread_count":0,"is_online":false,"last_msg_time":7200,
          "user":{"uid":"u_misky","unique_id":"misky","nickname":"` ｡","sec_uid":"sec_misky",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=misky"]},"follower_count":87},
          "last_message":{"message_id":"m_misky1","sender_uid":"u_misky","message_type":"aweme_share",
            "content":"shared a video","create_time":7200}
        },
        {
          "conversation_id":"c_ray","unread_count":0,"is_online":false,"last_msg_time":14400,
          "user":{"uid":"u_ray","unique_id":"ray","nickname":"ray","sec_uid":"sec_ray",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=ray"]},"follower_count":12},
          "last_message":{"message_id":"m_ray1","sender_uid":"u_ray","message_type":"text",
            "content":"Active today","create_time":14400}
        },
        {
          "conversation_id":"c_joak","unread_count":0,"is_online":false,"last_msg_time":28800,
          "user":{"uid":"u_joak","unique_id":"joakoqwaanw1","nickname":"joakoqwaanw1","sec_uid":"sec_joak",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=joak"]},"follower_count":3},
          "last_message":{"message_id":"m_joak1","sender_uid":"u_joak","message_type":"text",
            "content":"Seen","create_time":28800}
        },
        {
          "conversation_id":"c_dheyns","unread_count":2,"is_online":false,"last_msg_time":75600,
          "user":{"uid":"u_dheyns","unique_id":"Dheyns","nickname":"Dheyns","sec_uid":"sec_dheyns",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=dheyns"]},"follower_count":230},
          "last_message":{"message_id":"m_dheyns1","sender_uid":"u_dheyns","message_type":"aweme_share",
            "content":"shared a video","create_time":75600}
        },
        {
          "conversation_id":"c_ice","unread_count":1,"is_online":false,"last_msg_time":90000,
          "user":{"uid":"u_ice","unique_id":"ICE","nickname":"ICE","sec_uid":"sec_ice",
            "avatar_thumb":{"url_list":["https://i.pravatar.cc/200?u=ice"]},"follower_count":512},
          "last_message":{"message_id":"m_ice1","sender_uid":"u_ice","message_type":"text",
            "content":"Liked a post you commented on","create_time":90000}
        }
      ],
      "has_more":0,
      "cursor":0
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

    fun conversations(): ConversationEnvelope = CONVERSATIONS_ENVELOPE
    fun messages(conversationId: String): MessagesEnvelope = MessagesEnvelope(
        messages = listOf(
            com.milasoraki.tokiefy.extractor.model.messaging.DirectMessage(
                messageId = "${conversationId}_1",
                conversationId = conversationId,
                senderUid = conversationId,
                type = com.milasoraki.tokiefy.extractor.model.messaging.MessageType.AWEME_SHARE,
                content = "aweme:share1",
                createTimeEpochSeconds = 7200,
            ),
            com.milasoraki.tokiefy.extractor.model.messaging.DirectMessage(
                messageId = "${conversationId}_2",
                conversationId = conversationId,
                senderUid = "me",
                type = com.milasoraki.tokiefy.extractor.model.messaging.MessageType.TEXT,
                content = "Haha what's up",
                createTimeEpochSeconds = 3600,
            ),
            com.milasoraki.tokiefy.extractor.model.messaging.DirectMessage(
                messageId = "${conversationId}_3",
                conversationId = conversationId,
                senderUid = conversationId,
                type = com.milasoraki.tokiefy.extractor.model.messaging.MessageType.STICKER,
                stickerId = "stk_boom",
                content = "https://i.pravatar.cc/200?sticker=1",
                createTimeEpochSeconds = 600,
            ),
        ),
    )
    fun stickerStore(): com.milasoraki.tokiefy.extractor.model.sticker.StickerStoreResponse = STICKER_ENVELOPE

    // Wire envelopes below are returned by MockResponseInterceptor (JSON)
    // and must match the `@Json` names exactly. Typed envelopes above feed
    // repositories when Moshi is bypassed by a getOrElse fallback.
    data class ConversationEnvelope(
        val conversations: List<com.milasoraki.tokiefy.extractor.model.messaging.Conversation> = emptyList(),
    )
    data class MessagesEnvelope(
        val messages: List<com.milasoraki.tokiefy.extractor.model.messaging.DirectMessage> = emptyList(),
    )

    val CONVERSATIONS_ENVELOPE: ConversationEnvelope = ConversationEnvelope()
    val STICKER_ENVELOPE: com.milasoraki.tokiefy.extractor.model.sticker.StickerStoreResponse =
        com.milasoraki.tokiefy.extractor.model.sticker.StickerStoreResponse(
            recentStickers = listOf(
                com.milasoraki.tokiefy.extractor.model.sticker.Sticker("stk_boom", "boom", "https://i.pravatar.cc/200?sticker=1"),
                com.milasoraki.tokiefy.extractor.model.sticker.Sticker("stk_cat", "cat", "https://i.pravatar.cc/200?sticker=2"),
                com.milasoraki.tokiefy.extractor.model.sticker.Sticker("stk_pizza", "pizza", "https://i.pravatar.cc/200?sticker=4"),
                com.milasoraki.tokiefy.extractor.model.sticker.Sticker("stk_ham", "hamster", "https://i.pravatar.cc/200?sticker=5"),
            ),
            savedPacks = listOf(
                com.milasoraki.tokiefy.extractor.model.sticker.StickerPack(
                    packId = "p_recent",
                    name = "Recent",
                    coverUrl = "https://i.pravatar.cc/100?sticker=1",
                    stickers = listOf(
                        com.milasoraki.tokiefy.extractor.model.sticker.Sticker("stk_boom", "boom", "https://i.pravatar.cc/200?sticker=1"),
                        com.milasoraki.tokiefy.extractor.model.sticker.Sticker("stk_cat", "cat", "https://i.pravatar.cc/200?sticker=2"),
                        com.milasoraki.tokiefy.extractor.model.sticker.Sticker("stk_pizza", "pizza", "https://i.pravatar.cc/200?sticker=4"),
                        com.milasoraki.tokiefy.extractor.model.sticker.Sticker("stk_ham", "hamster", "https://i.pravatar.cc/200?sticker=5"),
                    ),
                ),
            ),
        )

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
