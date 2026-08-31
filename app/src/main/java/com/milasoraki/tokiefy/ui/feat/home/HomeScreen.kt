package com.milasoraki.tokiefy.ui.feat.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.data.FeedResult
import com.milasoraki.tokiefy.extractor.model.feed.Aweme
import com.milasoraki.tokiefy.extractor.model.feed.coverUrl
import com.milasoraki.tokiefy.extractor.model.feed.playUrl
import com.milasoraki.tokiefy.ui.components.Avatar
import com.milasoraki.tokiefy.ui.components.VideoPlayer
import com.milasoraki.tokiefy.ui.theme.TikTokPrimary

/** "For You" vertical video feed screen. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // Whenever the session flips from unauthenticated → authenticated
    // (i.e. right after the WebView grabs a cookie), re-fetch the feed so
    // the LIVE data replaces the SAMPLE cards without needing an app restart.
    LaunchedEffect(Unit) {
        val loggedIn = ServiceLocator.sessionManager.isLoggedIn()
        ServiceLocator.sessionManager.session.collect { session ->
            val nowLoggedIn = session.cookies.any { it.name == "sessionid" && it.value.length >= 16 && it.value != "0" }
            if (nowLoggedIn != loggedIn) {
                viewModel.refresh()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        FeedTopTabs()
        SourceChip(source = uiState.source, modifier = Modifier.align(Alignment.TopEnd))
        uiState.statusText?.let { msg ->
            Text(
                text = msg,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
        when {
            uiState.isLoading && uiState.items.isEmpty() -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
            uiState.items.isEmpty() -> Text(
                stringResource(R.string.feed_empty),
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Center),
            )
            else -> FeedVerticalPager(items = uiState.items)
        }
    }
}

@Composable
private fun SourceChip(source: FeedResult.Source, modifier: Modifier = Modifier) {
    val (label, color) = when (source) {
        FeedResult.Source.WEB_LIVE -> "LIVE" to TikTokPrimary
        FeedResult.Source.NATIVE_LIVE -> "LIVE (app)" to TikTokPrimary
        FeedResult.Source.MOCK -> "SAMPLE" to Color.White.copy(alpha = 0.55f)
    }
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 12.dp, end = 12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.25f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedVerticalPager(items: List<Aweme>) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondBoundsPageCount = 1,
    ) { page ->
        val item: Aweme = items[page]
        AwemeCard(item = item, isActive = page == pagerState.currentPage)
    }
}

@Composable
private fun AwemeCard(item: Aweme, isActive: Boolean) {
    var liked by remember(item.awemeId) { mutableStateOf(false) }
    var saved by remember(item.awemeId) { mutableStateOf(false) }
    var paused by remember { mutableStateOf(false) }

    val videoUrl: String? = item.video?.playUrl()
    val cover: String? = item.video?.coverUrl()

    Box(modifier = Modifier.fillMaxSize()) {
        // Video layer
        if (!videoUrl.isNullOrBlank()) {
            VideoPlayer(
                url = videoUrl,
                isPlaying = isActive && !paused,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AsyncImage(
                model = cover ?: "https://picsum.photos/seed/feed/400/700",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Tap to pause/play overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { paused = !paused },
                ),
        )

        // Top gradient (under top tabs)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
        )
        // Bottom gradient (behind description/actions)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
        )

        // Bottom-left caption + author + sound
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 80.dp, end = 72.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(imageUrl = item.author?.avatarUrl(), size = 38.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "@${item.author?.uniqueId ?: "tiktok"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* TODO(RELATION): follow */ },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 2.dp),
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description.ifBlank { stringResource(R.string.feed_default_hashtags) },
                color = Color.White,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.video_original_sound), color = Color.White, fontSize = 12.sp)
            }
        }

        // Right-side action rail
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

        // Pause indicator
        if (paused && !videoUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.icons.Icons.Filled
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(42.dp),
                )
            }
        }
    }
}
