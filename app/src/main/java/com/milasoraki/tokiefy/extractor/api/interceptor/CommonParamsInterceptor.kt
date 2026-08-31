package com.milasoraki.tokiefy.extractor.api.interceptor

import com.milasoraki.tokiefy.extractor.api.TikTokAppIds
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import java.util.UUID

/**
 * Appends query parameters that every native TikTok API request must
 * carry. Values are taken from the live capture doc 15b (Pixel 5,
 * TikTok 46.2.3, MX region, Latam unified).
 *
 * Why an interceptor and not @Query on each Retrofit method: these
 * keys are identical across hundreds of endpoints; centralising them
 * guarantees none is omitted and lets us tweak a single constant when
 * a build bump is required.
 */
public class CommonParamsInterceptor(
    private val params: Params,
) : Interceptor {

    public data class Params(
        val cdid: String,
        val openudid: String,
        val deviceId: Long,
        val installId: Long,
        val channel: String,
        val appVersion: String,
        val versionCode: Int,
        val updateVersionCode: Long,
        val aid: Int,
        val appName: String,
        val osApi: Int,
        val osVersion: String,
        val deviceType: String,
        val deviceBrand: String,
        val resolution: String,
        val dpi: Int,
        val language: String,
        val region: String,
        val timezoneName: String,
        val timezoneOffset: Int,
    ) {
        public companion object {
            public fun default(): Params = Params(
                cdid = UUID.randomUUID().toString().replace("-", ""),
                openudid = "c581ff1403a2feb5",
                deviceId = 7680012345678901285L,
                installId = 7680012345678901240L,
                channel = "googleplay",
                appVersion = TikTokAppIds.VERSION_NAME,
                versionCode = TikTokAppIds.VERSION_CODE,
                updateVersionCode = TikTokAppIds.UPDATE_VERSION_CODE,
                aid = TikTokAppIds.AID,
                appName = TikTokAppIds.APP_NAME,
                osApi = 36,
                osVersion = "16",
                deviceType = "Pixel 5",
                deviceBrand = "google",
                resolution = "1080*2340",
                dpi = 430,
                language = "es",
                region = "MX",
                timezoneName = "America/Tijuana",
                timezoneOffset = -28800,
            )
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val nowSec = (System.currentTimeMillis() / 1000)
        val nowMs = System.currentTimeMillis()
        val url: HttpUrl = original.url.newBuilder()
            .addQueryParameter("aid", params.aid.toString())
            .addQueryParameter("app_name", params.appName)
            .addQueryParameter("version_code", params.versionCode.toString())
            .addQueryParameter("version_name", params.appVersion)
            .addQueryParameter("update_version_code", params.updateVersionCode.toString())
            .addQueryParameter("manifest_version_code", params.versionCode.toString())
            .addQueryParameter("app_version", params.updateVersionCode.toString())
            .addQueryParameter("device_platform", "android")
            .addQueryParameter("os", "android")
            .addQueryParameter("os_version", params.osVersion)
            .addQueryParameter("os_api", params.osApi.toString())
            .addQueryParameter("device_type", params.deviceType)
            .addQueryParameter("device_brand", params.deviceBrand)
            .addQueryParameter("device_id", params.deviceId.toString())
            .addQueryParameter("iid", params.installId.toString())
            .addQueryParameter("openudid", params.openudid)
            .addQueryParameter("cdid", params.cdid)
            .addQueryParameter("channel", params.channel)
            .addQueryParameter("resolution", params.resolution)
            .addQueryParameter("dpi", params.dpi.toString())
            .addQueryParameter("ac", "wifi")
            .addQueryParameter("ac2", "wifi")
            .addQueryParameter("is_pad", "0")
            .addQueryParameter("current_region", params.region)
            .addQueryParameter("residence", params.region)
            .addQueryParameter("op_region", params.region)
            .addQueryParameter("sys_region", "US")
            .addQueryParameter("language", params.language)
            .addQueryParameter("app_language", params.language)
            .addQueryParameter("os_language", params.language)
            .addQueryParameter("locale", "es-419")
            .addQueryParameter("timezone_name", params.timezoneName)
            .addQueryParameter("timezone_offset", params.timezoneOffset.toString())
            .addQueryParameter("app_type", "normal")
            .addQueryParameter("ssmix", "a")
            .addQueryParameter("ts", nowSec.toString())
            .addQueryParameter("_rticket", nowMs.toString())
            .addQueryParameter("host_abi", "arm64-v8a")
            .addQueryParameter("uoo", "1")
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}
