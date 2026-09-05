package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.repository.IAlbumArtistRepository
import com.chaddy50.froh.data.scanner.util.UNKNOWN_ARTIST
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumArtistProcessorTest {

    @Test
    fun returnsAlbumArtistIdAndName() = runTest {
        val repo = FakeAlbumArtistRepository(nextId = 42L)
        val processor = AlbumArtistProcessor(repo)

        val result = processor.process("Beethoven")

        assertEquals(42L, result.first)
        assertEquals("Beethoven", result.second)
        assertEquals(1, repo.insertCount)
    }

    @Test
    fun secondCallWithSameNameReturnsCached() = runTest {
        val repo = FakeAlbumArtistRepository(nextId = 42L)
        val processor = AlbumArtistProcessor(repo)

        processor.process("Beethoven")
        val result = processor.process("Beethoven")

        assertEquals(42L, result.first)
        assertEquals(1, repo.insertCount)
    }

    @Test
    fun differentNamesAreNotCached() = runTest {
        val repo = FakeAlbumArtistRepository(nextId = 42L)
        val processor = AlbumArtistProcessor(repo)

        processor.process("Beethoven")
        processor.process("Mozart")

        assertEquals(2, repo.insertCount)
    }

    @Test
    fun resolvedValuesAreUsedInsteadOfCursorFields() = runTest {
        val repo = FakeAlbumArtistRepository(nextId = 7L)
        val processor = AlbumArtistProcessor(repo)

        // The album artist name now comes from Media3, where MediaStore had nothing at all
        val result = processor.process("Disasterpeace")

        assertEquals(7L, result.first)
        assertEquals("Disasterpeace", result.second)
        assertEquals("Disasterpeace", repo.lastRequestedName)
    }

    @Test
    fun nullAlbumArtistNameFallsBackToUnknown() = runTest {
        val repo = FakeAlbumArtistRepository(nextId = 1L)
        val processor = AlbumArtistProcessor(repo)

        // MetadataResolver substitutes UNKNOWN_ARTIST before the processor sees the name
        val result = processor.process(UNKNOWN_ARTIST)

        assertEquals("Unknown Artist", result.second)
    }
}

private class FakeAlbumArtistRepository(
    private val nextId: Long = 1L,
) : IAlbumArtistRepository {
    var insertCount = 0
    var lastRequestedName: String? = null

    override suspend fun findOrInsertAlbumArtist(albumArtistName: String): Long {
        insertCount++
        lastRequestedName = albumArtistName
        return nextId
    }
}
