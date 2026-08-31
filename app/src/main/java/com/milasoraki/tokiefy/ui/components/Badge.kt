package com.milasoraki.tokiefy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.ui.theme.TikTokPrimary

/**
 * Small circular red badge used for unread counts.
 *
 * @param count  number to display; capped at "9+" to match the screenshot.
 */
@Composable
public fun NotificationBadge(count: Int, modifier: Modifier = Modifier) {
    val label: String = if (count > 9) "9+" else count.toString()
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(TikTokPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
        )
    }
}
