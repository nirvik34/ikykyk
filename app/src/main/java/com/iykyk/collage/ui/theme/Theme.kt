package com.iykyk.collage.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = HotPink,
    secondary = SkyBlue,
    tertiary = LimeGreen,
    background = SoftBlack,
    surface = Charcoal,
    onPrimary = PrimaryWhite,
    onSecondary = PrimaryWhite,
    onBackground = PrimaryWhite,
    onSurface = PrimaryWhite
)

@Composable
fun IYKYKCollageTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SoftBlack.toArgb()
            window.navigationBarColor = SoftBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
