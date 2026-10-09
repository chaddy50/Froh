package com.chaddy50.froh.ui.composables.expanded

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chaddy50.froh.ui.composables.common.MusicScannerProgressBar

@Composable
fun ContentPane(
    isScanInProgress: Boolean,
    scanProgress: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(modifier = modifier) {
        MusicScannerProgressBar(isScanInProgress, scanProgress)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            content = content,
        )
    }
}
