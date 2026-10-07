package com.chaddy50.froh.ui.screens.genresScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.navigation.AppNavigator
import com.chaddy50.froh.navigation.ArtistsRoute
import com.chaddy50.froh.ui.composables.AddToPlaylistHandler
import com.chaddy50.froh.ui.composables.EmptyStateContent
import com.chaddy50.froh.ui.composables.EntityCard
import com.chaddy50.froh.ui.composables.EntityScreen
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.composables.rememberAddToPlaylistState
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import com.chaddy50.froh.utilities.audioLibraryPermission

@Composable
fun GenresScreen(
    playbackViewModel: PlaybackViewModel,
    playlistViewModel: PlaylistViewModel,
    appNavigator: AppNavigator,
    screenViewModel: GenresScreenViewModel = hiltViewModel(),
) {
    val uiState by screenViewModel.uiState.collectAsStateWithLifecycle()
    val allPlaylists by playlistViewModel.allPlaylists.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, audioLibraryPermission()
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isPermissionGranted = ContextCompat.checkSelfPermission(
                    context, audioLibraryPermission()
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isPermissionGranted = granted
    }

    val addToPlaylistState = rememberAddToPlaylistState<Genre>(
        getPlaylistMembership = { genre -> playlistViewModel.getPlaylistsThatGenreIsAlreadyIn(genre.id) },
        onAdd = { playlistId, genre -> playlistViewModel.addGenreToPlaylist(playlistId, genre.id) },
        onCreateAndAdd = { name, genre -> playlistViewModel.createPlaylistAndAddGenre(name, genre.id) },
    )

    EntityScreen(
        uiState.isLoading,
        {
            if (!isPermissionGranted && uiState.genres.isEmpty()) {
                val activity = context as Activity
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, audioLibraryPermission()
                )
                EmptyStateContent(
                    icon = Icons.Filled.FolderOff,
                    title = "Permission required",
                    subtitle = "This app needs access to your audio files to display your music library",
                    action = {
                        if (shouldShowRationale) {
                            Button(onClick = {
                                permissionLauncher.launch(audioLibraryPermission())
                            }) {
                                Text("Grant permission")
                            }
                        } else {
                            Button(onClick = { openAppSettings(context) }) {
                                Text("Open settings")
                            }
                        }
                    },
                )
            } else if (uiState.genres.isEmpty()) {
                EmptyStateContent(
                    icon = Icons.Filled.LibraryMusic,
                    title = "No music yet",
                    subtitle = "Add music to your device to get started",
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.genres) { genreWithStats ->
                        EntityCard(
                            genreWithStats.genre.name,
                            onClick = {
                                appNavigator.push(
                                    ArtistsRoute(
                                        genreId = genreWithStats.genre.id,
                                        title = genreWithStats.genre.name,
                                    )
                                )
                            },
                            onLongClick = { addToPlaylistState.show(genreWithStats.genre) },
                            icon = genreIcon(genreWithStats.genre.name),
                            subtitle = genreWithStats.subtitle,
                        )
                    }
                }
            }
        },
        onPlay = if (uiState.genres.isNotEmpty()) {{ playbackViewModel.playAllTracks(false) }} else null,
        onShuffle = if (uiState.genres.isNotEmpty()) {{ playbackViewModel.playAllTracks(true) }} else null,
    )

    AddToPlaylistHandler(state = addToPlaylistState, allPlaylists = allPlaylists)
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
