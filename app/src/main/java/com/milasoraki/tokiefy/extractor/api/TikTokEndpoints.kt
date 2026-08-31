package com.milasoraki.tokiefy.extractor.api

/**
 * Centralised list of API base URLs and path constants.
 *
 * Why it exists:
 * Path strings scattered across Retrofit interfaces drift and cause
 * subtle bugs (typos, missing `/v1/` prefixes). Keeping them here makes
 * it obvious which version of the API each endpoint targets.
 *
 * Two entry points exist in parallel:
 *   - [BASE_NATIVE_URL] (`api.tiktokv.com`) — used by the official app.
 *     Requires device params and X-Argus/X-Bogus signing for most
 *     endpoints; used for Digg/Relation/IM where the app signature is
 *     the only way.
 *   - [BASE_WEB_URL] (`www.tiktok.com`) — the web backend. Accepts a
 *     normal browser session cookie (which is what our WebView login
 *     grabber produces) and does NOT require X-Argus for the public
 *     For-You feed. We use this for the FYP when the user imported a
 *     session via the embedded WebView.
 */
public object TikTokEndpoints {
    public const val BASE_NATIVE_URL: String = "https://api.tiktokv.com/"
    public const val BASE_WEB_URL: String = "https://www.tiktok.com/"

    // --- Native (app) endpoints, require X-Argus / X-Bogus ---
    public const val FEED_PATH: String = "aweme/v1/feed/"
    public const val DIGG_PATH: String = "aweme/v1/commit/item/digg/"
    public const val RELATION_FOLLOW_PATH: String = "aweme/v1/commit/follow/user/"
    public const val IM_CONVERSATIONS_PATH: String = "aweme/v1/im/conversation/list/"
    public const val IM_MESSAGES_PATH: String = "aweme/v1/im/message/list/"
    public const val IM_SEND_PATH: String = "aweme/v1/im/message/send/"
    public const val IM_STICKER_STORE_PATH: String = "aweme/v1/im/sticker/store/"

    // --- Web endpoints, accept browser sessionid ---
    /**
     * For-You feed from the web client.
     *
     * Accepts `count` (≤30) and an optional `cursor`. Returns the same
     * `aweme_list` JSON shape as the native `/aweme/v1/feed/` endpoint,
     * so we reuse [com.milasoraki.tokiefy.extractor.model.feed.FeedResponse].
     */
    public const val WEB_FYP_PATH: String = "api/recommend/item_list/"
}
