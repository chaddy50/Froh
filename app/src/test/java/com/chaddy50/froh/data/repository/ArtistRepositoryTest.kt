package com.chaddy50.froh.data.repository

import com.chaddy50.froh.fakes.FakeArtistDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val ARTIST_DARREN_KORB = "Darren Korb"

class ArtistRepositoryFindOrInsertArtistTest {

    @Test
    fun insertsAndReturnsIdForNewArtist() = runTest {
        val dao = FakeArtistDao()
        val repository = ArtistRepository(dao)

        val artistId = repository.findOrInsertArtist(ARTIST_DARREN_KORB)

        assertTrue("expected a real id, got $artistId", artistId > 0L)
        assertEquals(1, dao.insertCount)
        assertEquals(ARTIST_DARREN_KORB, dao.artists[ARTIST_DARREN_KORB]?.name)
    }

    @Test
    fun returnsExistingIdForKnownArtist() = runTest {
        val dao = FakeArtistDao()
        val repository = ArtistRepository(dao)

        val firstId = repository.findOrInsertArtist(ARTIST_DARREN_KORB)
        val secondId = repository.findOrInsertArtist(ARTIST_DARREN_KORB)

        assertEquals(firstId, secondId)
    }

    @Test
    fun doesNotInsertTwiceForSameName() = runTest {
        val dao = FakeArtistDao()
        val repository = ArtistRepository(dao)

        repository.findOrInsertArtist(ARTIST_DARREN_KORB)
        repository.findOrInsertArtist(ARTIST_DARREN_KORB)

        assertEquals(1, dao.insertCount)
    }

    @Test
    fun differentNamesGetDistinctIds() = runTest {
        val dao = FakeArtistDao()
        val repository = ArtistRepository(dao)

        val korbId = repository.findOrInsertArtist(ARTIST_DARREN_KORB)
        val disasterpeaceId = repository.findOrInsertArtist("Disasterpeace")

        assertNotEquals(korbId, disasterpeaceId)
        assertEquals(2, dao.insertCount)
    }
}
