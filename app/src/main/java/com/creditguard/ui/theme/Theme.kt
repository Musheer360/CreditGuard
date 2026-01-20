package com.creditguard.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}
