package com.milasoraki.tokiefy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.ui.theme.TikTokOnline

/**
 * Circular profile picture.
 *
 * Why it exists:
 * Avatars appear in the bottom bar, inbox, chat top bar, message list
 * and feed side rail. Wrapping Coil's `AsyncImage` here keeps the
 * fallback shape, border and online-dot badge in one place so every
 * call site looks identical.
 *
 * @param imageUrl  URL of the avatar; a coloured placeholder is used when null.
 * @param size      diameter of the avatar.
 * @param isOnline  true to render the small green "online" dot.
 * @param onClick   optional click handler (e.g. open profile).
 */
@Composable
public fun Avatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    isOnline: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else Modifier
    Box(modifier = modifier.size(size).then(clickModifier)) {
        AsyncImage(
            model = imageUrl ?: "https://i.pravatar.cc/200?u=anon",
            contentDescription = stringResource(R.string.cd_avatar),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
        )
        if (imageUrl == null) {
            Box(modifier = Modifier.size(size).clip(CircleShape).background(Color(0xFF333333)))
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .size(size / 4)
                    .clip(CircleShape)
                    .background(TikTokOnline)
                    .border(2.dp, Color.Black, CircleShape),
            )
        }
    }
}
