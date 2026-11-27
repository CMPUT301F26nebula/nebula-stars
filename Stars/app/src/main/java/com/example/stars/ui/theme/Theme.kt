package com.example.stars.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.stars.StarsColors

/**
 * App theme built from the active StarsPalette, so Material components
 * (dialogs, date pickers, text fields that don't set their own colours) match it too
 */
@Composable
fun StarsTheme(content: @Composable () -> Unit) {
    val palette = StarsColors.palette
    val lightSurface = palette.surface.luminance() > 0.5f
    val lightBackground = palette.background.luminance() > 0.5f

    val base = if (lightSurface) lightColorScheme() else darkColorScheme()
    val colorScheme = base.copy(
        primary = palette.highlight,
        onPrimary = palette.onHighlight,
        secondary = palette.accent1,
        onSecondary = palette.onAccent,
        tertiary = palette.accent2,
        onTertiary = palette.onAccent,
        background = palette.surface,       // Material's "background" is what Scaffolds/dialogs fill with
        onBackground = palette.onSurface,
        surface = palette.surface,
        onSurface = palette.onSurface,
        surfaceContainer = palette.surface,
        surfaceContainerHigh = palette.surface,
        surfaceContainerHighest = palette.surface
    )

    // Status bar sits on the header (surface); nav bar sits on the background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = lightSurface        // dark icons on light header
            controller.isAppearanceLightNavigationBars = lightBackground
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}