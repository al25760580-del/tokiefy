package com.milasoraki.tokiefy.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.milasoraki.tokiefy.ui.components.RootTab
import com.milasoraki.tokiefy.ui.components.TikTokBottomBar
import com.milasoraki.tokiefy.ui.feat.friends.FriendsScreen
import com.milasoraki.tokiefy.ui.feat.home.HomeScreen
import com.milasoraki.tokiefy.ui.feat.profile.ProfileScreen
import com.milasoraki.tokiefy.ui.navigation.AppNavHost

/**
 * Application shell: Scaffold with bottom navigation and content area.
 *
 * Why both a NavHost AND a manual tab switch:
 * The inbox tab contains the chat screen as a child destination, so a
 * NavHost owns that tab's internal navigation. The other three tabs
 * (Home/Friends/Profile) are simple full-screen Composables that swap
 * out directly. This avoids nesting NavHosts while keeping the chat
 * back stack intact.
 */
@Composable
public fun MainScaffold() {
    var selectedTab by rememberSaveable { mutableStateOf(RootTab.HOME) }
    val inboxNavController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Black,
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
