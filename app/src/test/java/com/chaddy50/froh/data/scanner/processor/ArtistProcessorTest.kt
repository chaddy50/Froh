package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.entity.Artist
import com.chaddy50.froh.data.repository.IArtistRepository
import com.chaddy50.froh.data.scanner.util.UNKNOWN_ARTIST
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

private const val ARTIST_BEETHOVEN = "Beethoven"

class ArtistProcessorTest {

    @Test
    fun returnsArtistIdAndNameFromRepository() = runTest {
        val repo = FakeArtistRepository()
        val processor = ArtistProcessor(repo)

        val result = processor.process(ARTIST_BEETHOVEN)

        assertEquals(1L, result.first)
        assertEquals(ARTIST_BEETHOVEN, result.second)
        assertEquals(1, repo.insertCount)
    }

    @Test
    fun sameArtistNameReturnsCachedIdAndInsertsOnce() = runTest {
        val repo = FakeArtistRepository()
        val processor = ArtistProcessor(repo)

        val first = processor.process(ARTIST_BEETHOVEN)
        val second = processor.process(ARTIST_BEETHOVEN)

        assertEquals(first.first, second.first)
        assertEquals(ARTIST_BEETHOVEN, second.second)
        assertEquals(1, repo.insertCount)
    }

    @Test
    fun differentArtistNamesGetDifferentIds() = runTest {
        val repo = FakeArtistRepository()
        val processor = ArtistProcessor(repo)

        val beethoven = processor.process(ARTIST_BEETHOVEN)
        val mozart = processor.process("Mozart")

        assertNotEquals(beethoven.first, mozart.first)
        assertEquals(2, repo.insertCount)
    }

    @Test
    fun nullArtistNameFallsBackToUnknownArtist() = runTest {
        val repo = FakeArtistRepository()
        val processor = ArtistProcessor(repo)

        // MetadataResolver substitutes UNKNOWN_ARTIST before the processor sees the name
        val result = processor.process(UNKNOWN_ARTIST)

        assertEquals("Unknown Artist", result.second)
    }

    @Test
    fun tracksWithSameNameButDifferentMediaStoreIdsCollapseToOneArtist() = runTest {
        val repo = FakeArtistRepository()
        val processor = ArtistProcessor(repo)

        // Two tracks MediaStore reported under different ARTIST_IDs (one of them <unknown>)
        // resolve to the same Media3 name, so they must share a single artist row
        val fromWorkingRow = processor.process("Darren Korb")
        val fromDegenerateRow = processor.process("Darren Korb")

        assertEquals(fromWorkingRow.first, fromDegenerateRow.first)
        assertEquals(1, repo.insertCount)
        assertEquals(1, repo.insertedNames.size)
    }
}

private class FakeArtistRepository : IArtistRepository {
    var insertCount = 0
    val insertedNames = mutableSetOf<String>()
    private val artistIds = mutableMapOf<String, Long>()
    private var nextId = 1L

    override suspend fun insert(artist: Artist) {
        insertCount++
    }

    override suspend fun findOrInsertArtist(artistName: String): Long {
        artistIds[artistName]?.let { return it }

        insertCount++
        insertedNames.add(artistName)
        val artistId = nextId++
        artistIds[artistName] = artistId
        return artistId
    }
}
