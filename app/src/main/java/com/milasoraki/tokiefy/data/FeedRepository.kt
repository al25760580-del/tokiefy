package com.milasoraki.tokiefy.data

import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.TikTokFeedApi
import com.milasoraki.tokiefy.extractor.api.TikTokWebFeedApi
import com.milasoraki.tokiefy.extractor.model.feed.Aweme
import com.milasoraki.tokiefy.extractor.model.feed.FeedResponse
import com.milasoraki.tokiefy.extractor.remote.NetworkDebugLogger
import com.milasoraki.tokiefy.extractor.remote.OkHttpFactory
import com.milasoraki.tokiefy.extractor.remote.mock.MockData

/**
 * Outcome of a feed fetch, surfaced to the UI so it can show a status chip.
 */
public data class FeedResult(
    val items: List<Aweme>,
    val source: Source,
    val errorMessage: String? = null,
) {
    public enum class Source {
        /** Fetched from the authenticated web endpoint (real data). */
        WEB_LIVE,
        /** Fetched from the native app endpoint (real data, requires signing). */
        NATIVE_LIVE,
        /** Local bundled mock videos (offline/signed-out fallback). */
        MOCK,
    }
}

/**
 * Vertical video feed repository.
 *
 * Resolution order:
 *   1. Logged in → try the web `/api/recommend/item_list/` endpoint,
 *      which works with a browser sessionid cookie and no X-Argus.
 *   2. If `isProductionReady` is enabled and the web call failed /
 *      returned nothing, try the native `/aweme/v1/feed/` endpoint.
 *   3. Otherwise fall back to bundled mocks and label them so the UI
 *      can show the user they are sample data.
 */
public class FeedRepository(
    private val webFeedApi: TikTokWebFeedApi,
    private val nativeFeedApi: TikTokFeedApi,
) {
    public suspend fun fetchForYou(count: Int = 20): FeedResult {
        val loggedIn = ServiceLocator.sessionManager.isLoggedIn()

        if (loggedIn) {
            val web = runCatching { webFeedApi.forYou(count = count) }
            web.onSuccess { resp ->
                if (resp.isSuccess() && resp.awemes.isNotEmpty()) {
                    return FeedResult(resp.awemes, FeedResult.Source.WEB_LIVE)
                }
                NetworkDebugLogger.recordError(
                    "web FYP code=${resp.statusCode}/${resp.errorCode} msg=${resp.message} awemes=${resp.awemes.size}",
                )
            }
            web.onFailure { err ->
                NetworkDebugLogger.recordError("web FYP failed: ${err.message}")
            }
        }

        if (OkHttpFactory.isProductionReady) {
            val native = runCatching { nativeFeedApi.forYou(count = count) }
            native.onSuccess { resp ->
                if (resp.isSuccess() && resp.awemes.isNotEmpty()) {
                    return FeedResult(resp.awemes, FeedResult.Source.NATIVE_LIVE)
                }
                NetworkDebugLogger.recordError(
                    "native FYP code=${resp.statusCode} awemes=${resp.awemes.size}",
                )
            }
            native.onFailure { err ->
                NetworkDebugLogger.recordError("native FYP failed: ${err.message}")
            }
        }

        return FeedResult(
            items = MockData.FEED_ENVELOPE,
            source = FeedResult.Source.MOCK,
            errorMessage = if (loggedIn) {
                "Live feed blocked (X-Argus pending / web call failed) — showing samples. Check the debug log."
            } else {
                null
            },
        )
    }
}
