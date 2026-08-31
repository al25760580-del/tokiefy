package com.milasoraki.tokiefy.extractor.api.interceptor

import com.milasoraki.tokiefy.extractor.api.SessionHolder
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds session-level HEADERS (not cookies — those live in the shared
 * TiktokCookieJar). Specifically:
 *   - `X-CSRFToken` copied from the session's csrf token, which the
 *     server expects on mutating POSTs.
 *
 * Cookies are handled by the shared [com.milasoraki.tokiefy.extractor.remote.TiktokCookieJar]
 * so they are set, stored and updated according to RFC 6265 across
 * HTTP clients (native + web) automatically. Previously we tried to
 * build the Cookie header manually here which produced duplicates and
 * prevented cookies set by HTTP responses (msToken, odin_tt, ttwid,
 * Set-Cookie rotators from anti-bot) from being echoed back.
 */
public class SessionHeadersInterceptor(
    private val sessionHolder: SessionHolder,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val session = sessionHolder.get()
        val builder = chain.request().newBuilder()
        if (session.csrfToken.isNotBlank()) {
            builder.header("X-CSRFToken", session.csrfToken)
        }
        return chain.proceed(builder.build())
    }
}
