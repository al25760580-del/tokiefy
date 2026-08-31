package com.milasoraki.tokiefy.extractor.model.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * TikTok user as returned by user/feed endpoints.
 *
 * Field names mirror the JSON keys exactly (`unique_id`, `follower_count`)
 * so Moshi can decode without per-field `@Json`. Mapping to UI-friendly
 * strings (for example "12.3K followers") is done in repositories — this
 * class stays a pure DTO.
 */
@JsonClass(generateAdapter = true)
public data class User(
    @Json(name = "uid") val uid: String = "",
    @Json(name = "unique_id") val uniqueId: String = "",
    @Json(name = "nickname") val nickname: String = "",
    @Json(name = "avatar_thumb") val avatarThumb: ImageUrl? = null,
    @Json(name = "follower_count") val followerCount: Int = 0,
) {
    /** Returns the first available avatar URL or null. */
    public fun avatarUrl(): String? = avatarThumb?.urlList?.firstOrNull()
}

@JsonClass(generateAdapter = true)
public data class ImageUrl(
    @Json(name = "url_list") val urlList: List<String> = emptyList(),
)
