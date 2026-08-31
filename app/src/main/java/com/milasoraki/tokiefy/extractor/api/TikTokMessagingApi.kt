package com.milasoraki.tokiefy.extractor.api

import com.milasoraki.tokiefy.extractor.model.messaging.ConversationListResponse
import com.milasoraki.tokiefy.extractor.model.messaging.MessageListResponse
import com.milasoraki.tokiefy.extractor.model.messaging.SendMessageRequest
import com.milasoraki.tokiefy.extractor.model.messaging.SendMessageResponse
import com.milasoraki.tokiefy.extractor.model.messaging.SendStickerRequest
import com.milasoraki.tokiefy.extractor.model.sticker.StickerStoreResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit service for direct messaging endpoints.
 *
 * Messaging is kept in its own interface because it carries different
 * headers (`X-CSRFToken`, `Cookie`) than read-only endpoints and the
 * request/response envelopes are JSON-bodied rather than form-encoded.
 * Higher-level split by feature (conversations, messages, stickers)
 * lives in repositories under `data/`.
 */
public interface TikTokMessagingApi {
    @GET(TikTokEndpoints.IM_CONVERSATIONS_PATH)
    public suspend fun listConversations(
        @Query("count") count: Int = 20,
        @Query("cursor") cursor: Long = 0,
    ): ConversationListResponse

    @GET(TikTokEndpoints.IM_MESSAGES_PATH)
    public suspend fun listMessages(
        @Query("conversation_id") conversationId: String,
        @Query("count") count: Int = 50,
        @Query("cursor") cursor: Long = 0,
    ): MessageListResponse

    @POST(TikTokEndpoints.IM_SEND_PATH)
    public suspend fun sendMessage(@Body request: SendMessageRequest): SendMessageResponse

    @POST(TikTokEndpoints.IM_SEND_PATH)
    public suspend fun sendSticker(@Body request: SendStickerRequest): SendMessageResponse

    @GET(TikTokEndpoints.IM_STICKER_STORE_PATH)
    public suspend fun stickerStore(): StickerStoreResponse
}
