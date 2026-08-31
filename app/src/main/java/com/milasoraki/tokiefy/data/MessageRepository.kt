package com.milasoraki.tokiefy.data

import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.TikTokMessagingApi
import com.milasoraki.tokiefy.extractor.model.messaging.DirectMessage
import com.milasoraki.tokiefy.extractor.model.messaging.MessageListResponse
import com.milasoraki.tokiefy.extractor.model.messaging.SendMessageRequest
import com.milasoraki.tokiefy.extractor.model.messaging.SendMessageResponse
import com.milasoraki.tokiefy.extractor.model.messaging.SendStickerRequest

/**
 * Per-conversation message repository.
 *
 * Why it exists:
 * Screens (the chat ViewModel) should not know about request IDs,
 * client_msg_ids, or the shape of the Retrofit envelope. The repository
 * wraps those details and will later own retry + optimistic-state rules.
 */
public class MessageRepository(
    private val messagingApi: TikTokMessagingApi = ServiceLocator.messagingApi,
) {
    /**
     * Fetches messages for the given conversation.
     *
     * @param conversationId stable conversation identifier.
     * @return messages in chronological order (oldest first).
     */
    public suspend fun fetchMessages(conversationId: String): List<DirectMessage> {
        val response: MessageListResponse = runCatching {
            messagingApi.listMessages(conversationId = conversationId)
        }.getOrElse {
            com.milasoraki.tokiefy.extractor.remote.mock.MockData.messages(conversationId)
        }
        return response.messages.sortedBy { it.createTimeEpochSeconds }
    }

    /**
     * Sends a text message.
     *
     * @return server-acknowledged message id once the network call completes.
     */
    public suspend fun sendText(conversationId: String, text: String): SendMessageResponse {
        return messagingApi.sendMessage(
            SendMessageRequest(
                conversationId = conversationId,
                type = "text",
                content = text,
            ),
        )
    }

    /**
     * Sends a sticker message.
     *
     * @return server-acknowledged message id.
     */
    public suspend fun sendSticker(conversationId: String, stickerId: String, stickerUrl: String): SendMessageResponse {
        return messagingApi.sendSticker(
            SendStickerRequest(
                conversationId = conversationId,
                stickerId = stickerId,
                stickerUrl = stickerUrl,
            ),
        )
    }
}
