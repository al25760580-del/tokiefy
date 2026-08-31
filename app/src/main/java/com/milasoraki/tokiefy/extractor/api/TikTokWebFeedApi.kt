package com.milasoraki.tokiefy.extractor.api

import com.milasoraki.tokiefy.extractor.model.feed.FeedResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for the *web* For-You feed (`www.tiktok.com/api/recommend/item_list/`).
 *
 * Why a separate interface instead of reusing [TikTokFeedApi]:
 *
 * |                              | Native `api.tiktokv.com` | Web `www.tiktok.com` (this) |
 * |------------------------------|--------------------------|-----------------------------|
 * | Auth                         | Device params + signing  | Browser `sessionid` cookie  |
 * | Requires X-Argus/X-Bogus     | Yes (blocks us today)    | No                          |
 * | Source of our credentials    | Not available            | Provided by WebView login   |
 * | Returns same `aweme_list`    | Yes                      | Yes (compatible JSON)       |
 *
 * Because the WebView grabber produces exactly a browser session, this is
 * the endpoint that will return the user's real feed today without
 * waiting for libmsaoaidsec.so reverse engineering.
 */
public interface TikTokWebFeedApi {
    @GET(TikTokEndpoints.WEB_FYP_PATH)
    public suspend fun forYou(
        @Query("aid") aid: Int = 1988,
        @Query("count") count: Int = 20,
        @Query("cursor") cursor: Long = 0,
        @Query("sourceType") sourceType: Int = 12,
        @Query("itemID") itemId: Long = 1,
    ): FeedResponse
}
