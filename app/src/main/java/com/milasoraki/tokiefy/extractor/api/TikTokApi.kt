package com.milasoraki.tokiefy.extractor.api

import com.milasoraki.tokiefy.extractor.api.interceptor.BrowserHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.CommonParamsInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.SessionHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.UserAgentInterceptor
import com.milasoraki.tokiefy.extractor.remote.OkHttpFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Root Retrofit aggregator for TikTok's HTTP API.
 *
 * Two HTTP clients coexist (see OkHttpFactory KDoc):
 *   - [nativeClient] -> `api*.tiktokv.com` lanes (in-app).
 *   - [webClient]    -> `www.tiktok.com` lanes (browser).
 *
 * Authenticated account/feed calls are wired to the web client because
 * those endpoints work with the WebView-captured `sessionid` and do
 * not require X-Argus/X-Ladon signing.
 */
public class TikTokApi(
    public val feed: TikTokWebFeedApi,
    public val nativeFeed: TikTokFeedApi,
    public val digg: TikTokDiggApi,
    public val relation: TikTokRelationApi,
    public val messaging: TikTokMessagingApi,
    public val account: TikTokAccountApi,
) {
    public companion object {
        public fun create(
            sessionHolder: SessionHolder = SessionHolder(),
            commonParams: CommonParamsInterceptor.Params = CommonParamsInterceptor.Params.default(),
        ): TikTokApi {
            val sessionHeaders = SessionHeadersInterceptor(sessionHolder)
            val nativeClient: OkHttpClient = OkHttpFactory.buildNative(
                commonParams = CommonParamsInterceptor(commonParams),
                sessionHeaders = sessionHeaders,
                userAgent = UserAgentInterceptor(),
            )
            val webClient: OkHttpClient = OkHttpFactory.buildWeb(
                sessionHeaders = sessionHeaders,
                browserHeaders = BrowserHeadersInterceptor(),
            )

            // Lenient Moshi so TikTok's occasional trailing commas /
            // control characters in JSON bodies don't blow up parsing.
            val moshi = Moshi.Builder().build()

            val nativeRetrofit = Retrofit.Builder()
                .baseUrl(TikTokEndpoints.BASE_NATIVE_URL)
                .client(nativeClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
                .build()
            val webRetrofit = Retrofit.Builder()
                .baseUrl(TikTokEndpoints.BASE_WEB_URL)
                .client(webClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
                .build()
            return TikTokApi(
                feed = webRetrofit.create(TikTokWebFeedApi::class.java),
                nativeFeed = nativeRetrofit.create(TikTokFeedApi::class.java),
                digg = nativeRetrofit.create(TikTokDiggApi::class.java),
                relation = nativeRetrofit.create(TikTokRelationApi::class.java),
                messaging = nativeRetrofit.create(TikTokMessagingApi::class.java),
                // Account info is called over the web client so the
                // browser-cookie session the WebView captured is enough.
                account = webRetrofit.create(TikTokAccountApi::class.java),
            )
        }
    }
}
