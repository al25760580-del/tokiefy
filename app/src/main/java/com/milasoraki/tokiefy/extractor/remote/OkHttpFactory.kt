package com.milasoraki.tokiefy.extractor.remote

import com.milasoraki.tokiefy.extractor.api.interceptor.BodyIntegrityInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.CommonParamsInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.SessionHeadersInterceptor
import com.milasoraki.tokiefy.extractor.api.interceptor.UserAgentInterceptor
import com.milasoraki.tokiefy.extractor.remote.mock.MockResponseInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Constructs the single OkHttpClient used by the extractor layer.
 *
 * Why it exists:
 * HTTP client configuration (timeouts, interceptors, logging, mock
 * behaviour) is a plumbing detail that must not leak into repositories
 * or Retrofit service construction. This factory keeps that wiring in
 * one place.
 *
 * The mock interceptor is only added while [isProductionReady] is false,
 * so production builds never pull in synthetic-response code.
 */
public object OkHttpFactory {

    /**
     * Toggle to false until session + X-Bogus signing work end-to-end.
     * When true, the mock interceptor is removed and calls hit real
     * servers.
     */
    public var isProductionReady: Boolean = false

    public fun build(
        commonParams: CommonParamsInterceptor,
        sessionHeaders: SessionHeadersInterceptor,
        userAgent: UserAgentInterceptor,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(userAgent)
            .addInterceptor(commonParams)
            .addInterceptor(sessionHeaders)
            .addInterceptor(BodyIntegrityInterceptor())

        if (!isProductionReady) {
            builder.addInterceptor(MockResponseInterceptor())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        builder.addInterceptor(logging)

        return builder.build()
    }
}
