package com.jump.scanmonitor.ui.theme

import androidx.compose.ui.graphics.Color // androidx.compose.ui.graphics:1.5.0

/**
 * Color definitions for the ScanMonitorApps application
 * 
 * These colors are designed following Material Design principles with high contrast
 * to ensure readability in variable lighting conditions at stadiums.
 * All text color combinations meet WCAG AA standards for accessibility.
 */

// Primary colors - Blue
val Primary = Color(0xFF1976D2)
val PrimaryVariant = Color(0xFF1565C0)
val PrimaryBlue = Primary

// Secondary colors - Amber
val Secondary = Color(0xFFFFA000)
val SecondaryVariant = Color(0xFFFF8F00)
val SecondaryAmber = Secondary

// Background and surface colors
val Background = Color(0xFFFFFFFF)
val Surface = Color(0xFFF5F5F5)
val Error = Color(0xFFB00020)

// Colors for content displayed on top of primary colors
val OnPrimary = Color(0xFFFFFFFF) // White text on blue background
val OnSecondary = Color(0xFF000000) // Black text on amber background
val OnBackground = Color(0xFF000000) // Black text on white background
val OnSurface = Color(0xFF212121) // Dark gray text on light gray surface
val OnError = Color(0xFFFFFFFF) // White text on error color

// Text colors
val TextPrimary = Color(0xFF212121) // Dark gray for primary text
val TextSecondary = Color(0xFF757575) // Medium gray for secondary text

// Status indicator background colors
val OfflineBackground = Error.copy(alpha = 0.1f) // Subtle red background for offline state
val StaleDataBackground = Secondary.copy(alpha = 0.1f) // Subtle amber background for stale data