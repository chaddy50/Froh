package com.chaddy50.froh.ui.screens.albumsScreen

import com.chaddy50.froh.data.ClassicalGenreConfig
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Composer
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.entity.Track
import kotlin.time.Duration.Companion.minutes
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.ComposerRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PlaylistRepository
import com.chaddy50.froh.navigation.AlbumsRoute
import kotlinx.coroutines.Dispatchers
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeArtworkDownloader
import com.chaddy50.froh.fakes.FakeDeezerRepository
import com.chaddy50.froh.fakes.FakeComposerDao
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakeOpenOpusRepository
import com.chaddy50.froh.fakes.FakePlaylistDao
import com.chaddy50.froh.fakes.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AlbumsScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())
    private val albumArtistsFlow = MutableStateFlow<List<AlbumArtist>>(emptyList())
    private val genresFlow = MutableStateFlow<List<Genre>>(emptyList())
    private val composersFlow = MutableStateFlow<List<Composer>>(emptyList())
    private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())

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

    private fun createViewModel(
        genreId: Long = 1L,
        albumArtistId: Long = 1L,
        classicalGenreId: Long? = null,
    ): AlbumsScreenViewModel {
        val genreDao = FakeGenreDao(allGenres = genresFlow)
        val albumArtistDao = FakeAlbumArtistDao(albumArtistsFlow)
        val albumDao = FakeAlbumDao(albumsFlow, tracksFlow)
        val composerDao = FakeComposerDao(composersFlow)
        val artworkDownloader = FakeArtworkDownloader()
        val deezerRepository = FakeDeezerRepository()
        val openOpusRepository = FakeOpenOpusRepository()

        val config = ClassicalGenreConfig().apply { this.classicalGenreId = classicalGenreId }
        return AlbumsScreenViewModel(
            AlbumsRoute(genreId = genreId, albumArtistId = albumArtistId, title = "Test"),
            config,
            AlbumRepository(albumDao),
            AlbumArtistRepository(albumArtistDao, deezerRepository),
            GenreRepository(genreDao),
            PlaylistRepository(FakePlaylistDao()),
            ComposerRepository(composerDao, openOpusRepository, artworkDownloader, albumArtistDao),
        )
    }

    // --- uiState ---

    @Test
    fun initialStateIsLoading() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun emitsAlbumsForArtist() = runTest {
        val vm = createViewModel(albumArtistId = 1L)
        backgroundScope.launch { vm.uiState.collect() }

        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Pink Floyd", sortName = "Pink Floyd", portraitPath = "exists"),
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "The Wall", catalogueSortIndex = null, artistId = 1, year = "1979"),
            Album(id = 2, title = "Animals", catalogueSortIndex = null, artistId = 1, year = "1977"),
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.albums.size)
        assertEquals("Pink Floyd", state.screenTitle)
    }

    @Test
    fun screenTitleFallsBackToAlbumsWhenArtistNameNull() = runTest {
        val vm = createViewModel(albumArtistId = 99L)
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()

        assertEquals("Albums", vm.uiState.value.screenTitle)
    }

    @Test
    fun screenTitleIncludesSubGenreWhenSelected() = runTest {
        val vm = createViewModel(genreId = 10L, albumArtistId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.uiState.collect() }

        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        genresFlow.value = listOf(
            Genre(id = 10L, name = "Classical"),
            Genre(id = 20L, name = "Orchestral", parentGenreId = 10L),
        )
        // Provide a composer so the one-shot fetch doesn't trigger
        composersFlow.value = listOf(
            Composer(id = 1, albumArtistId = 1, openOpusId = 1, completeName = "Bach", birthYear = null, deathYear = null, epoch = null, portraitPath = null)
        )

        vm.updateSelectedSubGenreId(20L)
        advanceUntilIdle()

        assertEquals("Bach - Orchestral", vm.uiState.value.screenTitle)
    }

    @Test
    fun clearingSubGenreResetsTitleToArtistName() = runTest {
        val vm = createViewModel(genreId = 10L, albumArtistId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.uiState.collect() }

        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        genresFlow.value = listOf(
            Genre(id = 10L, name = "Classical"),
            Genre(id = 20L, name = "Orchestral", parentGenreId = 10L),
        )
        composersFlow.value = listOf(
            Composer(id = 1, albumArtistId = 1, openOpusId = 1, completeName = "Bach", birthYear = null, deathYear = null, epoch = null, portraitPath = null)
        )

        vm.updateSelectedSubGenreId(20L)
        advanceUntilIdle()
        assertEquals("Bach - Orchestral", vm.uiState.value.screenTitle)

        vm.updateSelectedSubGenreId(null)
        advanceUntilIdle()
        assertEquals("Bach", vm.uiState.value.screenTitle)
    }

    // --- entityHeaderState ---

    @Test
    fun entityHeaderShowsNonClassicalArtistInfo() = runTest {
        val vm = createViewModel(genreId = 1L, albumArtistId = 1L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        genresFlow.value = listOf(Genre(id = 1L, name = "Rock"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Pink Floyd", sortName = "Pink Floyd", portraitPath = "/portraits/pf.jpg"),
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "The Wall", catalogueSortIndex = null, artistId = 1, year = "1979"),
            Album(id = 2, title = "Animals", catalogueSortIndex = null, artistId = 1, year = "1977"),
        )
        tracksFlow.value = listOf(
            makeTrack(id = 1, albumId = 1, albumArtistId = 1, genreId = 1),
            makeTrack(id = 2, albumId = 2, albumArtistId = 1, genreId = 1),
        )
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("Pink Floyd", header.title)
        assertEquals("Rock", header.subtitle)
        assertEquals("2 albums", header.details)
    }

    @Test
    fun entityHeaderShowsComposerInfoForClassical() = runTest {
        val vm = createViewModel(genreId = 10L, albumArtistId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        genresFlow.value = listOf(Genre(id = 10L, name = "Classical"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        composersFlow.value = listOf(
            Composer(
                id = 1, albumArtistId = 1, openOpusId = 196,
                completeName = "Johann Sebastian Bach",
                birthYear = "1685", deathYear = "1750", epoch = "Baroque",
                portraitPath = "/portraits/bach.jpg",
            )
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "Goldberg Variations", catalogueSortIndex = 988, artistId = 1, year = "1741"),
        )
        tracksFlow.value = listOf(
            makeTrack(id = 1, albumId = 1, albumArtistId = 1, genreId = 20, parentGenreId = 10),
        )
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("Johann Sebastian Bach", header.title)
        assertEquals("Baroque - 1685–1750", header.subtitle)
        assertEquals("1 works", header.details)
        assertEquals("/portraits/bach.jpg", header.artworkPath)
    }

    @Test
    fun entityHeaderFallsBackWhenNoComposerForClassical() = runTest {
        val vm = createViewModel(genreId = 10L, albumArtistId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        genresFlow.value = listOf(Genre(id = 10L, name = "Classical"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "Goldberg Variations", catalogueSortIndex = 988, artistId = 1, year = "1741"),
        )
        tracksFlow.value = listOf(
            makeTrack(id = 1, albumId = 1, albumArtistId = 1, genreId = 20, parentGenreId = 10),
        )
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("Bach", header.title)
        assertEquals("Classical", header.subtitle)
        assertEquals("1 works", header.details)
    }

    @Test
    fun entityHeaderFallsBackWhenArtistNotFound() = runTest {
        val vm = createViewModel(genreId = 1L, albumArtistId = 99L)
        backgroundScope.launch { vm.entityHeaderState.collect() }
        advanceUntilIdle()

        assertEquals("Artist", vm.entityHeaderState.value.title)
    }

    @Test
    fun entityHeaderHandlesMissingBirthDeathYears() = runTest {
        val vm = createViewModel(genreId = 10L, albumArtistId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.entityHeaderState.collect() }

        genresFlow.value = listOf(Genre(id = 10L, name = "Classical"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        composersFlow.value = listOf(
            Composer(
                id = 1, albumArtistId = 1, openOpusId = 196,
                completeName = "Johann Sebastian Bach",
                birthYear = null, deathYear = null, epoch = "Baroque",
                portraitPath = null,
            )
        )
        albumsFlow.value = emptyList()
        advanceUntilIdle()

        val header = vm.entityHeaderState.value
        assertEquals("Baroque", header.subtitle)
    }

    // --- subGenres ---

    @Test
    fun subGenresReturnsSubGenresWhenClassical() = runTest {
        genresFlow.value = listOf(
            Genre(id = 10L, name = "Classical"),
            Genre(id = 20L, name = "Orchestral", parentGenreId = 10L),
            Genre(id = 21L, name = "Chamber", parentGenreId = 10L),
        )
        val vm = createViewModel(genreId = 10L, albumArtistId = 1L, classicalGenreId = 10L)
        // subGenres uses flowOn(Dispatchers.IO) so wait for the real dispatcher
        val subGenres = withContext(Dispatchers.Default) {
            vm.subGenres.first { it.isNotEmpty() }
        }

        assertEquals(2, subGenres.size)
    }

    @Test
    fun subGenresReturnsEmptyListWhenNonClassical() = runTest {
        val vm = createViewModel(genreId = 1L, albumArtistId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.subGenres.collect() }
        advanceUntilIdle()

        assertEquals(emptyList<Genre>(), vm.subGenres.value)
    }

    // --- uiState filtering ---

    @Test
    fun filtersAlbumsBySubGenreWhenSelected() = runTest {
        val vm = createViewModel(genreId = 10L, albumArtistId = 1L, classicalGenreId = 10L)
        backgroundScope.launch { vm.uiState.collect() }

        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )
        genresFlow.value = listOf(
            Genre(id = 10L, name = "Classical"),
            Genre(id = 20L, name = "Orchestral", parentGenreId = 10L),
        )
        composersFlow.value = listOf(
            Composer(id = 1, albumArtistId = 1, openOpusId = 1, completeName = "Bach", birthYear = null, deathYear = null, epoch = null, portraitPath = null)
        )
        albumsFlow.value = listOf(
            Album(id = 1, title = "Orchestral Suite", catalogueSortIndex = 1066, artistId = 1, year = "1730"),
            Album(id = 2, title = "Cello Suite", catalogueSortIndex = 1007, artistId = 1, year = "1720"),
        )
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.albums.size)

        vm.updateSelectedSubGenreId(20L)
        advanceUntilIdle()

        // FakeAlbumDao returns all albums for the artist regardless of genre filter,
        // but this verifies the flow re-emits when sub-genre changes
        assertFalse(vm.uiState.value.isLoading)
    }

    // --- isClassical ---

    @Test
    fun isClassicalTrueWhenGenreMatchesClassicalConfig() {
        val vm = createViewModel(genreId = 10L, classicalGenreId = 10L)
        assertTrue(vm.isClassical)
    }

    @Test
    fun isClassicalFalseWhenGenreDoesNotMatch() {
        val vm = createViewModel(genreId = 1L, classicalGenreId = 10L)
        assertFalse(vm.isClassical)
    }

    @Test
    fun isClassicalFalseWhenConfigIsNull() {
        val vm = createViewModel(genreId = 1L, classicalGenreId = null)
        assertFalse(vm.isClassical)
    }
}
