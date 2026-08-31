package com.milasoraki.tokiefy.extractor.api

import com.squareup.moshi.Json
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit service for "digg" (like/unlike) actions.
 *
 * Likes are POSTs with a query-string `action` parameter and an empty
 * body; the server returns a minimal { status_code } envelope that we
 * model with a private response holder.
 */
public interface TikTokDiggApi {
    @POST(TikTokEndpoints.DIGG_PATH)
    public suspend fun digg(
        @Query("aweme_id") awemeId: String,
        @Query("action") action: String, // "digg" | "undigg"
    ): DiggResponse

    public data class DiggResponse(
        @Json(name = "status_code") val statusCode: Int = 0,
    )
}
