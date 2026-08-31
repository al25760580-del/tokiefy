package com.milasoraki.tokiefy.extractor.api

/**
 * Hardcoded TikTok application identifiers.
 *
 * `aid=1180` is the international Android client; `app_name=trill` is the
 * internal codename still present in production builds. Pulling these out
 * as constants makes it trivial to switch to another client build (for
 * example TikTok Lite, aid=1340) during debugging.
 */
public object TikTokAppIds {
    public const val AID: Int = 1180
    public const val APP_NAME: String = "trill"
    public const val VERSION_NAME: String = "35.1.3"
    public const val VERSION_CODE: Int = 2023501030
}
