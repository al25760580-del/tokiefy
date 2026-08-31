package com.milasoraki.tokiefy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.ui.theme.TikTokAccent
import com.milasoraki.tokiefy.ui.theme.TikTokPrimary

/**
 * Root tab identifiers.
 *
 * An enum (instead of free-form strings) allows the `when` expressions in
 * `MainScaffold` to be verified exhaustive at compile time. Labels are
 * resolved from locale resources at call-site so this enum stays locale-
 * agnostic.
 */
public enum class RootTab(
    public val icon: ImageVector,
    public val selectedIcon: ImageVector,
    public val labelResId: Int,
    public val badgeCount: Int = 0,
) {
    HOME(Icons.Filled.Home, Icons.Filled.Home, R.string.tab_home),
    // Friends uses a small text badge instead of the extended People icon.
    FRIENDS(Icons.Filled.Star, Icons.Filled.Star, R.string.tab_friends),
    INBOX(Icons.Filled.ChatBubble, Icons.Filled.ChatBubble, R.string.tab_inbox, badgeCount = 3),
    PROFILE(Icons.Filled.Person, Icons.Filled.Person, R.string.tab_profile),
}

/**
 * TikTok-styled bottom navigation bar.
 *
 * The centre "Create" button is a white rounded rectangle with cyan/red
 * offset shadows that mimic the TikTok logo optical effect. It is not
 * modelled as a tab because it opens a camera screen rather than switching
 * the current content view.
 */
@Composable
public fun TikTokBottomBar(
    currentTab: RootTab,
    onTabSelected: (RootTab) -> Unit,
    onCreateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black,
        contentColor = Color.White,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            TabItem(
                tab = RootTab.HOME,
                selected = currentTab == RootTab.HOME,
                onClick = { onTabSelected(RootTab.HOME) },
                modifier = Modifier.weight(1f),
            )
            TabItem(
                tab = RootTab.FRIENDS,
                selected = currentTab == RootTab.FRIENDS,
                onClick = { onTabSelected(RootTab.FRIENDS) },
                modifier = Modifier.weight(1f),
                labelOverride = "👥",
            )
            CreateButton(onClick = onCreateClicked)
            TabItem(
                tab = RootTab.INBOX,
                selected = currentTab == RootTab.INBOX,
                onClick = { onTabSelected(RootTab.INBOX) },
                modifier = Modifier.weight(1f),
            )
            TabItem(
                tab = RootTab.PROFILE,
                selected = currentTab == RootTab.PROFILE,
                onClick = { onTabSelected(RootTab.PROFILE) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TabItem(
    tab: RootTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelOverride: String? = null,
) {
    val tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box {
            if (labelOverride != null) {
                Text(
                    text = labelOverride,
                    fontSize = 22.sp,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.icon,
                    contentDescription = stringResource(tab.labelResId),
                    tint = tint,
                    modifier = Modifier.size(24.dp),
                )
            }
            if (tab.badgeCount > 0) {
                NotificationBadge(
                    count = tab.badgeCount,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp),
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = stringResource(tab.labelResId),
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(top = 28.dp),
        )
    }
}

@Composable
private fun CreateButton(onClick: () -> Unit) {
    Box(
        Modifier
            .width(58.dp)
            .height(36.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Cyan (left) and red (right) offset shadows create the signature
        // TikTok logo optical effect around the white "+" pill.
        Box(
            Modifier
                .matchParentSize()
                .offset(x = (-3).dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TikTokAccent)
        )
        Box(
            Modifier
                .matchParentSize()
                .offset(x = 3.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TikTokPrimary)
        )
        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_create), tint = Color.Black)
        }
    }
}
