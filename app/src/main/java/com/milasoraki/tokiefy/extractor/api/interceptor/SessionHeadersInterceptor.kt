package com.milasoraki.tokiefy.extractor.api.interceptor

import com.milasoraki.tokiefy.extractor.api.SessionHolder
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds session-level headers: Cookie, X-CSRFToken and persistent device id.
 *
 * Why it exists:
 * Authenticated POSTs require the `Cookie` header carrying `sessionid`
 * (and friends) plus an `X-CSRFToken` header copied from the cookie jar.
 * The csrf token is omitted until the cookie jar contains one, so
 * unauthenticated or first-login calls do not carry a bogus token.
 */
public class SessionHeadersInterceptor(
    private val sessionHolder: SessionHolder,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val session = sessionHolder.get()
        val builder = chain.request().newBuilder()
        val cookieHeader: String = session.cookies
            .joinToString("; ") { "${it.name}=${it.value}" }
        if (cookieHeader.isNotBlank()) {
            builder.header("Cookie", cookieHeader)
        }
        if (session.csrfToken.isNotBlank()) {
            builder.header("X-CSRFToken", session.csrfToken)
        }
        return chain.proceed(builder.build())
    }
}
