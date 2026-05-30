package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun FlowingGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // High-fidelity hardware-accelerated fluid liquid canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Neon Cyan glowing liquid pool (Top-Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.16f), NeonCyan.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.15f),
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = Offset(w * 0.85f, h * 0.15f)
            )

            // 2. Neon Purple flowing center aura (Middle-Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPurple.copy(alpha = 0.14f), NeonPurple.copy(alpha = 0.03f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.55f),
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.15f, h * 0.55f)
            )

            // 3. Neon Pink glowing reservoir (Bottom-Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPink.copy(alpha = 0.15f), NeonPink.copy(alpha = 0.02f), Color.Transparent),
                    center = Offset(w * 0.8f, h * 0.85f),
                    radius = w * 0.75f
                ),
                radius = w * 0.75f,
                center = Offset(w * 0.8f, h * 0.85f)
            )
        }

        // Screen content rendered over the glass background
        content()
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    glowOpacity: Float = 0.12f,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            // Double-layer base styling for high refractivity "liquid glass"
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x3B1F1F2F), // Frosted dark glass
                        Color(0x1F141424)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            // Custom physical glass edge border
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f), // Specular light highlight on top edge
                        accentColor.copy(alpha = glowOpacity * 2.5f), // Edge glow
                        Color.White.copy(alpha = 0.04f) // Fade out bottom edge refraction
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            )
            .then(clickModifier)
            .padding(1.dp) // Offset content inside border lines
    ) {
        // Specular highlight gleam overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.01f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Internal content layout
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val activeAlpha = if (enabled && !isLoading) 1f else 0.5f
    val buttonColors = if (enabled && !isLoading) {
        listOf(
            accentColor.copy(alpha = 0.25f),
            accentColor.copy(alpha = 0.08f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.01f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(
                Brush.horizontalGradient(
                    colors = buttonColors
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f * activeAlpha),
                        accentColor.copy(alpha = 0.5f * activeAlpha),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(50.dp)
            )
            .clickable(
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glass specular swipe light accent
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f * activeAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = accentColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text.uppercase(),
                color = textColor.copy(alpha = activeAlpha),
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.8.sp,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
