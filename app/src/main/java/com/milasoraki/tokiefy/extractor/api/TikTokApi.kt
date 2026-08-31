package com.milasoraki.tokiefy.extractor.api

import com.milasoraki.tokiefy.extractor.api.interceptor.BrowserHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.CommonParamsInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.SessionHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.UserAgentInterceptor
import com.milasoraki.tokiefy.extractor.remote.OkHttpFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Root Retrofit aggregator for TikTok's HTTP API.
 *
 * Why it exists:
 * The API surface is split across several Retrofit sub-interfaces
 * (feed, digg, relation, messaging) so each can stay focused on one
 * domain. This class is the single place that wires them to a shared
 * OkHttp client and Moshi converter, mirroring the NewPipe/Xtra pattern
 * of an ISP-style aggregator.
 *
 * Two HTTP clients are maintained in parallel:
 *   - [nativeClient] targets `api.tiktokv.com` and carries the in-app UA
 *     + common device params required by Digg/Relation/IM. It requires
 *     X-Argus signing to return real data and stays mock-gated behind
 *     [com.milasoraki.tokiefy.extractor.remote.OkHttpFactory.isProductionReady].
 *   - [webClient] targets `www.tiktok.com` with a browser UA and
 *     Referer. It is used for the web For-You/PFY feed, which is the
 *     endpoint that accepts the browser `sessionid` captured by the
 *     embedded WebView login without requiring X-Argus.
 *
 * Trade-offs vs alternatives:
 *
 * | Option               | Pros                           | Cons                                |
 * |----------------------|--------------------------------|-------------------------------------|
 * | ISP-style (this)     | Each sub-API small; easy mock  | One extra object holding them       |
 * | Single Retrofit iface| Fewer files                    | God interface; hard to read         |
 * | Per-screen Retrofit  | Max isolation                  | Duplicated clients, more allocations|
 */
public class TikTokApi(
    public val feed: TikTokFeedApi,
    public val webFeed: TikTokWebFeedApi,
    public val digg: TikTokDiggApi,
    public val relation: TikTokRelationApi,
    public val messaging: TikTokMessagingApi,
) {
    public companion object {
        /** Builds the production API instance with all required interceptors. */
        public fun create(
            sessionHolder: SessionHolder = SessionHolder(),
            commonParams: CommonParamsInterceptor.Params = CommonParamsInterceptor.Params.default(),
        ): TikTokApi {
            val nativeClient: OkHttpClient = OkHttpFactory.build(
                commonParams = CommonParamsInterceptor(commonParams),
                sessionHeaders = SessionHeadersInterceptor(sessionHolder),
                userAgent = UserAgentInterceptor(),
            )
            val webClient: OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(BrowserHeadersInterceptor())
                .addInterceptor(SessionHeadersInterceptor(sessionHolder))
                .build()

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
                feed = nativeRetrofit.create(TikTokFeedApi::class.java),
                webFeed = webRetrofit.create(TikTokWebFeedApi::class.java),
                digg = nativeRetrofit.create(TikTokDiggApi::class.java),
                relation = nativeRetrofit.create(TikTokRelationApi::class.java),
                messaging = nativeRetrofit.create(TikTokMessagingApi::class.java),
            )
        }
    }
}
