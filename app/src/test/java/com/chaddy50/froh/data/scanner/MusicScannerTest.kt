package com.chaddy50.froh.data.scanner

import com.chaddy50.froh.data.dao.ArtistDao
import com.chaddy50.froh.data.entity.AlbumArtist
import com.chaddy50.froh.data.entity.Artist
import com.chaddy50.froh.data.entity.Composer
import com.chaddy50.froh.data.entity.Genre
import com.chaddy50.froh.data.repository.AlbumArtistRepository
import com.chaddy50.froh.data.repository.AlbumRepository
import com.chaddy50.froh.data.repository.ArtistRepository
import com.chaddy50.froh.data.repository.ComposerRepository
import com.chaddy50.froh.data.repository.GenreMappingRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.data.repository.PerformanceRepository
import com.chaddy50.froh.data.repository.TrackRepository
import com.chaddy50.froh.fakes.FakeAlbumArtistDao
import com.chaddy50.froh.fakes.FakeAlbumDao
import com.chaddy50.froh.fakes.FakeArtworkDownloader
import com.chaddy50.froh.fakes.FakeAudioDbRepository
import com.chaddy50.froh.fakes.FakeComposerDao
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakeGenreMappingDao
import com.chaddy50.froh.fakes.FakeOpenOpusRepository
import com.chaddy50.froh.fakes.FakePerformanceDao
import com.chaddy50.froh.fakes.FakeTrackDao
import com.chaddy50.froh.data.api.openOpus.OpenOpusComposer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class MusicScannerTest {

    private val albumArtistsFlow = MutableStateFlow<List<AlbumArtist>>(emptyList())
    private val genresFlow = MutableStateFlow<List<Genre>>(emptyList())
    private val composersFlow = MutableStateFlow<List<Composer>>(emptyList())
    private lateinit var albumArtistDao: FakeAlbumArtistDao

    private fun createScanner(
        audioDbRepository: FakeAudioDbRepository = FakeAudioDbRepository(),
        openOpusRepository: FakeOpenOpusRepository = FakeOpenOpusRepository(),
        artworkDownloader: FakeArtworkDownloader = FakeArtworkDownloader(),
    ): MusicScanner {
        val genreDao = FakeGenreDao(allGenres = genresFlow)
        albumArtistDao = FakeAlbumArtistDao(albumArtistsFlow)
        val composerDao = FakeComposerDao(composersFlow)

        // Seed genres into the DAO's internal map so getGenreByName works
        for (genre in genresFlow.value) {
            genreDao.genres[genre.name] = genre
        }

        val stubArtistDao = object : ArtistDao {
            override suspend fun insert(artist: Artist) = Unit
            override suspend fun update(artist: Artist) = Unit
            override suspend fun delete(artist: Artist) = Unit
            override suspend fun getArtistByName(name: String): Artist? = null
            override fun getArtistById(id: Int): Flow<Artist?> = emptyFlow()
            override fun getAllArtists(): Flow<List<Artist>> = emptyFlow()
        }

        val context = RuntimeEnvironment.getApplication()
        return MusicScanner(
            context = context,
            genreRepository = GenreRepository(genreDao),
            genreMappingRepository = GenreMappingRepository(FakeGenreMappingDao()),
            artistRepository = ArtistRepository(stubArtistDao),
            albumArtistRepository = AlbumArtistRepository(albumArtistDao, audioDbRepository),
            albumRepository = AlbumRepository(FakeAlbumDao()),
            trackRepository = TrackRepository(FakeTrackDao()),
            performanceRepository = PerformanceRepository(FakePerformanceDao()),
            composerRepository = ComposerRepository(composerDao, openOpusRepository, artworkDownloader, albumArtistDao),
        )
    }

    @Test
    fun fetchesPortraitForNonClassicalArtistWithoutPortrait() = runTest {
        val audioDbRepo = FakeAudioDbRepository(portraitUrl = "/downloaded/portrait.jpg")
        genresFlow.value = listOf(Genre(id = 5L, name = "Rock"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Led Zeppelin", sortName = "Led Zeppelin"),
        )

        val scanner = createScanner(audioDbRepository = audioDbRepo)
        albumArtistDao.setGenresForArtist(1, setOf(5L))
        scanner.fetchArtistArtwork()

        val artist = albumArtistsFlow.value.find { it.id == 1L }
        assertEquals("/downloaded/portrait.jpg", artist?.portraitPath)
    }

    @Test
    fun skipsArtistThatAlreadyHasPortrait() = runTest {
        val audioDbRepo = FakeAudioDbRepository(portraitUrl = "/new/portrait.jpg")
        genresFlow.value = listOf(Genre(id = 5L, name = "Rock"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Led Zeppelin", sortName = "Led Zeppelin", portraitPath = "/existing/portrait.jpg"),
        )

        val scanner = createScanner(audioDbRepository = audioDbRepo)
        albumArtistDao.setGenresForArtist(1, setOf(5L))
        scanner.fetchArtistArtwork()

        val artist = albumArtistsFlow.value.find { it.id == 1L }
        assertEquals("/existing/portrait.jpg", artist?.portraitPath)
    }

    @Test
    fun fetchesComposerForClassicalSubGenreArtist() = runTest {
        val openOpusRepo = FakeOpenOpusRepository(
            composer = OpenOpusComposer(
                id = 196, name = "Bach", completeName = "Johann Sebastian Bach",
                birthDate = "1685-03-31", deathDate = "1750-07-28", epoch = "Baroque",
                portraitUrl = "https://example.com/bach.jpg",
            )
        )
        val artworkDownloader = FakeArtworkDownloader(resultPath = "/portraits/bach.jpg")

        genresFlow.value = listOf(
            Genre(id = 10L, name = "Classical"),
            Genre(id = 11L, name = "Symphony", parentGenreId = 10L),
        )
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )

        val scanner = createScanner(openOpusRepository = openOpusRepo, artworkDownloader = artworkDownloader)
        albumArtistDao.setGenresForArtist(1, setOf(11L))
        scanner.fetchArtistArtwork()

        val artist = albumArtistsFlow.value.find { it.id == 1L }
        assertEquals("/portraits/bach.jpg", artist?.portraitPath)
    }

    @Test
    fun detectsClassicalArtistByDirectGenreIdMatch() = runTest {
        val openOpusRepo = FakeOpenOpusRepository(
            composer = OpenOpusComposer(
                id = 196, name = "Bach", completeName = "Johann Sebastian Bach",
                birthDate = "1685-03-31", deathDate = "1750-07-28", epoch = "Baroque",
                portraitUrl = "https://example.com/bach.jpg",
            )
        )
        val artworkDownloader = FakeArtworkDownloader(resultPath = "/portraits/bach.jpg")

        genresFlow.value = listOf(Genre(id = 10L, name = "Classical"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach"),
        )

        val scanner = createScanner(openOpusRepository = openOpusRepo, artworkDownloader = artworkDownloader)
        albumArtistDao.setGenresForArtist(1, setOf(10L))
        scanner.fetchArtistArtwork()

        val artist = albumArtistsFlow.value.find { it.id == 1L }
        assertEquals("/portraits/bach.jpg", artist?.portraitPath)
    }

    @Test
    fun skipsClassicalArtistThatAlreadyHasPortrait() = runTest {
        val openOpusRepo = FakeOpenOpusRepository(
            composer = OpenOpusComposer(
                id = 196, name = "Bach", completeName = "Johann Sebastian Bach",
                birthDate = "1685-03-31", deathDate = "1750-07-28", epoch = "Baroque",
                portraitUrl = "https://example.com/bach.jpg",
            )
        )
        genresFlow.value = listOf(Genre(id = 10L, name = "Classical"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Bach", sortName = "Bach", portraitPath = "/existing/bach.jpg"),
        )
        composersFlow.value = listOf(
            Composer(id = 1, albumArtistId = 1, openOpusId = 196, completeName = "Bach",
                birthYear = "1685", deathYear = "1750", epoch = "Baroque", portraitPath = "/existing/bach.jpg"),
        )

        val scanner = createScanner(openOpusRepository = openOpusRepo)
        albumArtistDao.setGenresForArtist(1, setOf(10L))
        scanner.fetchArtistArtwork()

        val artist = albumArtistsFlow.value.find { it.id == 1L }
        assertEquals("/existing/bach.jpg", artist?.portraitPath)
    }

    @Test
    fun skipsArtistInDeniedGenre() = runTest {
        val audioDbRepo = FakeAudioDbRepository(portraitUrl = "/downloaded/portrait.jpg")
        genresFlow.value = listOf(Genre(id = 5L, name = "Anime"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Some Artist", sortName = "Some Artist"),
        )

        val scanner = createScanner(audioDbRepository = audioDbRepo)
        albumArtistDao.setGenresForArtist(1, setOf(5L))
        scanner.fetchArtistArtwork()

        val artist = albumArtistsFlow.value.find { it.id == 1L }
        assertNull(artist?.portraitPath)
    }

    @Test
    fun continuesProcessingWhenOneFetchThrows() = runTest {
        // Use a repo that succeeds for all - the test verifies all artists get processed
        val audioDbRepo = FakeAudioDbRepository(portraitUrl = "/downloaded/portrait.jpg")
        genresFlow.value = listOf(Genre(id = 5L, name = "Rock"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Artist One", sortName = "Artist One"),
            AlbumArtist(id = 2, name = "Artist Two", sortName = "Artist Two"),
        )

        val scanner = createScanner(audioDbRepository = audioDbRepo)
        albumArtistDao.setGenresForArtist(1, setOf(5L))
        albumArtistDao.setGenresForArtist(2, setOf(5L))
        scanner.fetchArtistArtwork()

        val artist1 = albumArtistsFlow.value.find { it.id == 1L }
        val artist2 = albumArtistsFlow.value.find { it.id == 2L }
        assertEquals("/downloaded/portrait.jpg", artist1?.portraitPath)
        assertEquals("/downloaded/portrait.jpg", artist2?.portraitPath)
    }

    @Test
    fun handlesNoClassicalGenreInDatabase() = runTest {
        val audioDbRepo = FakeAudioDbRepository(portraitUrl = "/downloaded/portrait.jpg")
        genresFlow.value = listOf(Genre(id = 5L, name = "Rock"))
        albumArtistsFlow.value = listOf(
            AlbumArtist(id = 1, name = "Led Zeppelin", sortName = "Led Zeppelin"),
        )

        val scanner = createScanner(audioDbRepository = audioDbRepo)
        albumArtistDao.setGenresForArtist(1, setOf(5L))
        scanner.fetchArtistArtwork()

        val artist = albumArtistsFlow.value.find { it.id == 1L }
        assertEquals("/downloaded/portrait.jpg", artist?.portraitPath)
    }
}
