package com.milasoraki.tokiefy.extractor.api

import com.squareup.moshi.Json
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Retrofit service for "digg" (like/unlike) actions on the native app
 * endpoint (doc 15.5): `POST /aweme/v1/commit/item/digg/` with
 * `application/x-www-form-urlencoded` body.
 *
 * Note: in 46.2.3 the `type` parameter doubles as on/off (1=like, 0=unlike),
 * which matches what the older `action=digg|undigg` query did; the server
 * accepts both forms. We use the form-encoded body with `type` to match
 * what the real app sends.
 */
public interface TikTokDiggApi {
    @FormUrlEncoded
    @POST(TikTokEndpoints.DIGG_PATH)
    public suspend fun digg(
        @Field("aweme_id") awemeId: String,
        @Field("type") type: Int, // 1 = digg, 0 = undigg
        @Field("digg_type") diggType: Int = 1,
        @Field("channel_id") channelId: Int = 0,
        @Field("enter_from") enterFrom: String = "homepage_hot",
        @Field("item_id") itemId: String = awemeId,
    ): DiggResponse

    public data class DiggResponse(
        @Json(name = "status_code") val statusCode: Int = 0,
        @Json(name = "status_msg") val statusMsg: String? = null,
    )
}
