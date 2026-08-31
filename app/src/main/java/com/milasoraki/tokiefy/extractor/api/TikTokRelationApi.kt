package com.milasoraki.tokiefy.extractor.api

import com.squareup.moshi.Json
import retrofit2.http.POST
import retrofit2.http.Query

/** Retrofit service for follow/unfollow actions. */
public interface TikTokRelationApi {
    @POST(TikTokEndpoints.RELATION_FOLLOW_PATH)
    public suspend fun follow(
        @Query("user_id") userId: String,
        @Query("type") action: Int, // 1 = follow, 0 = unfollow
    ): RelationResponse

    public data class RelationResponse(
        @Json(name = "status_code") val statusCode: Int = 0,
        @Json(name = "follow_status") val followStatus: Int = 0,
    )
}
