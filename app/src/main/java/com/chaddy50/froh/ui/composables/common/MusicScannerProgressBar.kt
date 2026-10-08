package com.chaddy50.froh.ui.composables.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MusicScannerProgressBar(isScanInProgress: Boolean, scanProgress: Float) {
    AnimatedVisibility(
        visible = isScanInProgress,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(durationMillis = 300))
    ) {
        Column {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Scanning music...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )

            LinearProgressIndicator(
                progress = { scanProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }
}
