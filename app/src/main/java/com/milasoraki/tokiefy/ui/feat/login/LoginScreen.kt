package com.milasoraki.tokiefy.ui.feat.login

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.extractor.api.SessionManager
import com.milasoraki.tokiefy.ui.theme.TokiefyTheme
import com.milasoraki.tokiefy.ui.theme.TikTokPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Login screen.
 *
 * Why a simple session-id paste form:
 * TikTok has no public OAuth flow and username/password login triggers
 * CAPTCHA + device verification. Importing a `sessionid` cookie from a
 * logged-in browser or the official app is the same approach NewPipe
 * and other third-party clients use. The user can also skip to use the
 * app in mock mode.
 *
 * @param onContinue  called once the user either pastes a session or
 *                    chooses to continue in mock/guest mode.
 */
@Composable
public fun LoginScreen(
    onContinue: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

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
        OutlinedTextField(
            value = state.sessionId,
            onValueChange = { viewModel.updateSessionId(it) },
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { scope.launch { viewModel.login(); onContinue() } }),
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
            onClick = { scope.launch { viewModel.login(); onContinue() } },
            enabled = state.sessionId.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = TikTokPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text(stringResource(R.string.login_action_signin), color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onContinue) {
            Text(
                stringResource(R.string.login_action_continue_mock),
                color = Color.White.copy(alpha = 0.8f),
            )
        }
        if (state.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(state.error ?: "", color = TikTokPrimary, fontSize = 13.sp)
        }
    }
}

/** UI state for the login screen. */
public data class LoginUiState(
    val sessionId: String = "",
    val error: String? = null,
)

/** ViewModel that saves the imported session via [SessionManager]. */
public class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager: SessionManager = ServiceLocator.sessionManager

    private val _uiState = MutableStateFlow(LoginUiState())
    public val uiState: StateFlow<LoginUiState> = _uiState

    public fun updateSessionId(value: String) {
        _uiState.value = _uiState.value.copy(sessionId = value.trim(), error = null)
    }

    public suspend fun login() {
        val sid = _uiState.value.sessionId
        if (sid.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please paste a sessionid")
            return
        }
        sessionManager.saveSession(sessionId = sid)
    }
}
