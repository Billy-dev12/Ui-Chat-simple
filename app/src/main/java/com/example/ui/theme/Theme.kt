package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode {
    SYSTEM, DARK_SLATE, DARK_OLED, LIGHT
}

private val DarkSlateColorScheme = darkColorScheme(
    primary = MinimalPrimaryDark,
    onPrimary = MinimalOnPrimaryDark,
    primaryContainer = MinimalPrimaryContainerDark,
    onPrimaryContainer = MinimalOnPrimaryContainerDark,
    background = MinimalBackgroundDark,
    onBackground = MinimalOnBackgroundDark,
    surface = MinimalSurfaceDark,
    onSurface = MinimalOnSurfaceDark,
    surfaceVariant = MinimalSurfaceVariantDark,
    onSurfaceVariant = MinimalOnSurfaceVariantDark,
    secondary = MinimalPrimaryDark,
    tertiary = MinimalPrimaryContainerDark,
    outline = MinimalOutlineDark,
    outlineVariant = MinimalBorderDark
)

private val DarkOledColorScheme = darkColorScheme(
    primary = MinimalPrimaryDark,
    onPrimary = MinimalOnPrimaryDark,
    primaryContainer = MinimalPrimaryContainerDark,
    onPrimaryContainer = MinimalOnPrimaryContainerDark,
    background = Color(0xFF000000), // Pure Black for OLED
    onBackground = MinimalOnBackgroundDark,
    surface = Color(0xFF111111),
    onSurface = MinimalOnSurfaceDark,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = MinimalOnSurfaceVariantDark,
    secondary = MinimalPrimaryDark,
    tertiary = MinimalPrimaryContainerDark,
    outline = MinimalOutlineDark,
    outlineVariant = MinimalBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalPrimaryLight,
    onPrimary = MinimalOnPrimaryLight,
    primaryContainer = MinimalContainerLight,
    onPrimaryContainer = MinimalOnContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    secondary = MinimalPrimaryLight,
    tertiary = MinimalContainerLight,
    outline = Color(0xFFC4C6D0),
    outlineVariant = Color(0xFFE1E2EC)
)

@Composable
fun AuraChatTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK_SLATE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    
    val colorScheme = when (themeMode) {
        AppThemeMode.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isSystemDark) DarkSlateColorScheme else LightColorScheme
            }
        }
        AppThemeMode.DARK_SLATE -> DarkSlateColorScheme
        AppThemeMode.DARK_OLED -> DarkOledColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
