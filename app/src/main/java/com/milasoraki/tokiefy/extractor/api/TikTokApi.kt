package com.milasoraki.tokiefy.extractor.api

import com.milasoraki.tokiefy.extractor.api.interceptor.BrowserHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.CommonParamsInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.SessionHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.UserAgentInterceptor
import com.milasoraki.tokiefy.extractor.remote.OkHttpFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Root Retrofit aggregator for TikTok's HTTP API.
 *
 * Two HTTP clients coexist (see OkHttpFactory KDoc for the rationale):
 *   - [nativeClient] targets `api*.tiktokv.com` (app lanes). It carries
 *     the in-app UA, device params, body-integrity interceptor, and is
 *     currently intercepted by MockResponseInterceptor whenever
 *     [OkHttpFactory.isProductionReady] is false, because these lanes
 *     require X-Argus/X-Ladon signing (doc 15b).
 *   - [webClient] targets `www.tiktok.com` (browser lanes) with a
 *     mobile Chrome UA and Referer. Used for endpoints that accept
 *     the web sessionid we capture via the embedded WebView login.
 *
 * Feed is bound from the web client today because `/api/recommend/
 * item_list/` returns the real For-You feed with just a browser cookie.
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

            val nativeRetrofit = Retrofit.Builder()
                .baseUrl(TikTokEndpoints.BASE_NATIVE_URL)
                .client(nativeClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            val webRetrofit = Retrofit.Builder()
                .baseUrl(TikTokEndpoints.BASE_WEB_URL)
                .client(webClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            return TikTokApi(
                feed = webRetrofit.create(TikTokWebFeedApi::class.java),
                nativeFeed = nativeRetrofit.create(TikTokFeedApi::class.java),
                digg = nativeRetrofit.create(TikTokDiggApi::class.java),
                relation = nativeRetrofit.create(TikTokRelationApi::class.java),
                messaging = nativeRetrofit.create(TikTokMessagingApi::class.java),
                account = nativeRetrofit.create(TikTokAccountApi::class.java),
            )
        }
    }
}
