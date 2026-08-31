package com.milasoraki.tokiefy.extractor.api.interceptor

import com.milasoraki.tokiefy.extractor.api.TikTokAppIds
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

/**
 * Appends query parameters that every TikTok API request must carry.
 *
 * Why it exists:
 * Every request — even unauthenticated ones — requires `aid`, `device_id`,
 * `cdid`, `openudid`, `channel`, `os_api` etc. Centralising them in an
 * interceptor guarantees Retrofit interfaces stay clean and every call
 * sends a consistent set of parameters.
 *
 * Trade-offs:
 * | Approach                         | Pros                           | Cons                              |
 * |----------------------------------|--------------------------------|-----------------------------------|
 * | Interceptor (chosen)             | One place; can't be forgotten  | Harder to override per-call       |
 * | @Query on each Retrofit method   | Explicit per-call              | Boilerplate; easy to miss one     |
 * | OkHttp `addQueryParameter` util  | Similar to interceptor         | Duplicates across clients         |
 *
 * @param params  precomputed stable identifiers (device id, openudid …).
 */
public class CommonParamsInterceptor(
    private val params: Params,
) : Interceptor {

    /**
     * Stable identifiers used as common query parameters.
     *
     * @property cdid        Per-request unique identifier generated as the
     *                       MD5 of a timestamp; ensures each request carries
     *                       distinct query parameters so the server does not
     *                       reject it as a replay.
     * @property openudid    Stable per-install identifier persisted across
     *                       launches via encrypted preferences; prevents the
     *                       installation from looking like a brand-new device
     *                       on every cold start.
     * @property deviceId    Synthetic numeric device id required by feed and
     *                       relation endpoints that expect an IMEI/IDFA-like
     *                       stable value.
     * @property channel     Distribution channel (Play Store, APKPure, etc.).
     *                       Defaults to `"googleplay"`.
     * @property appVersion  Application version sent with every request; kept
     *                       in sync with Gradle's `versionName`.
     * @property versionCode Numeric internal version; the backend compares
     *                       this (not [appVersion]) to gate features.
     * @property aid         Application id on TikTok's servers. `1180` maps
     *                       to the international Android build; other values
     *                       target Douyin or TikTok Lite.
     * @property osApi       Android SDK level; drives codec and ad-format
     *                       selection server-side.
     * @property deviceType  Device model as reported by `Build.MODEL`.
     * @property buildNumber Internal build tag including the channel suffix;
     *                       surfaced in server diagnostics.
     */
    public data class Params(
        val cdid: String,
        val openudid: String,
        val deviceId: Long,
        val channel: String,
        val appVersion: String,
        val versionCode: Long,
        val aid: Int,
        val osApi: Int,
        val deviceType: String,
        val buildNumber: String,
    ) {
        public companion object {
            /** Builds a sane default parameter set for development builds. */
            public fun default(): Params = Params(
                cdid = freshCdid(),
                openudid = "0a1b2c3d4e5f6789",
                deviceId = 7348912345678901234L,
                channel = "googleplay",
                appVersion = TikTokAppIds.VERSION_NAME,
                versionCode = TikTokAppIds.VERSION_CODE.toLong(),
                aid = TikTokAppIds.AID,
                osApi = 33,
                deviceType = "Pixel 7",
                buildNumber = "${TikTokAppIds.VERSION_NAME}_${TikTokAppIds.VERSION_CODE}",
            )
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url: HttpUrl = original.url.newBuilder()
            .addQueryParameter("aid", params.aid.toString())
            .addQueryParameter("app_name", TikTokAppIds.APP_NAME)
            .addQueryParameter("version_code", params.versionCode.toString())
            .addQueryParameter("version_name", params.appVersion)
            .addQueryParameter("device_platform", "android")
            .addQueryParameter("os_version", "13")
            .addQueryParameter("os_api", params.osApi.toString())
            .addQueryParameter("device_type", params.deviceType)
            .addQueryParameter("device_id", params.deviceId.toString())
            .addQueryParameter("openudid", params.openudid)
            .addQueryParameter("cdid", freshCdid())
            .addQueryParameter("channel", params.channel)
            .addQueryParameter("build_number", params.buildNumber)
            .addQueryParameter("manifest_version_code", params.versionCode.toString())
            .addQueryParameter("update_version_code", params.versionCode.toString())
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }

    public companion object {
        /** Generates a fresh `cdid` per request, matching official client behaviour. */
        public fun freshCdid(): String {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(System.nanoTime().toString().toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
