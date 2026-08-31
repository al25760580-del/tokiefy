package com.milasoraki.tokiefy.extractor.model.sticker

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * A single sticker inside a [StickerPack].
 *
 * @property stickerId   stable identifier used when sending the sticker.
 * @property displayName user-facing label (used for accessibility).
 * @property url         CDN URL of the sticker image; typically an animated
 *                       webp/gif.
 */
@JsonClass(generateAdapter = true)
public data class Sticker(
    @Json(name = "sticker_id") val stickerId: String = "",
    @Json(name = "name") val displayName: String = "",
    @Json(name = "url") val url: String = "",
)

/** A named sticker pack; groups stickers in the "Saved" tab. */
@JsonClass(generateAdapter = true)
public data class StickerPack(
    @Json(name = "pack_id") val packId: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "cover_url") val coverUrl: String = "",
    @Json(name = "stickers") val stickers: List<Sticker> = emptyList(),
)

/** Sticker store response: recent stickers plus saved packs. */
@JsonClass(generateAdapter = true)
public data class StickerStoreResponse(
    @Json(name = "recent_stickers") val recentStickers: List<Sticker> = emptyList(),
    @Json(name = "saved_packs") val savedPacks: List<StickerPack> = emptyList(),
)
