package com.chaddy50.froh.ui.screens.settingsScreen.genreMappings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.navigation.LocalWindowWidthSizeClass
import com.chaddy50.froh.navigation.WindowWidthSizeClass
import com.chaddy50.froh.navigation.TopBarContent
import kotlinx.coroutines.launch

@Composable
fun ClassicalGenreSettingsScreen(
    onNavigateBack: () -> Unit,
    onRebuildLibrary: () -> Unit,
    onTopBarContentChanged: (TopBarContent) -> Unit = {},
    viewModel: ClassicalGenreSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onTopBarContentChanged(TopBarContent(title = "Classical Genres"))
    }

    BackHandler {
        scope.launch {
            val didChange = viewModel.saveSelections()
            if (didChange) {
                onRebuildLibrary()
            }
            onNavigateBack()
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = if (LocalWindowWidthSizeClass.current == WindowWidthSizeClass.EXPANDED) PaddingValues(bottom = 120.dp) else PaddingValues(),
    ) {
        items(uiState.genres, key = { it.genreId }) { genre ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleGenreSelection(genre.genreName) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = genre.genreName,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (genre.isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
