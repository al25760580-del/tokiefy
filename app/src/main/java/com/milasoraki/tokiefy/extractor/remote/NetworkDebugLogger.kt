package com.milasoraki.tokiefy.extractor.remote

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList

/**
 * In-memory HTTP log ring buffer plus HttpLoggingInterceptor-compatible
 * logger.
 *
 * Why it exists:
 * The user asked to be able to see *what the server returns to the
 * client* directly from the app while iterating on API integration.
 * Android's logcat is invisible to most testers, so we keep a bounded
 * ring buffer of recent HTTP lines and expose them as a StateFlow that
 * the debug screen can observe.
 *
 * Capacity is deliberately small (120 lines / ~16 KB) to avoid keeping
 * megabytes of response bodies in memory.
 */
public object NetworkDebugLogger {

    private const val TAG: String = "TokiefyHTTP"
    private const val MAX_LINES: Int = 180
    private val buffer: LinkedList<String> = LinkedList()

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    /** Hot stream of recent log lines (newest at the end). */
    public val lines: StateFlow<List<String>> = _lines.asStateFlow()

    private val _lastStatus = MutableStateFlow(Status(0, "", ""))
    /** Short status of the most recent request (method, code, url-path). */
    public val lastStatus: StateFlow<Status> = _lastStatus.asStateFlow()

    /** Most recent uncaught error message (empty if none). */
    private val _lastError = MutableStateFlow("")
    public val lastError: StateFlow<String> = _lastError.asStateFlow()

    /** HttpLoggingInterceptor.Logger adapter. */
    public val okHttpLogger: (String) -> Unit = { message ->
        Log.d(TAG, message)
        synchronized(buffer) {
            buffer.add(message)
            while (buffer.size > MAX_LINES) buffer.removeFirst()
            _lines.value = buffer.toList()
        }
        updateStatusFrom(message)
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

    /** Clears the ring buffer (e.g. on logout). */
    public fun clear() {
        synchronized(buffer) {
            buffer.clear()
            _lines.value = emptyList()
        }
        _lastStatus.value = Status(0, "", "")
        _lastError.value = ""
    }

    public fun recordError(message: String) {
        val line = "ERROR: $message"
        Log.e(TAG, line)
        synchronized(buffer) {
            buffer.add(line)
            while (buffer.size > MAX_LINES) buffer.removeFirst()
            _lines.value = buffer.toList()
        }
        _lastError.value = message
    }

    /** Snapshot of a single response's code for the UI chip. */
    public data class Status(
        val code: Int,
        val method: String,
        val url: String,
    )
}
