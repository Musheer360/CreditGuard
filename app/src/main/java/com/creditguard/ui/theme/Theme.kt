package com.creditguard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorScheme = darkColorScheme(
    primary = PlatinumSilver,
    onPrimary = PureBlack,
    secondary = SoftSilver,
    background = PureBlack,
    onBackground = Color.White,
    surface = PureBlack,
    onSurface = Color.White,
    surfaceVariant = CardSurface,
    onSurfaceVariant = SecondaryText,
    outline = TertiaryText,
    error = ErrorRed
)

@Composable
fun CreditGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}
