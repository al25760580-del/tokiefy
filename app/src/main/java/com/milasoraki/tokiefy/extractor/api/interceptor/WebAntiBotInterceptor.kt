package com.milasoraki.tokiefy.extractor.api.interceptor

import com.milasoraki.tokiefy.extractor.remote.TiktokCookieJar
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds anti-bot query parameters the web backend requires: `msToken`
 * (mirrored from the cookie of the same name) and a `WebIdLastTime`.
 *
 * Why an interceptor instead of @Query on each Retrofit method:
 * The values are set dynamically: the server returns a fresh `msToken`
 * in a `Set-Cookie` header on every call, and OkHttp's CookieJar stores
 * it; the NEXT request must echo that value as a **query parameter** in
 * addition to the cookie. Without this round-trip,
 * `www.tiktok.com/api/recommend/item_list/` returns HTTP 200 with an
 * empty body and our parser fails with EOFException.
 */
public class WebAntiBotInterceptor(
    private val cookieJar: TiktokCookieJar,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url
        val cookieHeader = cookieJar.snapshotRawFor(url)
        val msToken = cookieHeader
            .splitToSequence(';')
            .map { it.trim() }
            .firstOrNull { it.startsWith("msToken=") }
            ?.substringAfter("=")
            .orEmpty()

        val builder = url.newBuilder()
        if (msToken.isNotBlank() && original.url.queryParameter("msToken") == null) {
            builder.addQueryParameter("msToken", msToken)
        }
        if (original.url.queryParameter("WebIdLastTime") == null) {
            // Stable per-session anchor; web client sends the user's
            // creation time or 0 if unknown.
            builder.addQueryParameter("WebIdLastTime", "0")
        }
        return chain.proceed(original.newBuilder().url(builder.build()).build())
    }
}
