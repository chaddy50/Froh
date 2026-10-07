package com.chaddy50.froh

import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.ArtistRepository
import com.chaddy50.froh.data.repository.ComposerRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PerformanceRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.data.scanner.LibraryScanViewModel
import com.chaddy50.froh.navigation.NavigationHost
import com.chaddy50.froh.ui.composables.nowPlayingBar.PlaybackViewModel
import com.chaddy50.froh.ui.screens.playlistsScreen.PlaylistViewModel
import com.chaddy50.froh.ui.theme.MusicAppTheme
import com.chaddy50.froh.utilities.audioLibraryPermission
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MusicApp : ComponentActivity() {
    private val playbackViewModel: PlaybackViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val libraryScanViewModel: LibraryScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                audioLibraryPermission()
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            triggerLibraryScanIfNeeded()
        } else {
            permissionRequestLauncher.launch(audioLibraryPermission())
        }

        enableEdgeToEdge()
        setContent {
            MusicAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationHost(playbackViewModel, playlistViewModel, libraryScanViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                audioLibraryPermission()
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            triggerLibraryScanIfNeeded()
        }
    }

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission ->
            if (hasPermission) {
                triggerLibraryScanIfNeeded()
            }
        }

    private fun triggerLibraryScanIfNeeded() {
        lifecycleScope.launch {
            libraryScanViewModel.refreshLibrary()
        }
    }
}

interface MusicRepositoryProvider {
    val trackRepository: TrackRepository
    val albumRepository: AlbumRepository
    val genreRepository: GenreRepository
    val albumArtistRepository: AlbumArtistRepository
    val performanceRepository: PerformanceRepository
    val playlistRepository: PlaylistRepository
    val filesDirPath: String
}

@HiltAndroidApp
class MusicApplication: Application(), MusicRepositoryProvider {
    override val filesDirPath: String get() = filesDir.absolutePath
    @Inject override lateinit var trackRepository: TrackRepository
    @Inject override lateinit var albumRepository: AlbumRepository
    @Inject lateinit var artistRepository: ArtistRepository
    @Inject override lateinit var genreRepository: GenreRepository
    @Inject override lateinit var albumArtistRepository: AlbumArtistRepository
    @Inject override lateinit var performanceRepository: PerformanceRepository
    @Inject override lateinit var playlistRepository: PlaylistRepository
    @Inject lateinit var composerRepository: ComposerRepository
}
