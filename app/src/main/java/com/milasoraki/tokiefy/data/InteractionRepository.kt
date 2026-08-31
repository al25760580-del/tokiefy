package com.milasoraki.tokiefy.data

import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.TikTokDiggApi
import com.milasoraki.tokiefy.extractor.api.TikTokRelationApi

/**
 * Fires one-off user interactions: like, unlike, save, follow, share.
 *
 * Why it exists:
 * Grouping fire-and-forget POSTs here keeps ViewModels small and avoids
 * duplicating the optimistic-state patterns (update local state first,
 * reconcile with server response) in every screen.
 */
public class InteractionRepository(
    private val diggApi: TikTokDiggApi = ServiceLocator.diggApi,
    private val relationApi: TikTokRelationApi = ServiceLocator.relationApi,
) {
    public suspend fun like(awemeId: String) {
        runCatching { diggApi.digg(awemeId = awemeId, action = "digg") }
    }

    public suspend fun unlike(awemeId: String) {
        runCatching { diggApi.digg(awemeId = awemeId, action = "undigg") }
    }

    public suspend fun follow(userId: String) {
        runCatching { relationApi.follow(userId = userId, action = 1) }
    }
}
