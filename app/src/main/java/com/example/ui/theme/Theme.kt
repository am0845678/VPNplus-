package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalParsTheme = staticCompositionLocalOf { ThemePresets.RedGang }

object ParsTheme {
    val palette: ParsThemePalette
        @Composable
        @ReadOnlyComposable
        get() = LocalParsTheme.current
}

@Composable
fun ParsPingTheme(
    palette: ParsThemePalette = ThemePresets.RedGang,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = darkColorScheme(
        primary = palette.primaryAccent,
        onPrimary = Color.White,
        primaryContainer = palette.cardBg,
        onPrimaryContainer = palette.glowColor,
        secondary = palette.glowColor,
        onSecondary = palette.background,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.cardBg,
        onSurfaceVariant = palette.textSecondary,
        outline = palette.border,
        error = palette.errorRed,
        onError = Color.White
    )

    CompositionLocalProvider(LocalParsTheme provides palette) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = Typography,
            content = content
        )
    }
}
