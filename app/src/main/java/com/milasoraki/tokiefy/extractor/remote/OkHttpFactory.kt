package com.milasoraki.tokiefy.extractor.remote

import com.milasoraki.tokiefy.extractor.api.interceptor.BodyIntegrityInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.CommonParamsInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.CookieDomainBridgeInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.SessionHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.UserAgentInterceptor
import com.milasoraki.tokiefy.extractor.remote.mock.MockResponseInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Constructs OkHttp clients used by the extractor layer.
 *
 * Two factories are exposed so the native-app endpoints and the
 * browser-compatible web endpoints can have independent interceptor
 * stacks, but they all share the same debug logger ([NetworkDebugLogger])
 * so that the in-app debug console shows traffic from both.
 *
 * Mock interceptor: gated by [isProductionReady]. When false, native
 * endpoints are short-circuited to mock JSON so UI work can proceed
 * while X-Argus is unimplemented. Web endpoints go through regardless
 * because they work with a browser sessionid and no native signing.
 */
public object OkHttpFactory {

    /**
     * When true, native `api*.tiktokv.com` calls are allowed to hit the
     * real servers and the mock interceptor is removed. Defaults to
     * false until X-Argus/X-Ladon signing is implemented.
     */
    public var isProductionReady: Boolean = false

    private fun baseBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)

    private fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor(NetworkDebugLogger.okHttpLogger).apply {
            // BODY includes headers + full body in debug builds — exactly
            // what we need to see server responses on the phone.
            level = HttpLoggingInterceptor.Level.BODY
        }

    /** Native (app) client — api.tiktokv.com lanes. */
    public fun buildNative(
        commonParams: CommonParamsInterceptor,
        sessionHeaders: SessionHeadersInterceptor,
        userAgent: UserAgentInterceptor,
    ): OkHttpClient {
        val builder = baseBuilder()
            .addInterceptor(userAgent)
            .addInterceptor(commonParams)
            .addInterceptor(sessionHeaders)
            .addInterceptor(CookieDomainBridgeInterceptor())
            .addInterceptor(BodyIntegrityInterceptor())
        if (!isProductionReady) {
            builder.addInterceptor(MockResponseInterceptor())
        }
        builder.addInterceptor(loggingInterceptor())
        return builder.build()
    }

    /** Web (browser) client — www.tiktok.com lanes. */
    public fun buildWeb(
        sessionHeaders: SessionHeadersInterceptor,
        browserHeaders: com.milasoraki.tokiefy.extractor.api.interceptor.BrowserHeadersInterceptor,
    ): OkHttpClient {
        return baseBuilder()
            .addInterceptor(browserHeaders)
            .addInterceptor(sessionHeaders)
            .addInterceptor(CookieDomainBridgeInterceptor())
            .addInterceptor(loggingInterceptor())
            .build()
    }
}
