package com.chaddy50.froh.data.repository

import com.chaddy50.froh.fakes.FakeArtistDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistRepositoryFindOrInsertArtistTest {

    @Test
    fun insertsAndReturnsIdForNewArtist() = runTest {
        val dao = FakeArtistDao()
        val repository = ArtistRepository(dao)

        val artistId = repository.findOrInsertArtist("Darren Korb")

        assertTrue("expected a real id, got $artistId", artistId > 0L)
        assertEquals(1, dao.insertCount)
        assertEquals("Darren Korb", dao.artists["Darren Korb"]?.name)
    }

    @Test
    fun returnsExistingIdForKnownArtist() = runTest {
        val dao = FakeArtistDao()
        val repository = ArtistRepository(dao)

        val firstId = repository.findOrInsertArtist("Darren Korb")
        val secondId = repository.findOrInsertArtist("Darren Korb")

        assertEquals(firstId, secondId)
    }

    @Test
    fun doesNotInsertTwiceForSameName() = runTest {
        val dao = FakeArtistDao()
        val repository = ArtistRepository(dao)

        repository.findOrInsertArtist("Darren Korb")
        repository.findOrInsertArtist("Darren Korb")

        assertEquals(1, dao.insertCount)
    }

    @Test
    fun differentNamesGetDistinctIds() = runTest {
        val dao = FakeArtistDao()
        val repository = ArtistRepository(dao)

        val korbId = repository.findOrInsertArtist("Darren Korb")
        val disasterpeaceId = repository.findOrInsertArtist("Disasterpeace")

        assertNotEquals(korbId, disasterpeaceId)
        assertEquals(2, dao.insertCount)
    }
}
