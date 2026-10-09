package com.chaddy50.froh.ui.modalSheets.nowPlayingSheet.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    onDismiss: () -> Unit,
    isShuffleModeEnabled: Boolean,
    onShuffleToggled: () -> Unit,
    showQueueToggle: Boolean = false,
    isQueueHidden: Boolean = false,
    onQueueHiddenToggled: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Close",
                modifier = Modifier.size(32.dp)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            if (isShuffleModeEnabled) {
                FilledTonalIconButton(
                    onClick = onShuffleToggled,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onShuffleToggled
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (showQueueToggle) {
                if (!isQueueHidden) {
                    FilledTonalIconButton(
                        onClick = onQueueHiddenToggled,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onQueueHiddenToggled
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}