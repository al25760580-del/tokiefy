package com.milasoraki.tokiefy.extractor.remote.mock

import com.milasoraki.tokiefy.extractor.api.Session
import com.milasoraki.tokiefy.extractor.api.SessionHolder
import com.milasoraki.tokiefy.extractor.api.TikTokEndpoints
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * Returns canned JSON responses when the real backend cannot be reached.
 *
 * When it is active:
 *  - No authenticated session exists (guest mode): the backend would
 *    return 401/403 for most DM/like/follow routes.
 *  - The backend responds with 401, 403 or any network error.
 *
 * Why it lives at the HTTP-client layer rather than in fake repositories:
 * Intercepting at the OkHttp level exercises Retrofit, Moshi, and all
 * signing interceptors on the same code path that production uses. A
 * fake repository would bypass those layers and hide parse errors.
 *
 * Output: an HTTP 200 response carrying a JSON body that matches the
 * real (snake_case) schema.
 */
public class MockResponseInterceptor(
    private val sessionHolder: SessionHolder = SessionHolder(Session()),
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val shouldServeMock: Boolean = sessionHolder.get().userId.isBlank() || !isProductionReady
        if (!shouldServeMock) {
            return runCatching { chain.proceed(request) }
                .getOrElse { fakeResponse(request, selectPayload(request)) }
        }

        return runCatching {
            val real = chain.proceed(request)
            if (real.isSuccessful && real.code != HTTP_UNAUTHORIZED && real.code != HTTP_FORBIDDEN) {
                real
            } else {
                real.close()
                fakeResponse(request, selectPayload(request))
            }
        }.getOrElse { fakeResponse(request, selectPayload(request)) }
    }

    /**
     * Picks the canned JSON payload for the requested path.
     *
     * New routes added to [TikTokEndpoints] must have a matching case
     * here until full authentication and signing are implemented.
     */
    private fun selectPayload(request: Request): String {
        val path: String = request.url.encodedPath
        return when {
            path.contains(TikTokEndpoints.IM_CONVERSATIONS_PATH) -> MockData.CONVERSATIONS_RESPONSE.trimIndent()
            path.contains(TikTokEndpoints.IM_MESSAGES_PATH) -> {
                val conversationId: String = request.url.queryParameter("conversation_id").orEmpty()
                MockData.messagesFor(conversationId).trimIndent()
            }
            path.contains(TikTokEndpoints.IM_SEND_PATH) -> {
                val body = request.body?.let { body ->
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    buffer.readUtf8()
                } ?: ""
                MockData.sentMessageResponse(body).trimIndent()
            }
            path.contains(TikTokEndpoints.IM_STICKER_STORE_PATH) -> MockData.STICKERS_RESPONSE.trimIndent()
            path.contains(TikTokEndpoints.FEED_PATH) -> MockData.FEED_RESPONSE.trimIndent()
            path.contains(TikTokEndpoints.DIGG_PATH) ||
                path.contains(TikTokEndpoints.RELATION_FOLLOW_PATH) -> MockData.OK.trimIndent()
            else -> MockData.OK.trimIndent()
        }
    }

    private fun fakeResponse(request: Request, json: String): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(HTTP_OK)
            .message("OK (mock)")
            .body(json.toResponseBody(JSON_MEDIA_TYPE))
            .build()

    private companion object {
        private const val HTTP_OK: Int = 200
        private const val HTTP_UNAUTHORIZED: Int = 401
        private const val HTTP_FORBIDDEN: Int = 403
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()

        /** Flip to true once native X-Argus/X-Bogus signing is in place. TODO(SIGNING) */
        private const val isProductionReady: Boolean = false
    }
}
