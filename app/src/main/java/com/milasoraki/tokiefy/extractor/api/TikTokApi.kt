package com.milasoraki.tokiefy.extractor.api

import com.milasoraki.tokiefy.extractor.api.interceptor.CommonParamsInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.SessionHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.UserAgentInterceptor
import com.milasoraki.tokiefy.extractor.remote.OkHttpFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

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
    public val digg: TikTokDiggApi,
    public val relation: TikTokRelationApi,
    public val messaging: TikTokMessagingApi,
) {
    public companion object {
        /** Builds the production API instance with all required interceptors. */
        public fun create(
            baseUrl: String = TikTokEndpoints.BASE_URL,
            sessionHolder: SessionHolder = SessionHolder(),
            commonParams: CommonParamsInterceptor.Params = CommonParamsInterceptor.Params.default(),
        ): TikTokApi {
            val okHttpClient = OkHttpFactory.build(
                commonParams = commonParams,
                sessionHeaders = SessionHeadersInterceptor(sessionHolder),
                userAgent = UserAgentInterceptor(),
            )
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            return TikTokApi(
                feed = retrofit.create(TikTokFeedApi::class.java),
                digg = retrofit.create(TikTokDiggApi::class.java),
                relation = retrofit.create(TikTokRelationApi::class.java),
                messaging = retrofit.create(TikTokMessagingApi::class.java),
            )
        }
    }
}
