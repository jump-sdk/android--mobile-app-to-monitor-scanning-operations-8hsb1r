package com.jump.scanmonitor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Content

// Import color definitions
import com.jump.scanmonitor.ui.theme.Primary
import com.jump.scanmonitor.ui.theme.PrimaryVariant
import com.jump.scanmonitor.ui.theme.Secondary
import com.jump.scanmonitor.ui.theme.SecondaryVariant
import com.jump.scanmonitor.ui.theme.Background
import com.jump.scanmonitor.ui.theme.Surface
import com.jump.scanmonitor.ui.theme.Error
import com.jump.scanmonitor.ui.theme.OnPrimary
import com.jump.scanmonitor.ui.theme.OnSecondary
import com.jump.scanmonitor.ui.theme.OnBackground
import com.jump.scanmonitor.ui.theme.OnSurface
import com.jump.scanmonitor.ui.theme.OnError

// Import typography and shapes
import com.jump.scanmonitor.ui.theme.Typography
import com.jump.scanmonitor.ui.theme.Shapes

/**
 * Defines the Material Design theme for the ScanMonitorApps application.
 * 
 * This theme implements high contrast and clear typography to ensure readability
 * in various stadium environments, including variable lighting conditions.
 * Both light and dark theme variants are provided, with the system default
 * being respected unless explicitly overridden.
 * 
 * The colors used in this theme meet WCAG AA standards for accessibility and
 * are designed to be clearly visible in various lighting conditions that may be
 * encountered in stadium environments.
 */

private val DarkColorPalette = darkColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    secondary = Secondary,
    secondaryVariant = SecondaryVariant,
    background = Background,
    surface = Surface,
    error = Error,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onError = OnError
)

private val LightColorPalette = lightColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    secondary = Secondary,
    secondaryVariant = SecondaryVariant,
    background = Background,
    surface = Surface,
    error = Error,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onError = OnError
)

/**
 * Composable function that applies the ScanMonitor theme to its content.
 * Provides light or dark theme variants based on system settings or explicit preference.
 *
 * @param darkTheme Whether to use the dark theme. If not provided, follows system setting.
 * @param content The content to be styled with this theme.
 */
@Composable
fun ScanMonitorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}