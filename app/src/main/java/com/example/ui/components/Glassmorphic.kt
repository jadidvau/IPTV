package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Deep cyber immersive thematic colors (matching tailwind specification)
val CyberBackground = Color(0xFF050505)
val CyberSurface = Color(0xFF121217)
val NeonPurple = Color(0xFF9333EA) // Purple-600
val NeonOrchid = Color(0xFFC084FC) // Purple-400 accent
val GlowBlue = Color(0xFF6366F1) // Indigo-500
val CyberWhite = Color(0xFFF8FAFC) // Slate-50 high contrast
val CyberGray = Color(0xFF94A3B8) // Slate-400 text-white/40 fallback

/**
 * A custom modifier drawing an immersive mesh grid pattern
 * representing the "3D Net Background Effect".
 */
fun Modifier.cyberNetGrid(
    gridSpacing: Dp = 32.dp,
    gridColor: Color = Color(0xFFFFFFFF).copy(alpha = 0.02f)
): Modifier = drawBehind {
    val spacingPx = gridSpacing.toPx()
    val width = size.width
    val height = size.height

    // Draw vertical lines
    var x = 0f
    while (x < width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
        x += spacingPx
    }

    // Draw horizontal lines
    var y = 0f
    while (y < height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
        )
        y += spacingPx
    }
}

/**
 * Gradient ambient atmosphere backgrounds with top-left purple-900/20 glow
 * and bottom-right indigo-900/10 glow using relative canvas sizes.
 */
@Composable
fun ImmersiveBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .drawBehind {
                // Top-left purple glow (centered around -10% Y, -15% X, spanning 80% wide)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7E22CE).copy(alpha = 0.16f), // purple-700 soft glow
                            Color.Transparent
                        ),
                        center = Offset(size.width * -0.15f, size.height * -0.1f),
                        radius = size.width * 1.0f
                    ),
                    center = Offset(size.width * -0.15f, size.height * -0.1f),
                    radius = size.width * 1.0f
                )

                // Bottom-right indigo/blue glow (centered around 110% X, Y, spanning 80% wide)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF312E81).copy(alpha = 0.12f), // indigo-900/10 soft glow
                            Color.Transparent
                        ),
                        center = Offset(size.width * 1.15f, size.height * 1.1f),
                        radius = size.width * 1.0f
                    ),
                    center = Offset(size.width * 1.15f, size.height * 1.1f),
                    radius = size.width * 1.0f
                )
            }
            .cyberNetGrid(gridSpacing = 28.dp)
    ) {
        content()
    }
}

/**
 * A generic full-bleed gradient brush representing the atmosphere lighting
 * of nested purple/blue neon glows.
 */
fun getCyberAtmosphereBrush(): Brush {
    return Brush.radialGradient(
        colors = listOf(
            Color(0xFF581C87).copy(alpha = 0.15f), // Purple-900 glow
            Color(0xFF1E1B4B).copy(alpha = 0.08f), // Indigo-950 glow
            CyberBackground
        ),
        center = Offset.Unspecified,
        radius = Float.POSITIVE_INFINITY
    )
}

/**
 * A glassmorphic card component using semi-transparent layers,
 * thin glowing borders, and rounded corners (3xl = 24.dp ideal corner radius).
 */
@Composable
fun GlassmorphicPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    backgroundColor: Color = Color.White.copy(alpha = 0.05f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        Box(
            modifier = Modifier.padding(1.dp),
            content = content
        )
    }
}
