package com.milasoraki.tokiefy.ui.feat.login

import android.app.Application
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.SessionManager
import com.milasoraki.tokiefy.ui.theme.TikTokPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TIKTOK_WEB_URL: String = "https://www.tiktok.com/login?redirect_url=https%3A%2F%2Fwww.tiktok.com%2F&lang=en"

/**
 * Login screen.
 *
 * Two modes are offered:
 *   1. (Recommended) Embedded WebView pointing at tiktok.com/login. The user
 *      logs in with their normal TikTok credentials (Google/Facebook/email/
 *      phone). After every page load we inspect the WebView cookie jar and,
 *      the moment a valid `sessionid` cookie appears, we persist the session
 *      and finish the flow. No manual DevTools digging required.
 *   2. Manual `sessionid` paste — kept as a fallback for users who already
 *      have a session on another device or are debugging.
 *
 * Both paths route through [SessionManager.saveSession]; after that the
 * [ServiceLocator]-managed collector hot-swaps the session into the HTTP
 * client so the next API call is authenticated.
 */
@Composable
public fun LoginScreen(
    onContinue: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    when (state.mode) {
        LoginMode.Landing -> LandingContent(
            onOpenWeb = { viewModel.setMode(LoginMode.Web) },
            onOpenManual = { viewModel.setMode(LoginMode.Manual) },
            onContinueMock = onContinue,
        )
        LoginMode.Web -> WebLoginContent(
            loading = state.webLoading,
            capturedUsername = state.capturedUsername,
            onSessionCaptured = { handle ->
                scope.launch {
                    viewModel.saveCapturedSession(handle)
                    onContinue()
                }
            },
            onBack = { viewModel.setMode(LoginMode.Landing) },
            onGoManual = { viewModel.setMode(LoginMode.Manual) },
        )
        LoginMode.Manual -> ManualContent(
            state = state,
            onValueChange = { viewModel.updateSessionId(it) },
            onLogin = {
                scope.launch {
                    val ok = viewModel.login()
                    if (ok) onContinue()
                }
            },
            onBack = { viewModel.setMode(LoginMode.Landing) },
        )
    }
}

@Composable
private fun LandingContent(
    onOpenWeb: () -> Unit,
    onOpenManual: () -> Unit,
    onContinueMock: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Tokiefy",
            color = TikTokPrimary,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 42.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.login_subtitle),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onOpenWeb,
            colors = ButtonDefaults.buttonColors(containerColor = TikTokPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text(stringResource(R.string.login_action_signin_web), color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onOpenManual) {
            Text(
                stringResource(R.string.login_web_action_manual),
                color = Color.White.copy(alpha = 0.8f),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onContinueMock) {
            Text(
                stringResource(R.string.login_action_continue_mock),
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun ManualContent(
    state: LoginUiState,
    onValueChange: (String) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                text = "Tokiefy",
                color = TikTokPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 28.sp,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = state.sessionId,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.login_sessionid_hint)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = TikTokPrimary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                focusedLabelColor = TikTokPrimary,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                cursorColor = TikTokPrimary,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onLogin() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.login_help),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogin,
            enabled = state.sessionId.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = TikTokPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text(stringResource(R.string.login_action_signin), color = Color.White, fontSize = 16.sp)
        }
        if (state.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(state.error ?: "", color = TikTokPrimary, fontSize = 13.sp)
        }
    }
}

/**
 * Embedded WebView that loads TikTok's login page and sniffs cookies.
 *
 * Why AndroidView+WebView instead of Chrome Custom Tabs:
 * Custom Tabs run in a separate process so we cannot read their cookie
 * jar. A WebView owned by our process gives us direct access to
 * [CookieManager] and lets us capture the `sessionid` without asking the
 * user to dig into DevTools. JavaScript/DOM storage are enabled because
 * TikTok's login flow (Google/Facebook SSO, CAPTCHA) requires them.
 */
