package com.milasoraki.tokiefy.ui.feat.home

import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.extractor.model.feed.Aweme
import com.milasoraki.tokiefy.ui.components.Avatar
import com.milasoraki.tokiefy.ui.theme.TikTokPrimary

/** "For You" vertical feed screen. */
@Composable
public fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        FeedTopTabs()
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
            else -> FeedVerticalPager(items = uiState.items)
        }
    }
}

@Composable
private fun FeedTopTabs() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feed_tab_following),
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Box(modifier = Modifier.size(width = 2.dp, height = 18.dp).background(Color.White.copy(alpha = 0.4f)))
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Text(
            text = stringResource(R.string.feed_tab_for_you),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun FeedVerticalPager(items: List<Aweme>) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val current: Aweme = items.getOrElse(selectedIndex) { sampleItem() }
    Box(modifier = Modifier.fillMaxSize()) {
        AwemeCard(item = current)
        // Left/right tap zones are a stand-in for a VerticalPager until we
        // add a real pager; they keep the scaffold responsive.
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .tapNoRipple { if (selectedIndex > 0) selectedIndex -= 1 },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .tapNoRipple { if (selectedIndex < items.lastIndex) selectedIndex += 1 },
            )
        }
    }
}

@Composable
private fun BoxScope.AwemeCard(item: Aweme) {
    var liked by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
        AsyncImage(
            model = item.video?.cover?.urlList?.firstOrNull() ?: "https://picsum.photos/seed/feed/400/700",
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)))
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(200.dp)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
    )
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 12.dp, bottom = 80.dp, end = 72.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(imageUrl = item.author?.avatarUrl(), size = 38.dp)
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
                text = "@${item.author?.uniqueId ?: "tiktok"}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Button(
                onClick = { /* TODO(RELATION): follow */ },
                colors = ButtonDefaults.buttonColors(containerColor = TikTokPrimary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(26.dp),
            ) {
                Text(
                    stringResource(R.string.action_follow),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.description.ifBlank { stringResource(R.string.feed_default_hashtags) },
            color = Color.White,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            Text(stringResource(R.string.video_original_sound), color = Color.White, fontSize = 12.sp)
        }
    }
    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 80.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = { liked = !liked }) {
            Icon(
                imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = if (liked) TikTokPrimary else Color.White,
                modifier = Modifier.size(34.dp),
            )
        }
        Text("${(item.statistics?.diggCount ?: 1234) + if (liked) 1 else 0}", color = Color.White, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Icon(Icons.Filled.ChatBubble, contentDescription = stringResource(R.string.action_comments), tint = Color.White, modifier = Modifier.size(30.dp))
        Text("${item.statistics?.commentCount ?: 0}", color = Color.White, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        IconButton(onClick = { saved = !saved }) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = stringResource(R.string.action_save),
                tint = if (saved) TikTokPrimary else Color.White,
                modifier = Modifier.size(32.dp),
            )
        }
        Text(stringResource(R.string.action_save), color = Color.White, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share), tint = Color.White, modifier = Modifier.size(30.dp))
        Text("${item.statistics?.shareCount ?: 0}", color = Color.White, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Avatar(imageUrl = item.author?.avatarUrl(), size = 42.dp)
    }
}

private fun sampleItem(): Aweme = Aweme(description = "#fyp #viral #foryou")

@Composable
private fun Modifier.tapNoRipple(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null as Indication?,
    onClick = onClick,
)
