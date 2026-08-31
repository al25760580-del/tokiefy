package com.milasoraki.tokiefy.extractor.api

/**
 * Hardcoded TikTok application identifiers, updated from live capture
 * (doc 15b, build 46.2.3, channel googleplay, MX region).
 *
 * Why update from v35 to v46:
 * The live capture shows that the server expects current version params.
 * Requests signed with aid=1180/version 35.x are increasingly rejected as
 * stale. Using the verified `aid=1233` + `musical_ly` identifiers gives
 * the best chance of success on unsigned web-compatible endpoints and
 * keeps our canned device/UA headers consistent with what the server
 * sees from real 46.x clients.
 */
public object TikTokAppIds {
    public const val AID: Int = 1233
    public const val APP_NAME: String = "musical_ly"
    public const val VERSION_NAME: String = "46.2.3"
    public const val VERSION_CODE: Int = 460203
    public const val UPDATE_VERSION_CODE: Long = 2024602030L
}
