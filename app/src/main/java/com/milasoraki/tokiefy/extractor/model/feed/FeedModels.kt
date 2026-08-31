package com.milasoraki.tokiefy.extractor.model.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.milasoraki.tokiefy.extractor.model.user.ImageUrl
import com.milasoraki.tokiefy.extractor.model.user.User

/**
 * A single video ("aweme") in the vertical feed.
 *
 * Kept minimal for the MVP: only fields used by the Home screen are
 * modelled. Unknown fields are ignored by Moshi, so adding fields later
 * is safe.
 */
@JsonClass(generateAdapter = true)
public data class Aweme(
    @Json(name = "aweme_id") val awemeId: String = "",
    @Json(name = "desc") val description: String = "",
    @Json(name = "author") val author: User? = null,
    @Json(name = "video") val video: Video? = null,
    @Json(name = "statistics") val statistics: Statistics? = null,
) {
    /** Returns the first available cover URL or null. */
    public fun coverUrl(): String? = video?.coverUrl()
}

@JsonClass(generateAdapter = true)
public data class Video(
    @Json(name = "cover") val cover: ImageUrl? = null,
    @Json(name = "play_addr") val playAddress: ImageUrl? = null,
)

@JsonClass(generateAdapter = true)
public data class Statistics(
    @Json(name = "digg_count") val diggCount: Long = 0,
    @Json(name = "comment_count") val commentCount: Long = 0,
    @Json(name = "share_count") val shareCount: Long = 0,
)

@JsonClass(generateAdapter = true)
public data class FeedResponse(
    @Json(name = "aweme_list") val awemes: List<Aweme> = emptyList(),
    @Json(name = "has_more") val hasMore: Boolean = false,
    @Json(name = "max_cursor") val maxCursor: Long = 0,
)

/** Returns the first available cover URL or null. */
public fun Video.coverUrl(): String? = cover?.urlList?.firstOrNull()

/** Returns the first playable video URL or null. */
public fun Video.playUrl(): String? = playAddress?.urlList?.firstOrNull()
