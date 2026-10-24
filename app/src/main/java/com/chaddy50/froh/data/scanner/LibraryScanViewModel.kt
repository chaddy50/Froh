package com.chaddy50.froh.data.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.repository.GenreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryScanViewModel @Inject constructor(
    private val classicalGenreConfig: ClassicalGenreConfig,
    private val musicScanner: MusicScanner,
    private val genreRepository: GenreRepository,
) : ViewModel() {
    private val _isScanInProgress = MutableStateFlow(false)
    val isScanInProgress = _isScanInProgress.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress = _scanProgress.asStateFlow()

    init {
        viewModelScope.launch {
            initializeClassicalGenreId()

            musicScanner.scanProgress.collect {
                _scanProgress.value = it
            }
        }
    }

    fun rebuildLibrary() {
        viewModelScope.launch(Dispatchers.IO) {
            musicScanner.resetScanTimestamp()
            refreshLibrary()
        }
    }

    suspend fun refreshLibrary() {
        _isScanInProgress.value = true
        musicScanner.scan()
        initializeClassicalGenreId()
        _isScanInProgress.value = false

        viewModelScope.launch(Dispatchers.IO) {
            musicScanner.fetchArtistArtwork()
        }
    }

    private suspend fun initializeClassicalGenreId() {
        classicalGenreConfig.classicalGenreId =
            genreRepository.getGenreByName("Classical")?.id
    }
}
