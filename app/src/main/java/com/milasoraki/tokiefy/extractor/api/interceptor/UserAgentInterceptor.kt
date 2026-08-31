package com.milasoraki.tokiefy.extractor.api.interceptor

import com.milasoraki.tokiefy.extractor.api.TikTokAppIds
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Sets an in-app User-Agent matching the TikTok 46.2.3 (build 460203)
 * Google Play arm64 Android client captured in doc 15b.
 *
 * The UA string is what the real app emits; using it avoids being
 * fingerprinted as a stale/unknown client. Native `api*.tiktokv.com`
 * endpoints validate the UA family in addition to the `aid` parameter.
 */
public class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val ua = "com.zhiliaoapp.musically/${TikTokAppIds.VERSION_CODE} " +
            "(Linux; U; Android 13; en_US; Pixel 5; " +
            "Build/TQ3A.230901.001; Cronet/TTNetVersion:b4d74d15 2026-08-31)"
        val request = chain.request().newBuilder()
            .header("User-Agent", ua)
            .header("Accept-Encoding", "gzip, br, deflate")
            .build()
        return chain.proceed(request)
    }
}
