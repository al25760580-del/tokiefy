package com.milasoraki.tokiefy.extractor.api

import android.content.Context
import android.webkit.CookieManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * Persists the user's TikTok session.
 *
 * Two import paths:
 *   1. [saveFromWebView] — called by the embedded WebView login screen
 *      after it detects a real `sessionid`; pulls every known auth
 *      cookie straight from [CookieManager] so we preserve sid_tt,
 *      uid_tt, passport_csrf_token, ttwid, msToken, odin_tt, etc.
 *      (doc 04.5 / 15.3).
 *   2. [saveSession] — manual paste fallback for power users; only
 *      stores sessionid+uid+csrf+secUid.
 *
 * Cookies are persisted both as a single semicolon-separated raw string
 * (complete fidelity) and as a parsed list for convenient access in
 * interceptors.
 */
public class SessionManager(
    private val context: Context,
) {

    private val tiktokDotCom: HttpUrl = HttpUrl.Builder().scheme("https").host("www.tiktok.com").build()
    private val tiktokvDotCom: HttpUrl = HttpUrl.Builder().scheme("https").host("api.tiktokv.com").build()

    private val dataStore: DataStore<Preferences> get() = context.sessionDataStore

    /** Hot flow of the current session; emits updates immediately. */
    public val session: Flow<Session> = dataStore.data.map { prefs ->
        val raw: String = prefs[KEY_RAW_COOKIES].orEmpty()
        val uid: String = prefs[KEY_UID].orEmpty()
        val secUid: String = prefs[KEY_SEC_UID].orEmpty()
        if (raw.isBlank()) {
            Session()
        } else {
            val cookies = parseCookiesForUrl(raw, tiktokDotCom) + parseCookiesForUrl(raw, tiktokvDotCom)
            val csrf = cookies.firstOrNull {
                it.name == "passport_csrf_token" || it.name == "csrftoken" || it.name == "tt_csrf_token"
            }?.value.orEmpty()
            Session(
                cookies = cookies.distinctBy { "${it.name}@${it.domain}" },
                userId = uid,
                secUid = secUid,
                csrfToken = csrf,
            )
        }
    }

    /** Synchronous (suspended) accessor for one-shot reads. */
    public suspend fun current(): Session = session.first()

    /** True when a non-placeholder sessionid is present. */
    public suspend fun isLoggedIn(): Boolean {
        val s = current()
        return s.cookies.any {
            it.name == "sessionid" && it.value.length >= 16 && it.value != "0"
        }
    }

    /**
     * Imports cookies from the WebView's cookie jar.
     *
     * Called by the login screen after onPageFinished detects a valid
     * sessionid. We pull the whole raw Cookie header for tiktok.com and
     * for tiktokv.com (the WebView may have been redirected across the
     * two) and persist it as one string; the Session flow parses it
     * back into OkHttp Cookies for interceptors.
     */
    public suspend fun saveFromWebView(
        handleHint: String = "",
    ) {
        val cm = CookieManager.getInstance()
        val tiktokCookies: String = cm.getCookie("https://www.tiktok.com").orEmpty()
        val tiktokvCookies: String = cm.getCookie("https://api.tiktokv.com").orEmpty()
        val raw = listOf(tiktokCookies, tiktokvCookies).filter { it.isNotBlank() }.joinToString("; ")
        val map = parseCookieMap(raw)
        dataStore.edit { prefs ->
            prefs[KEY_RAW_COOKIES] = raw
            prefs[KEY_UID] = map["uid"].orEmpty()
            prefs[KEY_CSRF] = (map["passport_csrf_token"]
                ?: map["csrftoken"]
                ?: map["tt_csrf_token"]).orEmpty()
            prefs[KEY_SEC_UID] = handleHint
        }
    }

    /** Manual-paste fallback (power-user/debug). */
    public suspend fun saveSession(sessionId: String, uid: String = "", csrf: String = "", secUid: String = "") {
        dataStore.edit { prefs ->
            prefs[KEY_RAW_COOKIES] = "sessionid=$sessionId; sid_tt=$sessionId; uid=$uid; " +
                "passport_csrf_token=$csrf; csrftoken=$csrf"
            prefs[KEY_UID] = uid.trim()
            prefs[KEY_CSRF] = csrf.trim()
            prefs[KEY_SEC_UID] = secUid.trim()
        }
    }

    /** Clears the stored session (logs out). */
    public suspend fun clear() {
        dataStore.edit { it.clear() }
        CookieManager.getInstance().removeAllCookies(null)
    }

    private fun parseCookieMap(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        val out = HashMap<String, String>(16)
        for (part in raw.split(';')) {
            val eq = part.indexOf('=')
            if (eq <= 0) continue
            out[part.substring(0, eq).trim()] = part.substring(eq + 1).trim()
        }
        return out
    }

    private fun parseCookiesForUrl(raw: String, url: HttpUrl): List<Cookie> {
        val map = parseCookieMap(raw)
        val out = ArrayList<Cookie>(map.size)
        for ((name, value) in map) {
            // Build with the passed url as context so OkHttp accepts the
            // cookie as valid for that host; the domain attribute is
            // implied by the url host (tiktok.com / tiktokv.com).
            Cookie.parse(url, "$name=$value")?.let { out += it }
        }
        return out
    }

    private companion object {
        private val KEY_RAW_COOKIES = stringPreferencesKey("raw_cookies")
        private val KEY_UID = stringPreferencesKey("uid")
        private val KEY_CSRF = stringPreferencesKey("csrf")
        private val KEY_SEC_UID = stringPreferencesKey("sec_uid")

        private val Context.sessionDataStore by preferencesDataStore(name = "tokiefy_session")
    }
}
