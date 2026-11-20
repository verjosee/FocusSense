package com.focussense.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class ExtendedColors(
    val success: Color = SuccessGreen,
    val warning: Color = WarningAmber,
    val surfaceBorder: Color = SurfaceBorderDark,
    val textMuted: Color = TextMutedDark,
    val cardBackground: Color = DarkSlate
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

private val DarkColorScheme = darkColorScheme(
    primary = SoftViolet,
    onPrimary = DeepNight,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = SkyBlue,
    onSecondary = DeepNight,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = SkyBlue,
    background = DeepNight,
    onBackground = TextPrimaryDark,
    surface = DarkSlate,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorderDark,
    outlineVariant = SurfaceBorderDark.copy(alpha = 0.5f),
    error = ErrorRed,
    onError = DeepNight
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = LightSurface,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = SecondaryLight,
    onSecondary = LightSurface,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = SecondaryLight,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = SurfaceBorderLight,
    outlineVariant = SurfaceBorderLight.copy(alpha = 0.5f),
    error = ErrorRed,
    onError = LightSurface
)

@Composable
fun FocusSenseTheme(
    darkTheme: Boolean = true, // Default to Dark-first as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            success = SuccessGreen,
            warning = WarningAmber,
            surfaceBorder = SurfaceBorderDark,
            textMuted = TextMutedDark,
            cardBackground = DarkSlate
        )
    } else {
        ExtendedColors(
            success = Color(0xFF00B894),
            warning = Color(0xFFE17055),
            surfaceBorder = SurfaceBorderLight,
            textMuted = TextMutedLight,
            cardBackground = LightSurface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
