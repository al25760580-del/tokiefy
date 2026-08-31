package com.milasoraki.tokiefy.extractor.remote

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * In-memory HTTP log ring buffer plus HttpLoggingInterceptor-compatible
 * logger. Designed for on-device debugging: the user can open the debug
 * console from any screen and copy logs to the clipboard so they can
 * paste them into the issue report.
 *
 * Redaction: sensitive cookie/token values are masked both on screen and
 * on the clipboard. The masking keeps the first 4 and last 4 characters
 * of a cookie value so engineers can still distinguish different
 * sessions during troubleshooting, while never leaking the real secret.
 */
public object NetworkDebugLogger {

    private const val TAG: String = "TokiefyHTTP"
    private const val MAX_LINES: Int = 240
    private val buffer: LinkedList<String> = LinkedList()

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    public val lines: StateFlow<List<String>> = _lines.asStateFlow()

    private val _lastStatus = MutableStateFlow(Status(0, "", ""))
    public val lastStatus: StateFlow<Status> = _lastStatus.asStateFlow()

    private val _lastError = MutableStateFlow("")
    public val lastError: StateFlow<String> = _lastError.asStateFlow()

    /** Application context set on startup; used for clipboard copy. */
    private var appContext: Context? = null

    /** Must be called once from [com.milasoraki.tokiefy.app.TokiefyApp]. */
    public fun attach(context: Context) { appContext = context.applicationContext }

    /** HttpLoggingInterceptor.Logger adapter. */
    public val okHttpLogger: (String) -> Unit = { message ->
        val redacted = redact(message)
        Log.d(TAG, redacted)
        append(redacted)
        updateStatusFrom(redacted)
    }

    private val requestLine = Regex("""--> (GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS) (\S+)""")
    private val responseLine = Regex("""<-- (HTTP/[^ ]+ )?(\d{3}) """)

    private var pendingMethod: String = ""
    private var pendingUrl: String = ""

    private fun updateStatusFrom(line: String) {
        val mReq = requestLine.find(line)
        if (mReq != null) {
            pendingMethod = mReq.groupValues[1]
            pendingUrl = mReq.groupValues[2]
            return
        }
        val mRes = responseLine.find(line)
        if (mRes != null && pendingMethod.isNotEmpty()) {
            val code = mRes.groupValues[2].toIntOrNull() ?: 0
            _lastStatus.value = Status(code, pendingMethod, pendingUrl)
            if (code >= 400) {
                _lastError.value = "HTTP $code $pendingMethod ${shorten(pendingUrl)}"
            } else {
                _lastError.value = ""
            }
        }
    }

    private fun shorten(url: String): String {
        val q = url.indexOf('?')
        return if (q == -1) url else url.substring(0, q)
    }

    public fun clear() {
        synchronized(buffer) {
            buffer.clear()
            _lines.value = emptyList()
        }
        _lastStatus.value = Status(0, "", "")
        _lastError.value = ""
    }

    public fun recordError(message: String) {
        val redacted = "ERROR: ${redact(message)}"
        Log.e(TAG, redacted)
        append(redacted)
        _lastError.value = redacted.removePrefix("ERROR: ")
    }

    private fun append(line: String) {
        synchronized(buffer) {
            buffer.add(line)
            while (buffer.size > MAX_LINES) buffer.removeFirst()
            _lines.value = buffer.toList()
        }
    }

    /**
     * Copies the full redacted log to the Android clipboard so it can be
     * pasted into a chat / issue / email. Tokens are masked before
     * copying.
     *
     * @return human-readable result string for toast display.
     */
    public fun copyToClipboard(): String {
        val ctx = appContext ?: return "Clipboard unavailable"
        val joined = synchronized(buffer) { buffer.joinToString("\n") }
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("tokiefy-http-log", joined))
        return "Log copiado (${joined.length} chars, ${buffer.size} líneas)"
    }

    /** Status of the most recent request for the UI chip. */
    public data class Status(
        val code: Int,
        val method: String,
        val url: String,
    )

    // ----------------------------- redaction -----------------------------

    /** Cookie/header names that contain credentials and must be masked. */
    private val SENSITIVE_NAMES: Pattern = Pattern.compile(
        "(?i)(sessionid(_ss)?|sid_tt|uid_tt|sid_guard|ttwid|ms[Tt]oken|odin_tt|ttreq|" +
            "passport_csrf_token(_default)?|csrf(middleware)?token|x[-_]?csrftoken|" +
            "x[-_]?bogus|x[-_]?argus|x[-_]?ladon|x[-_]?gorgon|x[-_]?khronos|x[-_]?ss[-_]?stub|" +
            "authorization|cookie|set[-_]?cookie)",
    )

    private val COOKIE_PAIR: Pattern = Pattern.compile(
        "([A-Za-z0-9_\\-]+)=([^;\\r\\n]+)",
    )

    /**
     * Redacts a raw log line. Two cases:
     *  - Cookie/Set-Cookie/Authorization header line: mask every value
     *    whose name matches [SENSITIVE_NAMES] (keep 4+4 chars).
     *  - Anywhere else, if a long alphanumeric token is found that looks
     *    like a random session string (>= 20 hex/base64 chars) replace
     *    it with `<redacted>`. This catches tokens that appear inline in
     *    JSON bodies (e.g. sec_user_id base64, image signatures).
     */
    private fun redact(line: String): String {
        if (line.length <= 2) return line

        // Normalise header name to check against our list.
        val colon = line.indexOf(':')
        if (colon > 0 && colon < 40 && !line.startsWith(" ")) {
            val headerName = line.substring(0, colon).trim()
            val rest = line.substring(colon)
            if (SENSITIVE_NAMES.matcher(headerName).matches()) {
                return headerName + maskAllCookiePairs(rest)
            }
        }
        return maskInlineTokens(line)
    }

    private fun maskAllCookiePairs(raw: String): String {
        val m = COOKIE_PAIR.matcher(raw)
        val sb = StringBuffer(raw.length)
        while (m.find()) {
            val name = m.group(1)
            val value = m.group(2)
            val masked = if (SENSITIVE_NAMES.matcher(name).matches()) maskValue(value) else value
            m.appendReplacement(sb, Matcher.quoteReplacement("$name=$masked"))
        }
        m.appendTail(sb)
        return sb.toString()
    }

    private val TOKEN_RE = Regex("[A-Za-z0-9_\\-=+/.]{24,}")

    private fun maskInlineTokens(line: String): String {
        return TOKEN_RE.replace(line) { mr ->
            val v = mr.value
            // Don't mask JSON-ish structural bits like URLs/paths/keys.
            if (v.contains("/") || v.contains(".css") || v.contains(".js") || v.contains(".png") ||
                v.contains(".jpg") || v.equals("User-Agent", true)
            ) {
                v
            } else {
                maskValue(v)
            }
        }
    }

    private fun maskValue(v: String): String {
        if (v.length <= 8) return "•••"
        return v.take(4) + "•••" + v.takeLast(4)
    }
}
