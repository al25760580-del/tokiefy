package com.milasoraki.tokiefy.extractor.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Sets a browser-grade User-Agent and Referer for web endpoints.
 *
 * Why it exists:
 * The web backend at `www.tiktok.com/api/...` validates the caller using
 * browser signals — a mobile Chrome UA and a `Referer: https://www.tiktok.com/`
 * header. Missing either yields HTTP 403 or an HTML challenge page that
 * Moshi cannot parse. The native `api.tiktokv.com` client keeps its own
 * in-app UA via [UserAgentInterceptor]; this interceptor is for the
 * browser-cookie path only.
 */
public class BrowserHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 13; Pixel 5) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36",
            )
            .header("Referer", "https://www.tiktok.com/foryou")
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "es-419,es;q=0.9,en;q=0.8")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Dest", "empty")
            .build()
        return chain.proceed(request)
    }
}
