package com.chaddy50.froh.navigation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.window.core.layout.WindowSizeClass

enum class WindowWidthSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

val LocalWindowWidthSizeClass: ProvidableCompositionLocal<WindowWidthSizeClass> =
    compositionLocalOf { WindowWidthSizeClass.COMPACT }

@Composable
fun rememberWindowWidthSizeClass(): WindowWidthSizeClass {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
            WindowWidthSizeClass.EXPANDED
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
            WindowWidthSizeClass.MEDIUM
        else -> WindowWidthSizeClass.COMPACT
    }
}
