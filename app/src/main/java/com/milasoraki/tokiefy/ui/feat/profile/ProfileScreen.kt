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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.AccountData
import com.milasoraki.tokiefy.extractor.api.AccountInfoResponse
import com.milasoraki.tokiefy.extractor.remote.NetworkDebugLogger
import com.milasoraki.tokiefy.ui.theme.TikTokPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Profile tab.
 *
 * Shows the currently logged-in user's avatar/handle (fetched from
 * `/passport/account/info/v2/` on first composition) plus a logout
 * button and a status line indicating whether we have an active
 * session. While unauthenticated we show a simpler placeholder and a
 * prompt to sign in.
 */
@Composable
public fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { viewModel.load() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val avatarUrl = state.account?.avatarThumb?.urlList?.firstOrNull()
            AsyncImage(
                model = avatarUrl ?: "https://picsum.photos/seed/me/120/120",
                contentDescription = null,
                modifier = Modifier.size(96.dp).clip(CircleShape),
            )
            Text(
                text = when {
                    state.loading -> stringResource(R.string.profile_loading)
                    state.account != null -> "@${state.account.uniqueId ?: state.account.nickname ?: "user"}"
                    else -> stringResource(R.string.profile_title)
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
            if (state.account != null) {
                Text(
                    text = "uid ${state.account.userId}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                )
            } else if (!state.loading) {
                Text(
                    text = stringResource(R.string.profile_subtitle),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                )
            }
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                )
            }
            if (state.loading) {
                CircularProgressIndicator(color = TikTokPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            if (state.loggedIn) {
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
                NetworkDebugLogger.recordError("account/info: ${err.message}")
                _state.value = _state.value.copy(
                    loading = false,
                    error = err.message ?: "Failed to fetch profile",
                )
            }
            result.onSuccess { resp: AccountInfoResponse ->
                if (resp.statusCode == 0 && resp.data != null) {
                    _state.value = _state.value.copy(loading = false, account = resp.data)
                } else {
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "account/info status_code=${resp.statusCode}",
                    )
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
