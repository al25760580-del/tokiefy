package com.milasoraki.tokiefy.data

import com.milasoraki.tokiefy.extractor.api.TikTokFeedApi
import com.milasoraki.tokiefy.extractor.model.feed.Aweme
import com.milasoraki.tokiefy.extractor.model.feed.FeedResponse
import com.milasoraki.tokiefy.extractor.remote.mock.MockData

/**
 * Vertical video feed repository.
 *
 * Why it exists:
 * The Home / For-You screen consumes an infinite stream of Aweme objects
 * but should not know anything about `max_cursor` pagination or the
 * `feed_type` parameters. The repository handles pagination, error
 * fallback, and (eventually) caching.
 */
public class FeedRepository(
    private val feedApi: TikTokFeedApi,
) {
    public suspend fun fetchForYou(count: Int = 10): List<Aweme> {
        val response: FeedResponse = runCatching {
            feedApi.forYou(count = count)
        }.getOrElse {
            FeedResponse(
                awemes = MockData.FEED_ENVELOPE,
                hasMore = false,
                maxCursor = 0,
            )
        }
        return response.awemes
    }
}
