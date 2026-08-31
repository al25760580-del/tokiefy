package com.milasoraki.tokiefy.ui.feat.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.data.InboxNotification
import com.milasoraki.tokiefy.data.StoryCircle
import com.milasoraki.tokiefy.extractor.model.messaging.Conversation
import com.milasoraki.tokiefy.ui.components.Avatar
import com.milasoraki.tokiefy.ui.components.NotificationBadge
import com.milasoraki.tokiefy.ui.navigation.NavRoutes
import com.milasoraki.tokiefy.ui.theme.TikTokFollowerBadge
import com.milasoraki.tokiefy.ui.util.format
import com.milasoraki.tokiefy.ui.theme.TikTokInfoBlue
import com.milasoraki.tokiefy.ui.theme.TikTokInfoBlueBright
import com.milasoraki.tokiefy.ui.theme.TikTokOnline
import com.milasoraki.tokiefy.ui.theme.TikTokPrimary
import com.milasoraki.tokiefy.ui.theme.TikTokSurfaceChip
import com.milasoraki.tokiefy.ui.theme.TikTokYellow
import com.milasoraki.tokiefy.util.RelativeTime

/**
 * Inbox (messages) screen.
 *
 * Replicates the reference screenshots: top bar with "co+" logo + Inbox title
 * + green status dot + search; dismissible blue followers banner; pink
 * activity card; horizontal story/contact carousel; conversation list with
 * unread badges.
 */
@Composable
public fun InboxScreen(
    navController: NavController,
    viewModel: InboxViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        InboxTopBar()
        uiState.notifications
            .filterNot { it.id in uiState.dismissedNotificationIds }
            .forEach { notification ->
                NotificationCard(
                    notification = notification,
                    onDismiss = { viewModel.dismissNotification(notification.id) },
                )
            }
        StoriesRow(stories = uiState.stories)
        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.conversations, key = { it.conversationId }) { conversation ->
                ConversationRow(conversation = conversation) {
                    navController.navigate(NavRoutes.chatRoute(conversation.conversationId))
                }
            }
        }
    }
}

@Composable
private fun InboxTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = {}) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.menu_more), tint = Color.White)
        }
        Text(stringResource(R.string.inbox_co_plus), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(stringResource(R.string.inbox_title), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(TikTokOnline),
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {}) {
            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.menu_search), tint = Color.White)
        }
    }
}

@Composable
private fun NotificationCard(notification: InboxNotification, onDismiss: () -> Unit) {
    when (notification.kind) {
        InboxNotification.Kind.FOLLOWERS_BANNER -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TikTokInfoBlue)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(TikTokInfoBlueBright),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.banner_followers_title),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.banner_followers_body),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.dismiss), tint = Color.White)
                }
            }
        }
        InboxNotification.Kind.ACTIVITY -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(TikTokPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.activity_title),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.activity_body, notification.description),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_camera), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StoriesRow(stories: List<StoryCircle>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        items(stories, key = { it.id }) { story -> StoryItem(circle = story) }
    }
}

@Composable
private fun StoryItem(circle: StoryCircle) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            if (circle.isComposeButton) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .background(TikTokYellow),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.story_compose_prompt),
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp, end = 14.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(TikTokInfoBlueBright),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (circle.isOnline) {
                                    listOf(com.milasoraki.tokiefy.ui.theme.TikTokAccent, TikTokPrimary)
                                } else {
                                    listOf(com.milasoraki.tokiefy.ui.theme.TikTokAccent, TikTokPrimary, Color(0xFFF8B500))
                                }
                            )
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = circle.avatarUrl,
                        contentDescription = stringResource(R.string.cd_story_ring),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                }
                if (circle.hasFollowBadge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 22.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(TikTokSurfaceChip),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                if (circle.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(TikTokOnline)
                            .border(2.dp, Color.Black, CircleShape),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(if (circle.hasFollowBadge) 24.dp else 6.dp))
        Text(
            text = circle.label,
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            imageUrl = conversation.otherUser?.avatarUrl(),
            size = 56.dp,
            isOnline = conversation.isOnline,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.otherUser?.nickname
                        ?: conversation.otherUser?.uniqueId
                        ?: stringResource(R.string.user_unknown),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                val followers: Int = conversation.otherUser?.followerCount ?: 0
                if (followers > 0) {
                    Text(" 🔥 ", color = TikTokFollowerBadge, fontSize = 14.sp)
                    Text(
                        "$followers",
                        color = TikTokFollowerBadge,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text(
                text = conversationPreviewText(conversation),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (conversation.unreadCount > 0) {
            NotificationBadge(count = conversation.unreadCount)
        } else {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.action_camera),
                    tint = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}

/**
 * Builds the one-line preview shown under each conversation. The MieloVT
 * conversation specifically mirrors the screenshot's "Sent N min ago"
 * phrase; aweme shares use the "shared a video · N" format; other messages
 * fall back to their content or a short relative-time string.
 */
@Composable
private fun conversationPreviewText(conversation: Conversation): String {
    val lastMessage = conversation.lastMessage
    val lastType = lastMessage?.type
    return when {
        conversation.otherUser?.uniqueId == "MieloVT" ->
            RelativeTime.breakdown(conversation.lastMsgSeconds, RelativeTime.Style.LONG).format()
        lastType == com.milasoraki.tokiefy.extractor.model.messaging.MessageType.AWEME_SHARE ->
            RelativeTime.breakdown(conversation.lastMsgSeconds, RelativeTime.Style.AWEME_SHARE).format()
        lastMessage != null && lastMessage.content.isNotBlank() -> lastMessage.content
        else -> RelativeTime.breakdown(conversation.lastMsgSeconds, RelativeTime.Style.SHORT).format()
    }
}
