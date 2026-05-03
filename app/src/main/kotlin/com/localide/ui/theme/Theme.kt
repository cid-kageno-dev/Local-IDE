package com.localide.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// IDE Dark color palette
val IdeBackground = Color(0xFF0D0D0D)
val IdeSurface = Color(0xFF1A1A1A)
val IdeSurfaceVariant = Color(0xFF242424)
val IdeSurfaceContainer = Color(0xFF1E1E1E)
val IdeAccent = Color(0xFF7C6AF7)
val IdeAccentLight = Color(0xFF9D8FFF)
val IdeGreen = Color(0xFF4EC994)
val IdeRed = Color(0xFFFF5F56)
val IdeYellow = Color(0xFFFFBD2E)
val IdeCyan = Color(0xFF5AC8FA)
val IdeOnBackground = Color(0xFFE8E8E8)
val IdeOnSurface = Color(0xFFCCCCCC)
val IdeOnSurfaceVariant = Color(0xFF888888)
val IdeBorder = Color(0xFF2A2A2A)

// Syntax highlighting colors
val SyntaxKeyword = Color(0xFFCC99CD)
val SyntaxString = Color(0xFF7EC699)
val SyntaxComment = Color(0xFF999999)
val SyntaxNumber = Color(0xFFF08D49)
val SyntaxFunction = Color(0xFF6EB1EB)
val SyntaxType = Color(0xFF4EC994)
val SyntaxAnnotation = Color(0xFFE8BF6A)
val SyntaxOperator = Color(0xFFCCC)
val SyntaxDefault = Color(0xFFCDD3DE)

private val DarkColorScheme = darkColorScheme(
    primary = IdeAccent,
    onPrimary = Color.White,
    primaryContainer = IdeAccent.copy(alpha = 0.15f),
    onPrimaryContainer = IdeAccentLight,
    secondary = IdeGreen,
    onSecondary = Color.White,
    secondaryContainer = IdeGreen.copy(alpha = 0.15f),
    onSecondaryContainer = IdeGreen,
    tertiary = IdeCyan,
    background = IdeBackground,
    onBackground = IdeOnBackground,
    surface = IdeSurface,
    onSurface = IdeOnSurface,
    surfaceVariant = IdeSurfaceVariant,
    onSurfaceVariant = IdeOnSurfaceVariant,
    surfaceContainer = IdeSurfaceContainer,
    outline = IdeBorder,
    error = IdeRed,
    onError = Color.White,
)

@Composable
fun LocalIDETheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = IdeTypography,
        content = content
    )
}
