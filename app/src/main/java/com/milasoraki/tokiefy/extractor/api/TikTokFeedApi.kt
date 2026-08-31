package com.milasoraki.tokiefy.extractor.api

import com.milasoraki.tokiefy.extractor.model.feed.FeedResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for the vertical video feed.
 *
 * Split from the rest of the endpoints because feed calls carry distinct
 * query parameters (`count`, `max_cursor`, `feed_type`) and evolve on a
 * different cadence than messaging/relation endpoints.
 */
public interface TikTokFeedApi {
    @GET(TikTokEndpoints.FEED_PATH)
    public suspend fun forYou(
        @Query("count") count: Int = 10,
        @Query("feed_type") feedType: Int = 0,
        @Query("max_cursor") maxCursor: Long = 0,
    ): FeedResponse
}
