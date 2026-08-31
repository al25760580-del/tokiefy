package com.milasoraki.tokiefy.extractor.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Sets a realistic Android User-Agent for all requests.
 *
 * Why it exists:
 * The `aweme/v1/...` endpoints return a different payload (sometimes an
 * HTML page) when the UA looks like a desktop browser or the default
 * Retrofit/OkHttp identifier. A hardcoded mobile UA is sufficient during
 * development; in production it must match the one that `com.zhiliaoapp`
 * emits for the running OS version.
 */
public class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(
                "User-Agent",
                "com.zhiliaoapp.musically/2023501030 (Linux; U; Android 13; en_US; Pixel 7; Build/TQ3A.230901.001; Cronet/TTNetVersion:b4d74d15 2023-09-01)",
            )
            .build()
        return chain.proceed(request)
    }
}
