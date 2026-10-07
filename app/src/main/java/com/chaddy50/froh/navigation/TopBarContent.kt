package com.chaddy50.froh.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

data class TopBarContent(
    val title: String,
    val actions: @Composable RowScope.() -> Unit = {},
)
