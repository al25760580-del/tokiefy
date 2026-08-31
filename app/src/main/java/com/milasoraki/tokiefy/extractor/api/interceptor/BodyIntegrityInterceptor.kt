package com.milasoraki.tokiefy.extractor.api.interceptor

import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.security.MessageDigest

/**
 * Adds `X-SS-STUB`, the lowercase hex MD5 of the request body.
 *
 * Why it exists:
 * TikTok's POST endpoints validate that the body was not tampered with by
 * comparing `X-SS-STUB` against the MD5 of the received body. Without
 * this header the server rejects requests with a 403/10201 signature
 * error. GET requests are untouched because they have no body.
 *
 * Signing (`X-Argus`, `X-Bogus`) is NOT performed here — it requires
 * libmsaoaidsec.so or a community reimplementation and is marked
 * TODO(SIGNING) in the appropriate place.
 */
public class BodyIntegrityInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val body: RequestBody? = request.body
        if (body == null || request.method.uppercase() != "POST") {
            return chain.proceed(request)
        }
        val stub = md5Hex(body)
        val signed = request.newBuilder()
            .header("X-SS-STUB", stub)
            // TODO(SIGNING): attach real X-Argus / X-Bogus here.
            .header("X-Argus", "")
            .header("X-Bogus", "")
            .build()
        return chain.proceed(signed)
    }

    private fun md5Hex(body: RequestBody): String {
        val buffer = Buffer()
        body.writeTo(buffer)
        val digest = MessageDigest.getInstance("MD5").digest(buffer.readByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
