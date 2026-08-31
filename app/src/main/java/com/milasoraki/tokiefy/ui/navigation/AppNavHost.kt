package com.milasoraki.tokiefy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.milasoraki.tokiefy.ui.feat.chat.ChatScreen
import com.milasoraki.tokiefy.ui.feat.inbox.InboxScreen

/**
 * Root navigation graph.
 *
 * The bottom-nav screens are owned by `MainScaffold` (they swap inside
 * the Scaffold content slot), while the chat screen is a destination on
 * top of the inbox tab. This keeps the back stack simple: pressing back
 * from a chat returns to the inbox.
 */
@Composable
public fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoutes.INBOX) {
        composable(NavRoutes.INBOX) {
            InboxScreen(navController = navController)
        }
        composable(
            route = NavRoutes.CHAT,
            arguments = listOf(
                navArgument(NavRoutes.ARG_CONVERSATION_ID) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val conversationId: String =
                backStackEntry.arguments?.getString(NavRoutes.ARG_CONVERSATION_ID).orEmpty()
            ChatScreen(
                conversationId = conversationId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
