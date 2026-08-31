package com.milasoraki.tokiefy.extractor.api

import com.squareup.moshi.Json
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Retrofit service for follow/unfollow (doc 15.5):
 * `POST /aweme/v1/commit/follow/user/` form-encoded. Real clients always
 * send both `user_id` (numeric) and `sec_user_id` (opaque base64); we
 * pass whatever we have — when we call it from the feed card we only
 * know the numeric id, so `sec_user_id` is left blank.
 */
public interface TikTokRelationApi {
    @FormUrlEncoded
    @POST(TikTokEndpoints.RELATION_FOLLOW_PATH)
    public suspend fun follow(
        @Field("user_id") userId: String,
        @Field("sec_user_id") secUserId: String = "",
        @Field("type") type: Int, // 1 = follow, 0 = unfollow
        @Field("from") from: Int = 1,
        @Field("channel_id") channelId: Int = 0,
        @Field("request_from_type") requestFromType: String = "homepage_hot",
    ): RelationResponse

    public data class RelationResponse(
        @Json(name = "status_code") val statusCode: Int = 0,
        @Json(name = "follow_status") val followStatus: Int = 0,
        @Json(name = "status_msg") val statusMsg: String? = null,
    )
}
