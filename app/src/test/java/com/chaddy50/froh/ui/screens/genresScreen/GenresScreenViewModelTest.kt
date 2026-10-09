package com.chaddy50.froh.ui.screens.genresScreen

import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeDeezerRepository
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class GenresScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val genresFlow = MutableStateFlow<List<Genre>>(emptyList())
    private val albumArtistsFlow = MutableStateFlow<List<AlbumArtist>>(emptyList())
    private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())
    private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())

    private fun createViewModel(classicalGenreId: Long? = null): GenresScreenViewModel {
        val genreDao = FakeGenreDao(genresFlow)
        val albumArtistDao = FakeAlbumArtistDao(albumArtistsFlow)
        val albumDao = FakeAlbumDao(albumsFlow, tracksFlow)
        val config = ClassicalGenreConfig().apply { this.classicalGenreId = classicalGenreId }
        return GenresScreenViewModel(
            GenreRepository(genreDao),
            AlbumArtistRepository(albumArtistDao, FakeDeezerRepository()),
            AlbumRepository(albumDao),
            config,
        )
    }

    private fun makeTrack(
        id: Long,
        albumId: Long,
        albumArtistId: Long,
        genreId: Long,
        parentGenreId: Long? = null,
    ) = Track(
        id = id,
        uri = "uri/$id",
        title = "Track $id",
        number = 1,
        albumId = albumId,
        albumName = "Album $albumId",
        artistId = albumArtistId,
        artistName = "Artist $albumArtistId",
        albumArtistId = albumArtistId,
        albumArtistName = "Artist $albumArtistId",
        genreId = genreId,
        genreName = "Genre $genreId",
        parentGenreId = parentGenreId,
        parentGenreName = if (parentGenreId != null) "Genre $parentGenreId" else null,
        duration = 3.minutes,
        discNumber = 1,
        year = "2000",
    )

    @Test
    fun initialStateIsLoading() {
        val vm = createViewModel()
        val state = vm.uiState.value
        assertTrue(state.isLoading)
        assertEquals(emptyList<GenreWithStats>(), state.genres)
    }

    @Test
    fun emitsGenresFromRepository() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        val genres = listOf(
            Genre(id = 1, name = "Classical"),
            Genre(id = 2, name = "Rock"),
        )
        genresFlow.value = genres
        advanceUntilIdle()
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.genres.size)
        assertEquals("Classical", state.genres[0].genre.name)
        assertEquals("Rock", state.genres[1].genre.name)
    }

    @Test
    fun screenTitleIsGenres() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        genresFlow.value = emptyList()
        advanceUntilIdle()
        assertEquals("Genres", vm.uiState.value.screenTitle)
    }

    @Test
    fun updatesWhenRepositoryEmitsNewList() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }
        genresFlow.value = listOf(Genre(id = 1, name = "Rock"))
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.genres.size)

        genresFlow.value = listOf(
            Genre(id = 1, name = "Rock"),
            Genre(id = 2, name = "Jazz"),
        )
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.genres.size)
    }

    @Test
    fun genreCardShowsArtistAndAlbumCounts() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }

        genresFlow.value = listOf(Genre(id = 5, name = "Rock"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Led Zeppelin", sortName = "Led Zeppelin"),
            AlbumArtist(id = 2, name = "Pink Floyd", sortName = "Pink Floyd"),
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "Album 1", catalogueSortIndex = null, artistId = 1, year = "1970"),
            Album(id = 2, title = "Album 2", catalogueSortIndex = null, artistId = 1, year = "1971"),
            Album(id = 3, title = "Album 3", catalogueSortIndex = null, artistId = 2, year = "1973"),
        )
        tracksFlow.value = listOf(
            makeTrack(id = 1, albumId = 1, albumArtistId = 1, genreId = 5),
            makeTrack(id = 2, albumId = 2, albumArtistId = 1, genreId = 5),
            makeTrack(id = 3, albumId = 3, albumArtistId = 2, genreId = 5),
        )
        advanceUntilIdle()

        assertEquals("2 artists \u00B7 3 albums", vm.uiState.value.genres[0].subtitle)
    }

    @Test
    fun genreCardShowsZeroCounts() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }

        genresFlow.value = listOf(Genre(id = 5, name = "Rock"))
        albumArtistsFlow.value = emptyList()
        albumsFlow.value = emptyList()
        tracksFlow.value = emptyList()
        advanceUntilIdle()

        assertEquals("0 artists \u00B7 0 albums", vm.uiState.value.genres[0].subtitle)
    }

    @Test
    fun genreCardCountsIncludeSubGenres() = runTest {
        val vm = createViewModel()
        backgroundScope.launch { vm.uiState.collect() }

        // Genre 10 is "Classical" (parent), sub-genre artists have genreId = 10
        genresFlow.value = listOf(Genre(id = 10, name = "Classical"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "Work 1", catalogueSortIndex = null, artistId = 1, year = "1720"),
            Album(id = 2, title = "Work 2", catalogueSortIndex = null, artistId = 1, year = "1721"),
        )
        // Tracks belong to sub-genres with parentGenreId = 10
        tracksFlow.value = listOf(
            makeTrack(id = 1, albumId = 1, albumArtistId = 1, genreId = 20, parentGenreId = 10),
            makeTrack(id = 2, albumId = 2, albumArtistId = 1, genreId = 21, parentGenreId = 10),
        )
        advanceUntilIdle()

        assertEquals("1 artists \u00B7 2 albums", vm.uiState.value.genres[0].subtitle)
    }

    @Test
    fun classicalGenreUsesComposersAndWorksLabels() = runTest {
        val vm = createViewModel(classicalGenreId = 10L)
        backgroundScope.launch { vm.uiState.collect() }

        genresFlow.value = listOf(Genre(id = 10, name = "Classical"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "Work 1", catalogueSortIndex = null, artistId = 1, year = "1720"),
            Album(id = 2, title = "Work 2", catalogueSortIndex = null, artistId = 1, year = "1721"),
        )
        tracksFlow.value = listOf(
            makeTrack(id = 1, albumId = 1, albumArtistId = 1, genreId = 20, parentGenreId = 10),
            makeTrack(id = 2, albumId = 2, albumArtistId = 1, genreId = 21, parentGenreId = 10),
        )
        advanceUntilIdle()

        assertEquals("1 composers \u00B7 2 works", vm.uiState.value.genres[0].subtitle)
    }
}
