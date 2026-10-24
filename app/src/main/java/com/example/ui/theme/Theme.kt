package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Material 3 Light Color Scheme for MyanmarConnect
 * Primary: Peacock Teal (#006B65)
 * Secondary: Golden Amber (#C07E00)
 */
private val LightColorScheme = lightColorScheme(
    primary = PeacockPrimary,
    onPrimary = PeacockOnPrimary,
    primaryContainer = PeacockPrimaryContainer,
    onPrimaryContainer = PeacockOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,
    tertiary = DeepTealTertiary,
    onTertiary = DeepTealOnTertiary,
    tertiaryContainer = DeepTealTertiaryContainer,
    onTertiaryContainer = DeepTealOnTertiaryContainer,
    background = AppBackgroundLight,
    onBackground = AppOnSurfaceLight,
    surface = AppSurfaceLight,
    onSurface = AppOnSurfaceLight,
    surfaceVariant = AppSurfaceVariantLight,
    onSurfaceVariant = AppOnSurfaceVariantLight,
    outline = AppOutlineLight,
    outlineVariant = AppOutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

/**
 * Material 3 Dark Color Scheme for MyanmarConnect
 */
private val DarkColorScheme = darkColorScheme(
    primary = PeacockPrimaryDark,
    onPrimary = PeacockOnPrimaryDark,
    primaryContainer = PeacockPrimaryContainerDark,
    onPrimaryContainer = PeacockOnPrimaryContainerDark,
    secondary = AmberSecondaryDark,
    onSecondary = AmberOnSecondaryDark,
    secondaryContainer = AmberSecondaryContainerDark,
    onSecondaryContainer = AmberOnSecondaryContainerDark,
    tertiary = DeepTealTertiaryDark,
    onTertiary = DeepTealOnTertiaryDark,
    tertiaryContainer = DeepTealTertiaryContainerDark,
    onTertiaryContainer = DeepTealOnTertiaryContainerDark,
    background = AppBackgroundDark,
    onBackground = AppOnSurfaceDark,
    surface = AppSurfaceDark,
    onSurface = AppOnSurfaceDark,
    surfaceVariant = AppSurfaceVariantDark,
    onSurfaceVariant = AppOnSurfaceVariantDark,
    outline = AppOutlineDark,
    outlineVariant = AppOutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

/**
 * MyanmarConnect Central Material Design 3 Theme.
 * Uses Peacock Teal and Golden Amber palette paired with Noto Sans Myanmar typography.
 */
@Composable
fun MyanmarConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand-distinctive Peacock Teal & Amber active by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MyanmarTypography,
        content = content
    )
}

// Backwards-compatible alias for template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = MyanmarConnectTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
