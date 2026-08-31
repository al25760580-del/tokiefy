package com.milasoraki.tokiefy.extractor.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET

/**
 * Retrofit service for the currently-logged-in user's account metadata.
 *
 * `/passport/account/info/v2/` is one of the few read endpoints whose
 * response (doc 05.3 / 15.5) returns the logged-in user's uid, unique_id
 * (the @handle), nickname and avatar URLs. We call it right after the
 * WebView grabs a cookie so the UI can greet the user by their real
 * handle and so we can store secUid/uid for follow/like endpoints.
 */
public interface TikTokAccountApi {
    @GET("passport/account/info/v2/")
    public suspend fun accountInfo(): AccountInfoResponse
}

@JsonClass(generateAdapter = true)
public data class AccountInfoResponse(
    @Json(name = "status_code") val statusCode: Int = -1,
    @Json(name = "data") val data: AccountData? = null,
)

@JsonClass(generateAdapter = true)
public data class AccountData(
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "sec_uid") val secUid: String? = null,
    @Json(name = "unique_id") val uniqueId: String? = null,
    @Json(name = "nickname") val nickname: String? = null,
    @Json(name = "avatar_thumb") val avatarThumb: AccountAvatar? = null,
)

@JsonClass(generateAdapter = true)
public data class AccountAvatar(
    @Json(name = "url_list") val urlList: List<String> = emptyList(),
)
