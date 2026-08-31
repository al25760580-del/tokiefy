package com.milasoraki.tokiefy.ui.feat.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.extractor.model.messaging.Conversation
import com.milasoraki.tokiefy.extractor.model.messaging.DirectMessage
import com.milasoraki.tokiefy.extractor.model.messaging.MessageType
import com.milasoraki.tokiefy.extractor.model.sticker.Sticker
import com.milasoraki.tokiefy.extractor.model.sticker.StickerPack
import com.milasoraki.tokiefy.extractor.model.user.User
import com.milasoraki.tokiefy.ui.components.Avatar
import com.milasoraki.tokiefy.ui.components.ChatTextBubble
import com.milasoraki.tokiefy.ui.theme.TikTokAccent
import com.milasoraki.tokiefy.ui.theme.TikTokFollowerBadge
import com.milasoraki.tokiefy.ui.theme.TikTokSurfaceVariant

/**
 * Single 1-to-1 chat screen.
 *
 * Faithfully reproduces reference screenshot #1: top bar with avatar,
 * nickname, follower badge, online status; "today H:MM" timestamp;
 * recipient badge "misky"; bubble-style shared video with play button,
 * "Susie's Idea" label and "Ralsei" caption with a separate circular
 * share button beside it; a sticker panel with tabs, recent/saved rows
 * and "+" add button; and a rounded text input ("Message…") with camera,
 * keyboard, emoji, mic and send icons.
 *
 * @param conversationId identifier of the conversation opened from inbox.
 * @param onBack         callback for the top-left back arrow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val conversation: Conversation? = uiState.conversation
    val otherUser: User? = conversation?.otherUser

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        ChatTopAppBar(onBack = onBack, otherUser = otherUser, isOnline = conversation?.isOnline == true)

        val timestampText = String.format(stringResource(R.string.chat_timestamp_today), "5:28 p. m.")
        Text(
            text = timestampText,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp),
        )

        Box(modifier = Modifier.weight(1f)) {
            MessageList(messages = uiState.messages, otherAvatarUrl = otherUser?.avatarUrl())
            PeerBadge(peerName = otherUser?.uniqueId ?: "")
            Text(
                text = "🫧",
                fontSize = 80.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 30.dp, end = 24.dp),
            )
        }

        if (uiState.isStickerPanelOpen) {
            StickerPanel(
                recentStickers = uiState.recentStickers,
                savedPacks = uiState.savedStickerPacks,
                onPick = viewModel::sendSticker,
            )
        }

        ChatInputBar(
            draft = uiState.draft,
            onDraftChange = viewModel::updateDraft,
            stickersOpen = uiState.isStickerPanelOpen,
            onToggleStickers = { viewModel.toggleStickerPanel(!uiState.isStickerPanelOpen) },
            onSend = viewModel::sendDraftText,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopAppBar(onBack: () -> Unit, otherUser: User?, isOnline: Boolean) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(imageUrl = otherUser?.avatarUrl(), size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = otherUser?.nickname ?: otherUser?.uniqueId ?: stringResource(R.string.user_unknown),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        )
                        val followers: Int = otherUser?.followerCount ?: 0
                        if (followers > 0) {
                            Text(" 🔥", fontSize = 14.sp)
                            Text(
                                "$followers",
                                color = TikTokFollowerBadge,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Text(
                        text = if (isOnline) {
                            stringResource(R.string.chat_active_now)
                        } else {
                            stringResource(R.string.chat_active_today)
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.menu_back), tint = Color.White)
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Call, contentDescription = stringResource(R.string.menu_call), tint = Color.White)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.menu_more), tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
        modifier = Modifier.statusBarsPadding(),
    )
}

@Composable
private fun BoxScope.PeerBadge(peerName: String) {
    Row(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 12.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            AsyncImage(
                model = "https://i.pravatar.cc/100?u=misky",
                contentDescription = stringResource(R.string.cd_avatar),
                modifier = Modifier.size(40.dp).clip(CircleShape),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = 4.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(TikTokAccent.copy(alpha = 0.2f)),
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(peerName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MessageList(messages: List<DirectMessage>, otherAvatarUrl: String?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(messages.reversed(), key = { it.messageId }) { message ->
            val isMine: Boolean = message.senderUid == ChatViewModel.SENDER_SELF
            MessageRow(message = message, isMine = isMine, otherAvatarUrl = otherAvatarUrl)
        }
    }
}

@Composable
private fun MessageRow(message: DirectMessage, isMine: Boolean, otherAvatarUrl: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isMine) {
            Avatar(imageUrl = otherAvatarUrl, size = 32.dp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        when (message.type) {
            MessageType.STICKER -> {
                AsyncImage(
                    model = message.content,
                    contentDescription = stringResource(R.string.sticker_name_sticker),
                    modifier = Modifier.size(110.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
            }
            MessageType.AWEME_SHARE -> SharedVideoMessage()
            else -> ChatTextBubble(text = message.content, isMine = isMine)
        }
    }
}

@Composable
private fun SharedVideoMessage() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(width = 230.dp, height = 280.dp).clip(RoundedCornerShape(16.dp))) {
            AsyncImage(
                model = "https://picsum.photos/seed/share/400/500",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(stringResource(R.string.video_share_title), color = Color.White, fontSize = 10.sp)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.cd_play_video),
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp),
                )
            }
            Row(
                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(TikTokAccent))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    stringResource(R.string.video_share_author),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(TikTokSurfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share), tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun StickerPanel(
    recentStickers: List<Sticker>,
    savedPacks: List<StickerPack>,
    onPick: (Sticker) -> Unit,
) {
    val savedStickers: List<Sticker> = savedPacks.drop(1).flatMap { it.stickers }.ifEmpty {
        savedPacks.firstOrNull()?.stickers.orEmpty()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color(0xFF151515)),
    ) {
        LazyRow(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(R.string.menu_search),
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp),
                )
            }
            item {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TikTokSurfaceVariant)
                        .padding(6.dp),
                )
            }
            item {
                Icon(
                    Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp),
                )
            }
            item {
                Icon(
                    Icons.Filled.Face,
                    contentDescription = stringResource(R.string.chat_action_stickers),
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Text(
            text = stringResource(R.string.stickers_recent),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        )
        LazyRow(
            modifier = Modifier.height(90.dp).padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(recentStickers.take(4), key = { it.stickerId }) { sticker ->
                StickerTile(sticker = sticker, onClick = { onPick(sticker) }, size = 80.dp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.stickers_saved),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(TikTokSurfaceVariant)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.stickers_add),
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
            items(savedStickers, key = { it.stickerId }) { sticker ->
                StickerTile(sticker = sticker, onClick = { onPick(sticker) }, size = 80.dp)
            }
        }
    }
}

@Composable
private fun StickerTile(sticker: Sticker, onClick: () -> Unit, size: androidx.compose.ui.unit.Dp) {
    AsyncImage(
        model = sticker.url,
        contentDescription = sticker.displayName,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun ChatInputBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    stickersOpen: Boolean,
    onToggleStickers: () -> Unit,
    onSend: () -> Unit,
) {
    Surface(color = Color.Black, contentColor = Color.White) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = stringResource(R.string.action_camera),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(TikTokSurfaceVariant)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (draft.isEmpty()) {
                    Text(
                        stringResource(R.string.chat_input_placeholder),
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                )
            }
            IconButton(onClick = onToggleStickers) {
                Icon(
                    Icons.Filled.Keyboard,
                    contentDescription = stringResource(R.string.chat_action_keyboard),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            IconButton(onClick = onToggleStickers) {
                Icon(
                    Icons.Filled.Face,
                    contentDescription = stringResource(R.string.chat_action_stickers),
                    tint = if (stickersOpen) com.milasoraki.tokiefy.ui.theme.TikTokPrimary else Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = stringResource(R.string.chat_action_mic),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            IconButton(onClick = onSend) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = stringResource(R.string.chat_action_send),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}
