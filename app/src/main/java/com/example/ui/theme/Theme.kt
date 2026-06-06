package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = ThemeState.isDarkMode,
    content: @Composable () -> Unit
) {
    // Dynamic generation during recomposition ensures perfect sync
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = AccentTeal,
            onPrimary = LightSurface,
            primaryContainer = DarkSurface,
            onPrimaryContainer = TextPrimary,
            secondary = AccentViolet,
            onSecondary = LightSurface,
            background = DarkBg,
            surface = DarkSurface,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
            error = AccentRose,
            outline = DarkBorder
        )
    } else {
        lightColorScheme(
            primary = AccentTeal,
            onPrimary = LightSurface,
            primaryContainer = LightBorder,
            onPrimaryContainer = LightTextPrimary,
            secondary = AccentViolet,
            onSecondary = LightSurface,
            background = LightBg,
            surface = LightSurface,
            onBackground = LightTextPrimary,
            onSurface = LightTextPrimary,
            error = AccentRose,
            outline = LightBorder
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
