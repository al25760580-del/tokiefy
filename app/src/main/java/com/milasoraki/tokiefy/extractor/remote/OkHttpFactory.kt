package com.milasoraki.tokiefy.extractor.remote

import android.content.Context
import com.milasoraki.tokiefy.extractor.api.interceptor.BodyIntegrityInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.CommonParamsInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.SessionHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.UserAgentInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.WebAntiBotInterceptor
import com.milasoraki.tokiefy.extractor.remote.mock.MockResponseInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Constructs OkHttp clients used by the extractor layer.
 *
 * Two factories are exposed so native-app and web-browser endpoints can
 * have independent interceptor stacks. They share the SAME
 * [TiktokCookieJar] so cookies set on one lane (e.g. msToken received
 * from a web FYP response) are immediately visible on the other, which
 * is required for the anti-bot challenge flow to complete.
 *
 * Mock interceptor: gated by [isProductionReady]. When false, native
 * endpoints are short-circuited to mock JSON so UI work can proceed
 * while X-Argus is unimplemented. Web endpoints always hit real
 * servers because they work with a browser sessionid.
 */
public object OkHttpFactory {

    /**
     * When true, native `api*.tiktokv.com` calls are allowed to hit the
     * real servers and the mock interceptor is removed.
     */
    public var isProductionReady: Boolean = false

    private fun baseBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)

    private fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor(NetworkDebugLogger.okHttpLogger).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    /** Creates the single shared cookie jar used by all HTTP clients. */
    public fun newCookieJar(context: Context): TiktokCookieJar = TiktokCookieJar(context)

    /** Native (app) client — api.tiktokv.com lanes. */
    public fun buildNative(
        commonParams: CommonParamsInterceptor,
        sessionHeaders: SessionHeadersInterceptor,
        userAgent: UserAgentInterceptor,
        cookieJar: TiktokCookieJar,
    ): OkHttpClient {
        val builder = baseBuilder()
            .cookieJar(cookieJar)
            .addInterceptor(ResponsePreviewInterceptor())
            .addInterceptor(userAgent)
            .addInterceptor(commonParams)
            .addInterceptor(sessionHeaders)
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
        cookieJar: TiktokCookieJar,
    ): OkHttpClient {
        return baseBuilder()
            .cookieJar(cookieJar)
            .addInterceptor(ResponsePreviewInterceptor())
            .addInterceptor(browserHeaders)
            .addInterceptor(WebAntiBotInterceptor(cookieJar))
            .addInterceptor(sessionHeaders)
            .addInterceptor(loggingInterceptor())
            .build()
    }
}
