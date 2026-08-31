package com.milasoraki.tokiefy.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Corner radius tokens for Material surfaces.
 *
 * Chosen values:
 *  - bubbles and cards: 16–22 dp to match the screenshot's round-cornered
 *    messages and sticky pill buttons.
 *  - buttons: 22 dp (pill) for the input send button.
 *  - bottom sheet: 0 — TikTok uses hard sheet edges.
 */
public val TokiefyShapes: Shapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
)
