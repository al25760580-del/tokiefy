package com.milasoraki.tokiefy.ui.components

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

/**
 * Full-screen auto-looping video player.
 *
 * Why AndroidView + ExoPlayer (Media3):
 * Compose does not ship with a video composable at 1.5.x, and Coil only
 * handles images. Using `PlayerView` (the official Media3 widget) gives
 * us hardware-accelerated rendering, looping, buffering callbacks and
 * proper surface lifecycle.
 *
 * Lifecycle:
 * The [ExoPlayer] instance is created once per composable entry and
 * released on dispose. Callers drive [isPlaying] so only the currently
 * paged-in card actually renders video; off-screen cards keep their
 * player but are paused to save battery/bandwidth.
 *
 * @param url       direct MP4/HLS URL. If null/blank a poster is shown
 *                  (handled by the caller overlay).
 * @param isPlaying true while this card is the active page.
 * @param looping   whether to loop (always true in the feed).
 */
@Composable
public fun VideoPlayer(
    url: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    looping: Boolean = true,
) {
    val context = LocalContext.current
    val exoPlayer = remember(context) {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(
                "com.zhiliaoapp.musically/2023501030 (Linux; U; Android 13; en_US; Pixel 7; Build/TQ3A.230901.001; Cronet/TTNetVersion:b4d74d15 2023-09-01)",
            )
        val mediaSourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                repeatMode = if (looping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                playWhenReady = false
            }
    }

    DisposableEffect(url) {
        if (!url.isNullOrBlank()) {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            exoPlayer.prepare()
        }
        onDispose {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.pause()
            exoPlayer.playWhenReady = false
        }
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                useController = false
                setShutterBackgroundColor(android.graphics.Color.BLACK)
                player = exoPlayer
            }
        },
    )
}