@Composable
private fun WebLoginContent(
    loading: Boolean,
    capturedUsername: String?,
        onSessionCaptured: (handle: String?) -> Unit,
    onBack: () -> Unit,
    onGoManual: () -> Unit,
) {
    val context = LocalContext.current
    var localLoading by remember { mutableStateOf(true) }
    var urlBar by remember { mutableStateOf(TIKTOK_WEB_URL) }
    var alreadyCaptured by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black).statusBarsPadding()) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                text = if (capturedUsername != null) {
                    stringResource(R.string.login_web_success, capturedUsername)
                } else {
                    stringResource(R.string.login_web_title)
                },
                color = TikTokPrimary,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        if (loading || localLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = TikTokPrimary,
                trackColor = Color.Transparent,
            )
        }
        Text(
            text = stringResource(R.string.login_web_subtitle),
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadsImagesAutomatically = true
                            mediaPlaybackRequiresUserGesture = false
                            userAgentString =
                                "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 " +
                                    "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        }
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                localLoading = true
                                urlBar = url.orEmpty()
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                localLoading = false
                                urlBar = url.orEmpty()
                                if (alreadyCaptured) return
                                val cm = CookieManager.getInstance()
                                val raw: String = cm.getCookie("https://www.tiktok.com").orEmpty()
                                val cookies: Map<String, String> = parseCookies(raw)
                                val sid: String = cookies["sessionid"].orEmpty()
                                if (sid.length >= 16 && sid != "0") {
                                    alreadyCaptured = true
                                    // Try to scrape @handle from the page title once the feed loads.
                                    var handle: String? = null
                                    view?.title?.let { title ->
                                        val m = Regex("""@([A-Za-z0-9_.]+)""").find(title)
                                        handle = m?.groupValues?.get(1)
                                    }
                                    stopLoading()
                                    onSessionCaptured(handle)
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                // Keep navigation inside the WebView so Google/Facebook SSO works.
                                return false
                            }
                        }
                        loadUrl(TIKTOK_WEB_URL)
                    }
                },
                update = { _ -> /* page navigation is driven by the WebView itself */ },
            )

            if (localLoading && !alreadyCaptured) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = TikTokPrimary,
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Clear any pending loads but do NOT clear cookies: the user might
            // come back to this screen and reuse the login. Cookies live until
            // the user logs out explicitly.
        }
    }
}

/**
 * Parses a raw `Cookie:`-style header into a map of name→value.
 *
 * Why hand-rolled instead of okhttp3.Cookie.parseAll: the WebView's cookie
 * manager returns cookies as a single `; ` separated string, and we only
 * need four specific names. Using a tiny split keeps the UI layer free of
 * HTTP-client imports.
 */
private fun parseCookies(raw: String): Map<String, String> {
    if (raw.isBlank()) return emptyMap()
    val out = HashMap<String, String>(8)
    for (part in raw.split(';')) {
        val eq = part.indexOf('=')
        if (eq <= 0) continue
        val name = part.substring(0, eq).trim()
        val value = part.substring(eq + 1).trim()
        if (name.isNotEmpty() && !out.containsKey(name)) out[name] = value
    }
    return out
}

/** UI mode for the login entry point. */
public enum class LoginMode { Landing, Web, Manual }

/** UI state for the login screen. */
public data class LoginUiState(
    val mode: LoginMode = LoginMode.Landing,
    val sessionId: String = "",
    val webLoading: Boolean = true,
    val capturedUsername: String? = null,
    val error: String? = null,
)

/** ViewModel that owns login UI state and persists the imported session. */
public class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager: SessionManager = ServiceLocator.sessionManager

    private val _uiState = MutableStateFlow(LoginUiState())
    public val uiState: StateFlow<LoginUiState> = _uiState

    public fun setMode(mode: LoginMode) {
        _uiState.value = _uiState.value.copy(mode = mode, error = null)
    }

    public fun updateSessionId(value: String) {
        _uiState.value = _uiState.value.copy(sessionId = value.trim(), error = null)
    }

    /** Saves a session captured by the embedded WebView by reading all cookies from the WebView cookie jar. */
    public suspend fun saveCapturedSession(handle: String?) {
        sessionManager.saveFromWebView(handleHint = handle.orEmpty())
        val session = sessionManager.current()
        _uiState.value = _uiState.value.copy(capturedUsername = handle ?: session.userId)
    }

    /** Validates and persists the manually-pasted sessionid; returns true on success. */
    public suspend fun login(): Boolean {
        val sid = _uiState.value.sessionId
        if (sid.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please paste a sessionid")
            return false
        }
        return withContext(Dispatchers.IO) {
            sessionManager.saveSession(sessionId = sid)
            true
        }
    }
}
