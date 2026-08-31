package com.milasoraki.tokiefy.data

import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.TikTokFeedApi
import com.milasoraki.tokiefy.extractor.api.TikTokWebFeedApi
import com.milasoraki.tokiefy.extractor.model.feed.Aweme
import com.milasoraki.tokiefy.extractor.model.feed.FeedResponse
import com.milasoraki.tokiefy.extractor.remote.OkHttpFactory
import com.milasoraki.tokiefy.extractor.remote.mock.MockData

/**
 * Outcome of a feed fetch, surfaced to the UI so it can show a status chip.
 *
 * @property items       Aweme cards to render.
 * @property source      Where the items came from (for the user-visible label).
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
 * Why it exists:
 * The Home / For-You screen consumes an infinite stream of Aweme objects
 * but should not know anything about which backend to call. The repo
 * decides which endpoint to hit based on session availability:
 *
 *   1. Logged in → try the **web** For-You endpoint first (no X-Argus
 *      needed, works with the cookie the WebView captured).
 *   2. If the web call fails and `isProductionReady == true`, fall back
 *      to the native endpoint (will likely stay blocked until X-Argus
 *      is implemented).
 *   3. If everything fails, return the bundled mock videos and set
 *      [FeedResult.source] to [FeedResult.Source.MOCK] so the UI can
 *      label them clearly instead of pretending they are live.
 */
public class FeedRepository(
    private val feedApi: TikTokFeedApi,
    private val webFeedApi: TikTokWebFeedApi,
) {
    public suspend fun fetchForYou(count: Int = 20): FeedResult {
        val loggedIn = ServiceLocator.sessionManager.isLoggedIn()

        // 1) Authenticated → web endpoint (no signing needed).
        if (loggedIn) {
            val web = runCatching { webFeedApi.forYou(count = count) }
            web.onSuccess { resp ->
                if (resp.awemes.isNotEmpty()) {
                    return FeedResult(resp.awemes, FeedResult.Source.WEB_LIVE)
                }
            }
        }

        // 2) Native endpoint — only useful once X-Argus works.
        if (OkHttpFactory.isProductionReady) {
            val native = runCatching { feedApi.forYou(count = count) }
            native.onSuccess { resp ->
                if (resp.awemes.isNotEmpty()) {
                    return FeedResult(resp.awemes, FeedResult.Source.NATIVE_LIVE)
                }
            }
        }

        // 3) Mock fallback, labelled so the user isn't fooled.
        return FeedResult(
            items = MockData.FEED_ENVELOPE,
            source = FeedResult.Source.MOCK,
            errorMessage = if (loggedIn) {
                "Live feed blocked (missing X-Argus) — showing sample videos."
            } else {
                null
            },
        )
    }
}
