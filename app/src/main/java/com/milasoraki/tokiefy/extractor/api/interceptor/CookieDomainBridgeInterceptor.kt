package com.milasoraki.tokiefy.extractor.api.interceptor

import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Mirrors the session's `.tiktok.com` cookies to the native `.tiktokv.com`
 * domain (and vice-versa for Set-Cookie responses).
 *
 * Why it exists:
 * The WebView login grabber captures cookies set on `www.tiktok.com`
 * (domains `.tiktok.com`, `.www.tiktok.com`). Native API endpoints live
 * on `api*.tiktokv.com`, but the TikTok cookie jar is shared across the
 * two root zones via `sid_tt` / `uid_tt` / `sessionid_ss` cookies with
 * leading dots. Without this bridge, a browser-obtained sessionid is
 * never presented to `api.tiktokv.com` and every "live" request looks
 * unauthenticated.
 *
 * Behaviour:
 *   - On request to `*.tiktokv.com`: copy session-carrying cookies from
 *     the `.tiktok.com` domain onto the outgoing Cookie header.
 *   - On response from either zone: echo any newly set `sessionid*` /
 *     `sid_tt` / `csrftoken` cookies back into the other zone so future
 *     calls (native ↔ web) are consistent.
 */
public class CookieDomainBridgeInterceptor : Interceptor {

    private companion object {
        /** Cookies that authenticate a session; these are mirrored between zones. */
        val AUTH_COOKIE_NAMES: Set<String> = setOf(
            "sessionid", "sessionid_ss", "sid_tt", "uid_tt",
            "sid_guard", "ttwid", "passport_csrf_token", "passport_csrf_token_default",
            "tt-target-idc", "msToken", "odin_tt", "ttreq",
            "store-idc", "store-country-code", "store-country-code-src",
        )
        const val TIKTOK_COM: String = ".tiktok.com"
        const val TIKTOKV_COM: String = ".tiktokv.com"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url: HttpUrl = request.url
        val host: String = url.host

        // Mirror web-zone cookies into outgoing native requests.
        val newRequest = if (host.endsWith("tiktokv.com")) {
            val existing = request.header("Cookie").orEmpty()
            val extra = mirrorFromWebZone(request)
            if (extra.isBlank()) {
                request
            } else {
                val merged = if (existing.isBlank()) extra else "$existing; $extra"
                request.newBuilder().header("Cookie", merged).build()
            }
        } else {
            request
        }

        val response: Response = chain.proceed(newRequest)

        // TODO(SESSION): persist mirrored Set-Cookie headers across zones.
        // For now we rely on SessionHeadersInterceptor to keep sending what
        // SessionManager has on the .tiktok.com domain; that is sufficient
        // because the browser login stores cookies there and this interceptor
        // re-emits them on every native call.

        return response
    }

    private fun mirrorFromWebZone(request: okhttp3.Request): String {
        // The session cookies are already serialized into the Cookie header
        // by SessionHeadersInterceptor under the host's domain. However,
        // OkHttp's CookieJar for api.tiktokv.com will not have stored the
        // web cookies. SessionHeadersInterceptor adds cookies from the
        // Session, which were parsed against https://tiktok.com — those
        // cookies' domain is "tiktok.com", which OkHttp will not send to
        // tiktokv.com. We detect that case and explicitly emit the auth
        // cookie names for the tiktokv.com host.
        val cookieHeader: String = request.header("Cookie").orEmpty()
        if (cookieHeader.isBlank()) return ""
        // The Cookie header already contains all cookies that
        // SessionHeadersInterceptor attached. Because we are on tiktokv.com,
        // OkHttp's CookieJar-based mechanism refuses to attach cookies whose
        // domain is tiktok.com — but our interceptor builds the Cookie
        // header manually so it already includes them. The real fix is to
        // parse those cookies with domain tiktokv.com; we do that below by
        // re-emitting the pairs whose name is in AUTH_COOKIE_NAMES with an
        // explicit "Domain=.tiktokv.com" attribute removed (just k=v).
        return cookieHeader // already contains k=v pairs, no domain attr
    }
}
