package com.milasoraki.tokiefy.data

import com.milasoraki.tokiefy.extractor.api.TikTokDiggApi
import com.milasoraki.tokiefy.extractor.api.TikTokRelationApi
import com.milasoraki.tokiefy.extractor.remote.NetworkDebugLogger

/**
 * Fires one-off user interactions: like, unlike, save, follow.
 *
 * Failures are logged to [NetworkDebugLogger] but not surfaced to the
 * UI (the optimistically-toggled heart/follow icon stays in its new
 * state; a future retry-on-reconnect pass can reconcile). Until X-Argus
 * signing is implemented these calls will likely return 403, which the
 * debug log will show.
 */
public class InteractionRepository(
    private val diggApi: TikTokDiggApi,
    private val relationApi: TikTokRelationApi,
) {
    public suspend fun like(awemeId: String): Result<TikTokDiggApi.DiggResponse> = runCatching {
        diggApi.digg(awemeId = awemeId, type = 1)
    }.onFailure { NetworkDebugLogger.recordError("like/$awemeId failed: ${it.message}") }

    public suspend fun unlike(awemeId: String): Result<TikTokDiggApi.DiggResponse> = runCatching {
        diggApi.digg(awemeId = awemeId, type = 0)
    }.onFailure { NetworkDebugLogger.recordError("unlike/$awemeId failed: ${it.message}") }

    public suspend fun follow(userId: String, secUserId: String = ""): Result<TikTokRelationApi.RelationResponse> =
        runCatching { relationApi.follow(userId = userId, secUserId = secUserId, type = 1) }
            .onFailure { NetworkDebugLogger.recordError("follow/$userId failed: ${it.message}") }

    public suspend fun unfollow(userId: String, secUserId: String = ""): Result<TikTokRelationApi.RelationResponse> =
        runCatching { relationApi.follow(userId = userId, secUserId = secUserId, type = 0) }
            .onFailure { NetworkDebugLogger.recordError("unfollow/$userId failed: ${it.message}") }
}
