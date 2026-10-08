package com.chaddy50.froh.ui.composables.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass

@Composable
fun EntityScreen(
    isLoading: Boolean,
    content: @Composable () -> Unit,
    onPlay: (() -> Unit)? = null,
    onShuffle: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            content()
        }

        if (onPlay != null && onShuffle != null && LocalWindowWidthSizeClass.current != WindowWidthSizeClass.EXPANDED) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(onClick = onPlay) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                }
                FloatingActionButton(onClick = onShuffle) {
                    Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle")
                }
            }
        }
    }
}
