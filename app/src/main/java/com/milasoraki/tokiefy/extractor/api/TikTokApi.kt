package com.milasoraki.tokiefy.extractor.api

import android.content.Context
import com.milasoraki.tokiefy.extractor.api.interceptor.BrowserHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.CommonParamsInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.SessionHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.UserAgentInterceptor
import com.milasoraki.tokiefy.extractor.remote.OkHttpFactory
import com.milasoraki.tokiefy.extractor.remote.TiktokCookieJar
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Root Retrofit aggregator.
 *
 * A single [TiktokCookieJar] is shared between the native and web
 * OkHttpClient instances so cookies set by either lane (most notably
 * the anti-bot `msToken` that the web FYP endpoint demands on the
 * second request) are persisted and replayed on both.
 */
public class TikTokApi(
    public val feed: TikTokWebFeedApi,
    public val nativeFeed: TikTokFeedApi,
    public val digg: TikTokDiggApi,
    public val relation: TikTokRelationApi,
    public val messaging: TikTokMessagingApi,
    public val account: TikTokAccountApi,
    public val cookieJar: TiktokCookieJar,
) {
    public companion object {
        public fun create(
            context: Context,
            sessionHolder: SessionHolder = SessionHolder(),
            commonParams: CommonParamsInterceptor.Params = CommonParamsInterceptor.Params.default(),
        ): TikTokApi {
            val cookieJar = OkHttpFactory.newCookieJar(context.applicationContext)
            val sessionHeaders = SessionHeadersInterceptor(sessionHolder)
            val nativeClient: OkHttpClient = OkHttpFactory.buildNative(
                commonParams = CommonParamsInterceptor(commonParams),
                sessionHeaders = sessionHeaders,
                userAgent = UserAgentInterceptor(),
                cookieJar = cookieJar,
            )
            val webClient: OkHttpClient = OkHttpFactory.buildWeb(
                sessionHeaders = sessionHeaders,
                browserHeaders = BrowserHeadersInterceptor(),
                cookieJar = cookieJar,
            )

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
                account = webRetrofit.create(TikTokAccountApi::class.java),
                cookieJar = cookieJar,
            )
        }
    }
}
