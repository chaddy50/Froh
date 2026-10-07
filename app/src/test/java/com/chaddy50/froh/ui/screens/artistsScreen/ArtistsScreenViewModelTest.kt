package com.chaddy50.froh.ui.screens.artistsScreen

import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.entity.Track
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.navigation.ArtistsRoute
import kotlinx.coroutines.Dispatchers
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeAudioDbRepository
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.MainDispatcherRule
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ArtistsScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val albumArtistsFlow = MutableStateFlow<List<AlbumArtist>>(emptyList())
    private val genresFlow = MutableStateFlow<List<Genre>>(emptyList())
    private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())
    private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())

    private fun createViewModel(
        genreId: Long = 1L,
        title: String = "Test",
        classicalGenreId: Long? = null,
        playlistDao: FakePlaylistDao = FakePlaylistDao(),
    ): ArtistsScreenViewModel {
        val genreDao = FakeGenreDao(allGenres = genresFlow)
        val albumArtistDao = FakeAlbumArtistDao(albumArtistsFlow)
        val albumDao = FakeAlbumDao(albumsFlow, tracksFlow)

        val config = ClassicalGenreConfig().apply { this.classicalGenreId = classicalGenreId }
        return ArtistsScreenViewModel(
            ArtistsRoute(genreId = genreId, title = title),
            config,
            AlbumArtistRepository(albumArtistDao, FakeAudioDbRepository()),
            AlbumRepository(albumDao),
            GenreRepository(genreDao),
            PlaylistRepository(playlistDao),
        )
    }

    @Test
    fun initialStateIsLoading() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun emitsArtistsFromRepository() = runTest {
        val vm = createViewModel(genreId = 5L, title = "Rock")
        backgroundScope.launch { vm.uiState.collect() }

        genresFlow.value = listOf(Genre(id = 5L, name = "Rock"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Led Zeppelin", sortName = "Led Zeppelin"),
            AlbumArtist(id = 2, name = "Pink Floyd", sortName = "Pink Floyd"),
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.artists.size)
        assertEquals("Rock", state.screenTitle)
        assertEquals("Led Zeppelin", state.artists[0].artist.name)
        assertEquals("Pink Floyd", state.artists[1].artist.name)
    }

    @Test
    fun screenTitleUsesRouteTitle() = runTest {
        val vm = createViewModel(genreId = 99L, title = "My Genre")
        backgroundScope.launch { vm.uiState.collect() }
        albumArtistsFlow.value = emptyList()
        advanceUntilIdle()

        assertEquals("My Genre", vm.uiState.value.screenTitle)
    }

    @Test
    fun entityHeaderShowsGenreNameAndArtistCount() = runTest {
        val vm = createViewModel(genreId = 5L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        genresFlow.value = listOf(Genre(id = 5L, name = "Rock"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Led Zeppelin", sortName = "Led Zeppelin"),
            AlbumArtist(id = 2, name = "Pink Floyd", sortName = "Pink Floyd"),
        )
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("Rock", header.title)
        assertEquals("2 artists", header.subtitle)
    }

    @Test
    fun entityHeaderShowsComposersLabelForClassicalGenre() = runTest {
        val vm = createViewModel(genreId = 10L, classicalGenreId = 10L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        genresFlow.value = listOf(Genre(id = 10L, name = "Classical"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("Classical", header.title)
        assertEquals("1 composers", header.subtitle)
    }

    @Test
    fun entityHeaderFallsBackWhenGenreNotFound() = runTest {
        val vm = createViewModel(genreId = 99L)
        backgroundScope.launch { vm.entityHeaderState.collect() }
        advanceUntilIdle()

        assertEquals("Genre", vm.entityHeaderState.value.title)
    }

    @Test
    fun entityHeaderIncludesPlaylistMembershipData() = runTest {
        val playlistIdsFlow = MutableStateFlow(listOf(1L, 3L))
        val playlistDao = FakePlaylistDao(playlistIdsContainingGenreFlow = playlistIdsFlow)
        val vm = createViewModel(genreId = 5L, playlistDao = playlistDao)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        genresFlow.value = listOf(Genre(id = 5L, name = "Rock"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Led Zeppelin", sortName = "Led Zeppelin"),
        )
        advanceUntilIdle()

        assertEquals(setOf(1L, 3L), vm.entityHeaderState.value.playlistsThatEntityIsAlreadyIn)
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
    fun artistCardShowsAlbumCountSubtitle() = runTest {
        val vm = createViewModel(genreId = 5L)
        backgroundScope.launch { vm.uiState.collect() }

        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Led Zeppelin", sortName = "Led Zeppelin"),
            AlbumArtist(id = 2, name = "Pink Floyd", sortName = "Pink Floyd"),
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "Album 1", catalogueSortIndex = null, artistId = 1, year = "1970"),
            Album(id = 2, title = "Album 2", catalogueSortIndex = null, artistId = 1, year = "1971"),
            Album(id = 3, title = "Album 3", catalogueSortIndex = null, artistId = 1, year = "1972"),
            Album(id = 4, title = "Album 4", catalogueSortIndex = null, artistId = 2, year = "1973"),
        )
        tracksFlow.value = listOf(
            makeTrack(id = 1, albumId = 1, albumArtistId = 1, genreId = 5),
            makeTrack(id = 2, albumId = 2, albumArtistId = 1, genreId = 5),
            makeTrack(id = 3, albumId = 3, albumArtistId = 1, genreId = 5),
            makeTrack(id = 4, albumId = 4, albumArtistId = 2, genreId = 5),
        )
        advanceUntilIdle()

        assertEquals("3 albums", vm.uiState.value.artists[0].subtitle)
        assertEquals("1 albums", vm.uiState.value.artists[1].subtitle)
    }

    @Test
    fun artistCardShowsWorksLabelForClassicalGenre() = runTest {
        val vm = createViewModel(genreId = 10L, classicalGenreId = 10L)
        backgroundScope.launch { vm.uiState.collect() }

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

        assertEquals("2 works", vm.uiState.value.artists[0].subtitle)
    }

    @Test
    fun artistCardShowsZeroCountWhenNoAlbums() = runTest {
        val vm = createViewModel(genreId = 5L)
        backgroundScope.launch { vm.uiState.collect() }

        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "New Artist", sortName = "New Artist"),
        )
        albumsFlow.value = emptyList()
        tracksFlow.value = emptyList()
        advanceUntilIdle()

        assertEquals("0 albums", vm.uiState.value.artists[0].subtitle)
    }

    @Test
    fun artistCardCountsAlbumsFromSubGenresOfParentGenre() = runTest {
        // Genre 10 is "Classical" (parent), genres 20 and 21 are sub-genres
        val vm = createViewModel(genreId = 10L, classicalGenreId = 10L)
        backgroundScope.launch { vm.uiState.collect() }

        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "Orchestral Suite", catalogueSortIndex = null, artistId = 1, year = "1720"),
            Album(id = 2, title = "Chamber Music", catalogueSortIndex = null, artistId = 1, year = "1721"),
            Album(id = 3, title = "Choral Work", catalogueSortIndex = null, artistId = 1, year = "1722"),
        )
        // Tracks belong to sub-genres (20, 21) with parentGenreId = 10
        tracksFlow.value = listOf(
            makeTrack(id = 1, albumId = 1, albumArtistId = 1, genreId = 20, parentGenreId = 10),
            makeTrack(id = 2, albumId = 2, albumArtistId = 1, genreId = 21, parentGenreId = 10),
            makeTrack(id = 3, albumId = 3, albumArtistId = 1, genreId = 20, parentGenreId = 10),
        )
        advanceUntilIdle()

        assertEquals("3 works", vm.uiState.value.artists[0].subtitle)
    }

    @Test
    fun artistCardDoesNotCountAlbumsFromOtherGenres() = runTest {
        val vm = createViewModel(genreId = 5L)
        backgroundScope.launch { vm.uiState.collect() }

        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Artist", sortName = "Artist"),
        )
        // Artist has 3 albums but only 2 have tracks in genre 5
        albumsFlow.value = listOf(
            Album(id = 1, title = "Album 1", catalogueSortIndex = null, artistId = 1, year = "2000"),
            Album(id = 2, title = "Album 2", catalogueSortIndex = null, artistId = 1, year = "2001"),
            Album(id = 3, title = "Album 3", catalogueSortIndex = null, artistId = 1, year = "2002"),
        )
        tracksFlow.value = listOf(
            makeTrack(id = 1, albumId = 1, albumArtistId = 1, genreId = 5),
            makeTrack(id = 2, albumId = 2, albumArtistId = 1, genreId = 5),
            makeTrack(id = 3, albumId = 3, albumArtistId = 1, genreId = 99), // different genre
        )
        advanceUntilIdle()

        assertEquals("2 albums", vm.uiState.value.artists[0].subtitle)
    }
}
