package com.milasoraki.tokiefy.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokiefy (TikTok) palette.
 *
 * Why these values:
 * TikTok uses pure-black backgrounds for the feed and inbox (the visual
 * trademark), a pink `#FE2C55` as the primary action color, a cyan
 * `#25F4EE` for accents and the + button glow, and a dark-surface tone
 * `#1F1F1F` for chips, input bars and card backgrounds. Matching these
 * lets the UI feel faithful to the reference screenshots.
 */
public val TikTokPrimary: Color = Color(0xFFFE2C55)     // pink/red
public val TikTokAccent: Color = Color(0xFF25F4EE)      // cyan/turquoise
public val TikTokSurfaceVariant: Color = Color(0xFF1F1F1F)
public val TikTokYellow: Color = Color(0xFFF8B500)      // create button bg
public val TikTokOnline: Color = Color(0xFF25F488)      // online green dot
public val TikTokInfoBlue: Color = Color(0xFF1253BA)    // followers banner bg
public val TikTokInfoBlueBright: Color = Color(0xFF2E82FF)
public val TikTokFollowerBadge: Color = Color(0xFFFE2C55) // follower count fire
public val TikTokSurfaceChip: Color = Color(0xFF2A2A2A)
