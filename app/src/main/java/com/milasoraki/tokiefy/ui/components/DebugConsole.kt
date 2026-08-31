package com.milasoraki.tokiefy.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milasoraki.tokiefy.extractor.remote.NetworkDebugLogger

/**
 * In-app HTTP debug console.
 *
 * When [open] is false a small 🐞 button with a coloured status dot is
 * rendered; when true a full-screen overlay shows the redacted log with
 * copy/clear/close actions. Sensitive values (session cookies, X-Argus,
 * X-Bogus, Authorization…) are masked before display and before being
 * copied to the clipboard — only the first and last 4 characters are
 * kept for debugging.
 */
@Composable
public fun DebugConsole(
    open: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!open) {
        val lastStatus by NetworkDebugLogger.lastStatus.collectAsState()
        val lastErr by NetworkDebugLogger.lastError.collectAsState()
        val dotColor = when {
            lastErr.isNotBlank() -> Color(0xFFFF5252)
            lastStatus.code in 200..299 -> Color(0xFF25C16E)
            lastStatus.code >= 400 -> Color(0xFFFF5252)
            else -> Color.White.copy(alpha = 0.3f)
        }
        IconButton(onClick = onToggle, modifier = modifier) {
            Icon(
                Icons.Filled.BugReport,
                contentDescription = "Debug",
                tint = dotColor,
                modifier = Modifier.size(22.dp),
            )
        }
        return
    }

    val lines by NetworkDebugLogger.lines.collectAsState()
    val lastStatus by NetworkDebugLogger.lastStatus.collectAsState()
    val lastErr by NetworkDebugLogger.lastError.collectAsState()
    val ctx = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xEE000000)),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BugReport, contentDescription = null, tint = TikTokDebugGreen)
                    Spacer(Modifier.size(6.dp))
                    Column {
                        Text(
                            "HTTP Debug Console",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                        val suffix = when {
                            lastStatus.code == 0 -> "idle"
                            lastStatus.code in 200..299 -> "OK"
                            lastStatus.code >= 400 -> "HTTP ${lastStatus.code}"
                            else -> lastStatus.code.toString()
                        }
                        Text(
                            "last: ${lastStatus.method} ${short(lastStatus.url)} — $suffix",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                        )
                        if (lastErr.isNotBlank()) {
                            Text(
                                lastErr.take(160),
                                color = Color(0xFFFF5252),
                                fontSize = 10.sp,
                            )
                        }
                    }
                }
                Row {
                    TextButton(onClick = {
                        val msg = NetworkDebugLogger.copyToClipboard()
                        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Copy", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                    TextButton(onClick = { NetworkDebugLogger.clear() }) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Clear", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    IconButton(onClick = onToggle) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "Tokens (sessionid, X-Argus, cookies…) se ocultan automáticamente antes de copiar.",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 9.sp,
            )
            Spacer(Modifier.height(4.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
                    .background(Color(0xFF0B0B0B), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                items(lines) { line ->
                    Text(
                        text = line,
                        color = colorFor(line),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        softWrap = true,
                    )
                }
                if (lines.isEmpty()) {
                    item {
                        Text(
                            "Waiting for HTTP traffic… (perform an action to see requests)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun short(url: String): String {
    if (url.isBlank()) return ""
    val q = url.indexOf('?')
    val path = if (q == -1) url else url.substring(0, q)
    return path.takeLast(60)
}

private fun colorFor(line: String): Color = when {
    line.startsWith("-->") -> Color(0xFF64B5F6)
    line.startsWith("<--") && line.substringAfter("<--").take(6).contains(" 2") -> Color(0xFF81C784)
    line.startsWith("<--") && line.substringAfter("<--").take(6).let { it.contains(" 4") || it.contains(" 5") } -> Color(0xFFEF5350)
    line.startsWith("<--") -> Color(0xFFFFD54F)
    line.startsWith("ERROR:") -> Color(0xFFFF5252)
    line.startsWith("set-cookie", ignoreCase = true) || line.startsWith("cookie:", ignoreCase = true) -> Color(0xFFCE93D8)
    line.matches(Regex("^\\s*(\"[^\"]+\"|\\w+): .+$")) && line.contains(":") -> Color(0xFFB0BEC5)
    else -> Color(0xFFEEEEEE)
}

private val TikTokDebugGreen = Color(0xFF25C16E)
