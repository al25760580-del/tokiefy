package com.milasoraki.tokiefy.extractor.api

/**
 * Centralised list of API base URLs and path constants.
 *
 * Why it exists:
 * Path strings scattered across Retrofit interfaces drift and cause
 * subtle bugs (typos, missing `/v1/` prefixes). Keeping them here makes
 * it obvious which version of the API each endpoint targets.
 */
public object TikTokEndpoints {
    public const val BASE_URL: String = "https://api.tiktokv.com/"
    public const val FEED_PATH: String = "aweme/v1/feed/"
    public const val DIGG_PATH: String = "aweme/v1/commit/item/digg/"
    public const val RELATION_FOLLOW_PATH: String = "aweme/v1/commit/follow/user/"
    public const val IM_CONVERSATIONS_PATH: String = "aweme/v1/im/conversation/list/"
    public const val IM_MESSAGES_PATH: String = "aweme/v1/im/message/list/"
    public const val IM_SEND_PATH: String = "aweme/v1/im/message/send/"
    public const val IM_STICKER_STORE_PATH: String = "aweme/v1/im/sticker/store/"
}
