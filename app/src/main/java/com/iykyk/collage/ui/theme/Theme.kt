package com.iykyk.collage.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = InkBlack,
    secondary = LimeBlock,
    tertiary = PeachAccent,
    background = CanvasBg,
    surface = SurfaceCard,
    onPrimary = PrimaryWhite,
    onSecondary = InkBlack,
    onBackground = InkBlack,
    onSurface = InkBlack
)

@Composable
fun IYKYKCollageTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CanvasBg.toArgb()
            window.navigationBarColor = CanvasBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

