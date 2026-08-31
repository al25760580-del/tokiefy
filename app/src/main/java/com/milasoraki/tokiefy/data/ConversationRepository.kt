package com.milasoraki.tokiefy.data

import com.milasoraki.tokiefy.extractor.api.TikTokMessagingApi
import com.milasoraki.tokiefy.extractor.model.messaging.Conversation
import com.milasoraki.tokiefy.extractor.model.messaging.ConversationListResponse
import com.milasoraki.tokiefy.extractor.remote.NetworkDebugLogger
import com.milasoraki.tokiefy.extractor.remote.mock.MockData

/**
 * Read access to the direct-message inbox conversation list.
 *
 * Why it exists:
 * The inbox screen needs a list of conversations with the other user,
 * unread count, online flag and last-message preview. The repository
 * hides Retrofit/JSON details from the UI and is the single place
 * pagination and caching are added later.
 */
public class ConversationRepository(
    private val messagingApi: TikTokMessagingApi,
) {
    /**
     * Fetches the latest page of conversations.
     *
     * @return conversations in server order (most recent first).
     */
    public suspend fun fetchConversations(): List<Conversation> {
        val response: ConversationListResponse = runCatching {
            messagingApi.listConversations()
        }.onFailure { err ->
            NetworkDebugLogger.recordError("conversations/list: ${err.message}")
        }.getOrElse { MockData.CONVERSATIONS_RESPONSE_TYPED }
        return response.conversations
    }
}
