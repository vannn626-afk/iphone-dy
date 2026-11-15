package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LeovanDarkColorScheme = darkColorScheme(
    primary = LeovanTextPrimary,
    onPrimary = LeovanBlack,
    primaryContainer = LeovanCardSecondary,
    onPrimaryContainer = LeovanTextPrimary,
    secondary = LeovanTextSecondary,
    onSecondary = LeovanBlack,
    secondaryContainer = LeovanCardSecondary,
    onSecondaryContainer = LeovanTextPrimary,
    tertiary = StatusGreen,
    onTertiary = LeovanBlack,
    background = LeovanBlack,
    onBackground = LeovanTextPrimary,
    surface = LeovanCardPrimary,
    onSurface = LeovanTextPrimary,
    surfaceVariant = LeovanCardSecondary,
    onSurfaceVariant = LeovanTextSecondary,
    outline = LeovanBorder,
    outlineVariant = LeovanBorder,
    error = StatusRed,
    onError = LeovanTextPrimary
)

val LeovanShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun LeovanIslandTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LeovanDarkColorScheme,
        typography = Typography,
        shapes = LeovanShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    LeovanIslandTheme(content = content)
}
