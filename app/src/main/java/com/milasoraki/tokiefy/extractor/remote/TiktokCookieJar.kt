package com.milasoraki.tokiefy.extractor.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * OkHttp [CookieJar] that is the single source of truth for cookies.
 *
 * Why we don't use the default in-memory jar:
 *   1. Cookies must survive app restarts (DataStore-backed).
 *   2. Both the native OkHttpClient (`.tiktokv.com`) and the web
 *      OkHttpClient (`.tiktok.com`) must see the SAME jar so cookies
 *      set by one lane (e.g. `msToken` from the web FYP response) are
 *      immediately visible to the other.
 *   3. The embedded WebView login flow stores captured cookies into
 *      the same map via [mergeFromWebView], and HTTP responses can
 *      add new cookies (e.g. anti-bot `msToken`, `odin_tt`, `ttwid`)
 *      on the fly — those get persisted and replayed on the next
 *      request.
 *
 * Cookie values that are expired, `Max-Age=0` or `name=deleted` are
 * dropped when observed.
 */
public class TiktokCookieJar(
    private val context: Context,
) : CookieJar {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** In-memory mirror, keyed by "${domain};${name}" → Cookie. */
    private val store: ConcurrentHashMap<String, Cookie> = ConcurrentHashMap()

    init {
        // Hydrate from DataStore on a background thread; requests issued
        // during early startup will fall back to an empty map and re-save
        // whatever the server returns.
        scope.launch {
            val raw = context.cookieDataStore.data.map { it[KEY_RAW_COOKIES].orEmpty() }.first()
            parseAllIntoStore(raw)
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        var changed = false
        for (c in cookies) {
            if (c.value.isBlank() || c.value == "deleted" || c.expiresAt < System.currentTimeMillis()) {
                val key = keyFor(c)
                if (store.remove(key) != null) changed = true
                continue
            }
            store[keyFor(c)] = c
            changed = true
        }
        if (changed) persistAsync()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val out = ArrayList<Cookie>(store.size / 2)
        val iter = store.values.iterator()
        while (iter.hasNext()) {
            val c = iter.next()
            if (c.expiresAt < now) {
                iter.remove()
                continue
            }
            if (c.matches(url)) out += c
        }
        return out
    }

    /**
     * Replace the cookie set with the values captured from the WebView
     * login (raw "; "-separated Cookie header for a host). Called after
     * the user completes the embedded login flow.
     */
    public fun mergeFromWebView(raw: String) {
        parseAllIntoStore(raw)
        // Also parse cookies against api.tiktokv.com so native endpoints
        // see them immediately without needing a Set-Cookie round trip.
        val nativeHost = HttpUrl.Builder().scheme("https").host("api.tiktokv.com").build()
        val tiktokCom = HttpUrl.Builder().scheme("https").host("www.tiktok.com").build()
        for (part in raw.split(';')) {
            val eq = part.indexOf('=')
            if (eq <= 0) continue
            val pair = "${part.substring(0, eq).trim()}=${part.substring(eq + 1).trim()}"
            Cookie.parse(tiktokCom, pair)?.let { store[keyFor(it)] = it }
            Cookie.parse(nativeHost, pair)?.let { store[keyFor(it)] = it }
        }
        persistAsync()
    }

    /** Serialises the current store into a "; "-separated Cookie string. */
    public fun snapshotRawFor(url: HttpUrl): String =
        loadForRequest(url).joinToString("; ") { "${it.name}=${it.value}" }

    // --------------------------- persistence ---------------------------

    private fun parseAllIntoStore(raw: String) {
        if (raw.isBlank()) return
        val tiktokCom = HttpUrl.Builder().scheme("https").host("www.tiktok.com").build()
        for (part in raw.split(';')) {
            val eq = part.indexOf('=')
            if (eq <= 0) continue
            val pair = "${part.substring(0, eq).trim()}=${part.substring(eq + 1).trim()}"
            Cookie.parse(tiktokCom, pair)?.let { store[keyFor(it)] = it }
        }
    }

    private fun persistAsync() {
        scope.launch {
            // Canonicalise by serialising all cookies for www.tiktok.com;
            // OkHttp will re-derive them for any other host via domain
            // matching on load.
            val tiktokCom = HttpUrl.Builder().scheme("https").host("www.tiktok.com").build()
            val raw = store.values
                .filter { it.matches(tiktokCom) }
                .distinctBy { it.name }
                .joinToString("; ") { "${it.name}=${it.value}" }
            context.cookieDataStore.edit { prefs -> prefs[KEY_RAW_COOKIES] = raw }
        }
    }

    private fun keyFor(c: Cookie): String = "${c.domain};${c.name}"

    /**
     * Domain match that understands leading-dot host cookies (RFC 6265
     * simplified). OkHttp's Cookie already validates secure/httponly on
     * its own; we just need the host-domain suffix check.
     */
    private fun Cookie.matches(url: HttpUrl): Boolean {
        val host = url.host
        val d = domain.trim().lowercase().removePrefix(".")
        return host == d || host.endsWith(".$d")
    }

    private companion object {
        private val KEY_RAW_COOKIES = stringPreferencesKey("raw_cookies")
        private val Context.cookieDataStore by preferencesDataStore(name = "tokiefy_cookies")
    }
}
