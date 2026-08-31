package com.milasoraki.tokiefy.extractor.remote

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * Buffers every HTTP response body and, if the Content-Type is non-JSON
 * or the body starts with a non-JSON marker, records a one-line preview
 * into [NetworkDebugLogger] so the in-app debug console shows exactly
 * what the server returned (HTML challenge, empty body, etc.) instead
 * of the cryptic Moshi "Use JsonReader.setLenient(true)" message.
 *
 * The body is re-buffered so downstream interceptors and Retrofit's
 * converter still see the full bytes.
 */
public class ResponsePreviewInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val body = response.body ?: return response
        val bytes = body.bytes()
        val url = response.request.url.encodedPath
        // Re-emit the buffered body for the rest of the pipeline.
        val newBody = bytes.toResponseBody(body.contentType())
        val rebuilt = response.newBuilder().body(newBody).build()

        if (!response.isSuccessful && bytes.isNotEmpty()) {
            val preview = String(bytes, 0, minOf(bytes.size, 300))
                .replace("\n", " ")
                .replace("\r", " ")
                .trim()
            NetworkDebugLogger.recordError("HTTP ${response.code} $url -> $preview")
        }
        return rebuilt
    }
}
