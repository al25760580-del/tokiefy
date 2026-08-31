package com.milasoraki.tokiefy.extractor.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET

/**
 * Retrofit service for the currently-logged-in user's account metadata.
 *
 * Called via the **web** client (www.tiktok.com) so the browser-cookie
 * sessionid captured by the embedded WebView works without native
 * X-Argus signing.
 *
 * Response envelope note:
 *   - The *native* endpoint returns `{status_code: 0, message: "success",
 *     data: {...}}`.
 *   - The *web* endpoint returns `{message: "success", data: {...}}` with
 *     no top-level status_code when things succeed, and `message:
 *     "error", error_code: N` when they don't.
 * We accept both shapes: [statusCode] defaults to 0 and we treat any
 * non-"success" message or a non-zero error_code as failure.
 */
public interface TikTokAccountApi {
    @GET("passport/account/info/v2/")
    public suspend fun accountInfo(): AccountInfoResponse
}

@JsonClass(generateAdapter = true)
public data class AccountInfoResponse(
    @Json(name = "status_code") val statusCode: Int = 0,
    @Json(name = "error_code") val errorCode: Int = 0,
    @Json(name = "message") val message: String = "",
    @Json(name = "data") val data: AccountData? = null,
) {
    /** True when the response signals success regardless of which envelope variant came back. */
    public fun isSuccess(): Boolean =
        (statusCode == 0 && errorCode == 0) && (message.equals("success", ignoreCase = true) || (message.isBlank() && data != null))
}

/**
 * Web account payload. Field names are a superset covering both the
 * native and web wrappers: `unique_id` (native) and `username` (web)
 * both alias to the @handle; `avatar_thumb` (native) and `avatar_url`
 * (web) alias to the avatar.
 */
@JsonClass(generateAdapter = true)
public data class AccountData(
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "user_id_str") val userIdStr: String? = null,
    @Json(name = "sec_user_id") val secUid: String? = null,
    @Json(name = "unique_id") val uniqueId: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "nickname") val nickname: String? = null,
    @Json(name = "screen_name") val screenName: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "mobile") val mobile: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "avatar_thumb") val avatarThumb: AccountAvatar? = null,
) {
    /** The @handle, trying all known field aliases. */
    public fun handle(): String? = uniqueId?.ifBlank { null }
        ?: username?.ifBlank { null }
        ?: screenName?.ifBlank { null }
        ?: nickname?.ifBlank { null }

    /** Avatar URL from whichever field the server returned. */
    public fun avatar(): String? = avatarUrl?.ifBlank { null }
        ?: avatarThumb?.urlList?.firstOrNull()

    /** Returns the numeric user id preferring the _str variant to avoid precision loss. */
    public fun resolvedUserId(): String = userIdStr?.ifBlank { null } ?: userId
}

@JsonClass(generateAdapter = true)
public data class AccountAvatar(
    @Json(name = "url_list") val urlList: List<String> = emptyList(),
)
