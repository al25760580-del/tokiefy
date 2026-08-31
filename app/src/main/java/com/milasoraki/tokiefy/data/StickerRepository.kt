package com.milasoraki.tokiefy.data

import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.TikTokMessagingApi
import com.milasoraki.tokiefy.extractor.model.sticker.StickerStoreResponse

/**
 * Sticker store (packs and recent stickers) repository.
 *
 * Why it exists:
 * Sticker metadata lives on a separate endpoint (`im/sticker/store`) but
 * is only used by the chat screen. The repository is the single point of
 * access so the ViewModel doesn't have to know the endpoint path.
 */
public class StickerRepository(
    private val messagingApi: TikTokMessagingApi = ServiceLocator.messagingApi,
) {
    public suspend fun fetchStore(): StickerStoreResponse {
        return runCatching { messagingApi.stickerStore() }
            .getOrElse { com.milasoraki.tokiefy.extractor.remote.mock.MockData.stickerStore() }
    }
}
