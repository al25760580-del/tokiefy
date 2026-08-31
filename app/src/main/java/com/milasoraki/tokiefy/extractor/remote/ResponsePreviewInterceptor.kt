package com.milasoraki.tokiefy.extractor.remote

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Buffers every HTTP response body so:
 *   - non-2xx responses are previewed in [NetworkDebugLogger] (first
 *     300 chars, on a single line), and
 *   - 200 responses with a non-JSON content-type (e.g. HTML challenge
 *     pages, CAPTCHA pages) are also previewed, because they will be
 *     turned into Moshi parse exceptions by Retrofit that don't show
 *     the offending body.
 *
 * The body is re-emitted to the rest of the pipeline so Retrofit can
 * still consume it.
 */
public class ResponsePreviewInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val body = response.body ?: return response
        val bytes = body.bytes()
        val url = response.request.url.encodedPath
        val ct = (body.contentType()?.toString() ?: "").lowercase()
        val newBody = bytes.toResponseBody(body.contentType())
        val rebuilt = response.newBuilder().body(newBody).build()

        val firstByte = bytes[0]
        val looksLikeJson = ct.contains("json") ||
            (bytes.isNotEmpty() && (firstByte == '{'.code.toByte() || firstByte == '['.code.toByte()))

        if (bytes.isNotEmpty() && (!response.isSuccessful || !looksLikeJson)) {
            val preview = String(bytes, 0, minOf(bytes.size, 300))
                .replace("\n", " ")
                .replace("\r", " ")
                .trim()
            val label = if (response.isSuccessful) "non-JSON 200" else "HTTP ${response.code}"
            NetworkDebugLogger.recordError("$label $url -> $preview")
        }
        return rebuilt
    }
}
