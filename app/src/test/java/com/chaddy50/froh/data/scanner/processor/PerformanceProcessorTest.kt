package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.entity.Performance
import com.chaddy50.froh.data.repository.IPerformanceRepository
import com.chaddy50.froh.data.scanner.util.UNKNOWN_ALBUM
import com.chaddy50.froh.data.scanner.util.UNKNOWN_ARTIST
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PerformanceProcessorTest {

    private suspend fun process(
        processor: PerformanceProcessor,
        isClassical: Boolean = true,
        trackId: Long = 1L,
        genreId: Long = 5L,
        albumId: Long = 100L,
        artistId: Long = 10L,
        albumName: String = "Symphony No. 5",
        artistName: String = "Beethoven",
        year: String = "1808",
    ) = processor.process(
        isClassical, trackId, genreId, albumId, artistId, albumName, artistName, year,
    )

    @Test
    fun nonClassicalReturnsNull() = runTest {
        val processor = PerformanceProcessor(FakePerformanceRepository(), FakeArtworkSaver())

        val result = process(processor, isClassical = false)

        assertNull(result)
    }

    @Test
    fun classicalInsertsAndReturnsTriple() = runTest {
        val repo = FakePerformanceRepository(nextInsertId = 42L)
        val artworkSaver = FakeArtworkSaver(artworkPath = "/art/42.jpg")
        val processor = PerformanceProcessor(repo, artworkSaver)

        val result = process(processor)

        assertEquals(42L, result?.first)
        assertEquals("/art/42.jpg", result?.second)
        assertEquals("1808", result?.third)
        assertEquals(1, repo.insertCount)
    }

    @Test
    fun secondCallReturnsCachedResult() = runTest {
        val repo = FakePerformanceRepository(nextInsertId = 42L)
        val processor = PerformanceProcessor(repo, FakeArtworkSaver())

        process(processor, trackId = 1L)
        val result = process(processor, trackId = 2L)

        assertEquals(42L, result?.first)
        assertEquals(1, repo.insertCount) // no second insert
    }

    @Test
    fun insertConflictFallsBackToQuery() = runTest {
        val repo = FakePerformanceRepository(nextInsertId = -1L, findResult = 99L)
        val artworkSaver = FakeArtworkSaver(artworkPath = "/art/99.jpg")
        val processor = PerformanceProcessor(repo, artworkSaver)

        val result = process(processor)

        assertEquals(99L, result?.first)
        assertEquals("/art/99.jpg", result?.second)
    }

    @Test
    fun insertConflictWithNoFallbackReturnsNull() = runTest {
        val repo = FakePerformanceRepository(nextInsertId = -1L, findResult = null)
        val processor = PerformanceProcessor(repo, FakeArtworkSaver())

        val result = process(processor)

        assertNull(result)
    }

    @Test
    fun resolvedValuesAreUsedInsteadOfCursorFields() = runTest {
        val repo = FakePerformanceRepository(nextInsertId = 1L)
        val processor = PerformanceProcessor(repo, FakeArtworkSaver())

        // Album name, artist name and year now come from Media3, not the MediaStore row
        process(
            processor,
            albumName = "Piano Concerto No. 2",
            artistName = "Rachmaninoff",
            year = "1901",
        )

        val performance = repo.lastInsertedPerformance!!
        assertEquals("Piano Concerto No. 2", performance.albumName)
        assertEquals("Rachmaninoff", performance.artistName)
        assertEquals("1901", performance.year)
    }

    @Test
    fun unknownAlbumNameIsPassedThrough() = runTest {
        val repo = FakePerformanceRepository(nextInsertId = 1L)
        val processor = PerformanceProcessor(repo, FakeArtworkSaver())

        // MetadataResolver substitutes UNKNOWN_ALBUM before the processor sees the name
        process(processor, albumName = UNKNOWN_ALBUM)

        assertEquals("Unknown Album", repo.lastInsertedPerformance?.albumName)
    }

    @Test
    fun unknownArtistNameIsPassedThrough() = runTest {
        val repo = FakePerformanceRepository(nextInsertId = 1L)
        val processor = PerformanceProcessor(repo, FakeArtworkSaver())

        // MetadataResolver substitutes UNKNOWN_ARTIST before the processor sees the name
        process(processor, artistName = UNKNOWN_ARTIST)

        assertEquals("Unknown Artist", repo.lastInsertedPerformance?.artistName)
    }
}

private class FakePerformanceRepository(
    private val nextInsertId: Long = 1L,
    private val findResult: Long? = null,
) : IPerformanceRepository {
    var insertCount = 0
    var lastInsertedPerformance: Performance? = null

    override suspend fun insert(performance: Performance): Long {
        insertCount++
        lastInsertedPerformance = performance
        return nextInsertId
    }

    override suspend fun findByAlbumAndArtist(albumId: Long, artistId: Long): Long? = findResult
}
