package com.milasoraki.tokiefy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milasoraki.tokiefy.ui.theme.TikTokSurfaceVariant

/**
 * Outgoing/incoming text bubble used in the chat screen.
 *
 * Why it exists:
 * Both sides share layout logic — only the background colour and corner
 * radii differ (TikTok uses fully rounded ends on the opposite side of
 * the speaker). Encapsulating that here keeps ChatScreen focused on
 * positioning.
 *
 * @param text   text content.
 * @param isMine true for messages the current user sent (white bg).
 */
@Composable
public fun ChatTextBubble(text: String, isMine: Boolean, modifier: Modifier = Modifier) {
    val bubbleColor: Color = if (isMine) Color(0xFF3D3D3D) else TikTokSurfaceVariant
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isMine) 18.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 18.dp,
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(bubbleColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(text = text, color = Color.White, fontSize = 16.sp)
    }
}
