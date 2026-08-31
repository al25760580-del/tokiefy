package com.milasoraki.tokiefy.ui.feat.profile

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.AccountData
import com.milasoraki.tokiefy.extractor.remote.NetworkDebugLogger
import com.milasoraki.tokiefy.ui.components.DebugConsole
import com.milasoraki.tokiefy.ui.theme.TikTokPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Profile tab.
 *
 * Fetches `/passport/account/info/v2/` over the **web** client so it
 * works with the WebView-captured sessionid without needing native
 * X-Argus signing. The debug bug is always visible in the top-left so
 * the tester can inspect why a call failed even if the profile has
 * not loaded yet.
 */
@Composable
public fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var debugOpen by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.load() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val account = state.account
            val avatarUrl = account?.avatar()
            AsyncImage(
                model = avatarUrl ?: "https://picsum.photos/seed/me/120/120",
                contentDescription = null,
                modifier = Modifier.size(96.dp).clip(CircleShape),
            )
            when {
                state.loading -> {
                    Text(
                        text = stringResource(R.string.profile_loading),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    )
                    CircularProgressIndicator(color = TikTokPrimary, modifier = Modifier.size(22.dp))
                }
                account != null -> {
                    Text(
                        text = "@${account.handle() ?: "user"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                    Text(
                        text = "uid ${account.resolvedUserId()}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                    )
                    if (!account.email.isNullOrBlank()) {
                        Text(
                            text = account.email,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                        )
                    }
                }
                state.loggedIn -> {
                    // Logged in but account info call failed — still show
                    // the logout button so the user can retry or sign out.
                    Text(
                        text = stringResource(R.string.profile_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                    Text(
                        text = stringResource(R.string.profile_subtitle),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.profile_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    )
                    Text(
                        text = stringResource(R.string.profile_subtitle),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                    )
                }
            }
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                )
            }
            if (state.loggedIn) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { scope.launch { viewModel.logout() } },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.6f),
                ) {
                    Text(stringResource(R.string.profile_logout), color = Color.White)
                }
            }
        }
        DebugConsole(
            open = debugOpen,
            onToggle = { debugOpen = !debugOpen },
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 4.dp, top = 4.dp),
        )
    }
}

public data class ProfileState(
    val loading: Boolean = true,
    val loggedIn: Boolean = false,
    val account: AccountData? = null,
    val error: String? = null,
)

public class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ProfileState())
    public val state: StateFlow<ProfileState> = _state.asStateFlow()

    public fun load() {
        viewModelScope.launch {
            val sm = ServiceLocator.sessionManager
            val loggedIn = sm.isLoggedIn()
            _state.value = _state.value.copy(loading = true, loggedIn = loggedIn, error = null)
            if (!loggedIn) {
                _state.value = _state.value.copy(loading = false)
                return@launch
            }
            val result = runCatching { ServiceLocator.api.account.accountInfo() }
            result.onFailure { err ->
                NetworkDebugLogger.recordError("passport/account/info: ${err.message}")
                val msg = err.message
                    ?.substringBefore("\n")
                    ?.take(120)
                    ?: "Failed to fetch profile"
                _state.value = _state.value.copy(loading = false, error = msg)
            }
            result.onSuccess { resp ->
                if (resp.isSuccess() && resp.data != null) {
                    _state.value = _state.value.copy(loading = false, account = resp.data, error = null)
                } else {
                    val msg = "account/info msg=${resp.message} code=${resp.statusCode}/${resp.errorCode}"
                    NetworkDebugLogger.recordError(msg)
                    _state.value = _state.value.copy(loading = false, error = msg)
                }
            }
        }
    }

    public suspend fun logout() {
        ServiceLocator.sessionManager.clear()
        NetworkDebugLogger.clear()
        _state.value = ProfileState(loading = false, loggedIn = false)
    }
}
