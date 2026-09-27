package com.videorder.downloader.presentation.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BgDark,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = AccentCyan,
    secondary = AccentBlue,
    onSecondary = BgDark,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = StatusError,
    onError = BgDark
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = SurfaceLight,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = AccentBlue,
    secondary = AccentCyan,
    onSecondary = SurfaceLight,
    background = BgLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = StatusError,
    onError = SurfaceLight
)

@Composable
fun VideorderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
