package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FuturisticDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = NeonCyan.copy(alpha = 0.2f),
    onPrimaryContainer = NeonCyan,
    secondary = NeonPink,
    onSecondary = Color.White,
    secondaryContainer = NeonPink.copy(alpha = 0.2f),
    onSecondaryContainer = NeonPink,
    tertiary = NeonPurple,
    onTertiary = Color.White,
    tertiaryContainer = NeonPurple.copy(alpha = 0.2f),
    onTertiaryContainer = NeonPurple,
    background = DarkBackground,
    onBackground = LightText,
    surface = DarkSurface,
    onSurface = LightText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = LightText,
    error = NeonPink,
    onError = Color.White,
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = FuturisticDarkColorScheme, typography = Typography, content = content)
}
