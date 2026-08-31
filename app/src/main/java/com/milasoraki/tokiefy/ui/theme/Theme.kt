package com.milasoraki.tokiefy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TokiefyDarkColors = darkColorScheme(
    primary = TikTokPrimary,
    secondary = TikTokAccent,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

/** Applies the Tokiefy (TikTok dark) Material 3 theme. */
@Composable
public fun TokiefyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TokiefyDarkColors,
        typography = TokiefyTypography,
        shapes = TokiefyShapes,
        content = content,
    )
}
