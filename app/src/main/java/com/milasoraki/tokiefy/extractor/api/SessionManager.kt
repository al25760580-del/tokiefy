package com.milasoraki.tokiefy.extractor.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.Cookie

/**
 * Persists the user's TikTok session.
 *
 * Why it exists:
 * The API only returns real data when the caller presents a valid
 * `sessionid` (and related cookies). Users paste this value on the
 * login screen; it is stored in DataStore (encrypted at rest on Android
 * 6+ when backed by Jetpack Security — kept simple here) and injected
 * into the HTTP client via [SessionHeadersInterceptor].
 *
 * Login (session import) is the only authentication model we support
 * right now because TikTok's official OAuth flow is closed to third
 * parties and username/password login triggers CAPTCHA + device
 * verification. Importing cookies from an existing logged-in client is
 * the same approach used by NewPipe/Xtra.
 */
public class SessionManager(
    private val context: Context,
) {

    private val dataStore: DataStore<Preferences> get() = context.sessionDataStore

    /** Hot flow of the current session; emits updates immediately. */
    public val session: Flow<Session> = dataStore.data.map { prefs ->
        val sessionId: String = prefs[KEY_SESSION_ID].orEmpty()
        val uid: String = prefs[KEY_UID].orEmpty()
        val csrf: String = prefs[KEY_CSRF].orEmpty()
        val secUid: String = prefs[KEY_SEC_UID].orEmpty()
        if (sessionId.isBlank()) {
            Session()
        } else {
            val cookies = mutableListOf<Cookie>()
            cookies += Cookie.parse("https://tiktok.com", "sessionid=$sessionId")!!
            if (csrf.isNotBlank()) {
                cookies += Cookie.parse("https://tiktok.com", "csrftoken=$csrf")!!
            }
            Session(
                cookies = cookies,
                userId = uid,
                secUid = secUid,
                csrfToken = csrf,
            )
        }
    }

    /** Synchronous (suspended) accessor for one-shot reads. */
    public suspend fun current(): Session = session.first()

    /** True when the user has saved a non-blank session id. */
    public suspend fun isLoggedIn(): Boolean = current().userId.isNotBlank() ||
        current().cookies.any { it.name == "sessionid" && it.value.isNotBlank() }

    /** Persists a session imported from the login screen. */
    public suspend fun saveSession(sessionId: String, uid: String = "", csrf: String = "", secUid: String = "") {
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_ID] = sessionId.trim()
            prefs[KEY_UID] = uid.trim()
            prefs[KEY_CSRF] = csrf.trim()
            prefs[KEY_SEC_UID] = secUid.trim()
        }
    }

    /** Clears the stored session (logs out). */
    public suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        private val KEY_SESSION_ID = stringPreferencesKey("session_id")
        private val KEY_UID = stringPreferencesKey("uid")
        private val KEY_CSRF = stringPreferencesKey("csrf")
        private val KEY_SEC_UID = stringPreferencesKey("sec_uid")

        private val Context.sessionDataStore by preferencesDataStore(name = "tokiefy_session")
    }
}
