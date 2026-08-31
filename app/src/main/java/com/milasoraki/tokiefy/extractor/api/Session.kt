package com.milasoraki.tokiefy.extractor.api

import okhttp3.Cookie

/**
 * Immutable session data exchanged with TikTok's servers.
 *
 * @param cookies    cookies received from the server (notably `sessionid`,
 *                   `sid_guard`, `uid`, `csrftoken`).
 * @param userId     numeric user id bound to this session; used in many
 *                   user-specific endpoints.
 * @param secUid     stable user identifier used in profile endpoints.
 * @param csrfToken  CSRF token mirrored from the `csrftoken` cookie; sent
 *                   back in the `X-CSRFToken` header on POST requests.
 */
public data class Session(
    val cookies: List<Cookie> = emptyList(),
    val userId: String = "",
    val secUid: String = "",
    val csrfToken: String = "",
)

/**
 * Holds the current [Session] and exposes a mutable setter.
 *
 * Why a holder instead of a `var` on the service: interceptors (which
 * need the session) are created once when the client is built; replacing
 * the holder reference avoids rebuilding the client on login/logout.
 */
public class SessionHolder(
    private var current: Session = Session(),
) {
    public fun get(): Session = current
    public fun set(session: Session) { current = session }
}
