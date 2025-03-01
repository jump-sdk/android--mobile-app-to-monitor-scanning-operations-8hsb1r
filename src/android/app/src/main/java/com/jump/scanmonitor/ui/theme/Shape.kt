package com.jump.scanmonitor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.dp

/**
 * Defines the shape theming for the ScanMonitorApps application.
 * These shapes are used for consistent styling across different UI components,
 * following Material Design guidelines to maintain a cohesive visual appearance.
 * 
 * - small: Used for smaller components like buttons, chips, text fields
 * - medium: Used for medium-sized components like cards and dialogs
 * - large: Used for larger surface components like bottom sheets
 */
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)