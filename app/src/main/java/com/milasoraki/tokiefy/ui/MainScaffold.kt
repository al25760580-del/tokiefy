package com.milasoraki.tokiefy.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.ui.components.RootTab
import com.milasoraki.tokiefy.ui.components.TikTokBottomBar
import com.milasoraki.tokiefy.ui.feat.friends.FriendsScreen
import com.milasoraki.tokiefy.ui.feat.home.HomeScreen
import com.milasoraki.tokiefy.ui.feat.login.LoginScreen
import com.milasoraki.tokiefy.ui.feat.profile.ProfileScreen
import com.milasoraki.tokiefy.ui.navigation.AppNavHost

/**
 * Application shell: login gate + Scaffold with bottom navigation.
 *
 * The user first sees [LoginScreen] until they either paste a valid
 * sessionid or choose "Continue in mock/guest mode". After onboarding
 * the regular bottom-nav scaffold is shown. The bottom-nav tabs swap
 * Composables directly (Home/Friends/Profile); only Inbox hosts its
 * own NavHost so chat navigation pushes/pops inside the tab.
 */
@Composable
public fun MainScaffold() {
    var onboardingDone by rememberSaveable { mutableStateOf(false) }
    var hasSession by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasSession = ServiceLocator.sessionManager.isLoggedIn()
    }

    if (!onboardingDone && !hasSession) {
        LoginScreen(onContinue = { onboardingDone = true })
        return
    }

    var selectedTab by rememberSaveable { mutableStateOf(RootTab.HOME) }
    val inboxNavController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            TikTokBottomBar(
                currentTab = selectedTab,
                onTabSelected = { tab -> selectedTab = tab },
                onCreateClicked = { /* TODO(CREATE): open camera screen */ },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                RootTab.HOME -> HomeScreen()
                RootTab.FRIENDS -> FriendsScreen()
                RootTab.INBOX -> AppNavHost(navController = inboxNavController)
                RootTab.PROFILE -> ProfileScreen()
            }
        }
    }
}
