
package com.chaddy50.froh.data.scanner.processor

import com.chaddy50.froh.data.repository.GenreMappingRepository
import com.chaddy50.froh.data.repository.GenreRepository
import com.chaddy50.froh.fakes.FakeGenreDao
import com.chaddy50.froh.fakes.FakeGenreMappingDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private fun createProcessor(
    genreDao: FakeGenreDao = FakeGenreDao(),
    genreMappingDao: FakeGenreMappingDao = FakeGenreMappingDao(),
): GenreProcessor = GenreProcessor(GenreRepository(genreDao), GenreMappingRepository(genreMappingDao))

private fun createSeededGenreMappingDao(): FakeGenreMappingDao {
    val dao = FakeGenreMappingDao()
    val defaults = listOf(
        "Solo Piano", "Symphony", "String Quartet", "Piano Concerto",
        "Ballet", "Cello Concerto", "Horn with Orchestra", "Orchestra and Piano",
        "Orchestral", "Piano Quartet", "Piano Trio", "Piano with Orchestra",
        "Violin Concerto", "Violin Sonata", "Organ and Orchestra",
        "Piano and Orchestra", "Violin and Harp", "Cello Sonata",
        "Clarinet Quintet", "Clarinet Sonata", "Clarinet Trio",
        "Concerto for Violin, Cello, and Orchestra", "Horn Trio",
        "Piano Quintet", "Piano for Four Hands", "String Quintet",
        "String Sextet", "Viola Sonata",
    ).map { com.chaddy50.froh.data.entity.GenreMapping(subGenreName = it, parentGenreName = "Classical") }
    dao.mappings.addAll(defaults)
    return dao
}

class GenreProcessorProcessTest {

    @Test
    fun returnsGenreIdAndName() = runTest {
        val processor = createProcessor()
        val result = processor.process("Rock")
        assertEquals("Rock", result.genreName)
        assertEquals(1L, result.genreId)
    }

    @Test
    fun unknownGenreNameIsPassedThrough() = runTest {
        val processor = createProcessor()
        val result = processor.process("Unknown Genre")
        assertEquals("Unknown Genre", result.genreName)
    }

    @Test
    fun nonClassicalGenreHasNullParentAndIsNotClassical() = runTest {
        val processor = createProcessor()
        val result = processor.process("Rock")
        assertNull(result.parentGenreId)
        assertFalse(result.isClassical)
    }

    @Test
    fun classicalSubGenreHasParentIdAndIsClassical() = runTest {
        val processor = createProcessor(genreMappingDao = createSeededGenreMappingDao())
        processor.setUpClassicalMappings()
        val result = processor.process("Symphony")
        assertTrue(result.isClassical)
        assertNotNull(result.parentGenreId)
    }

    @Test
    fun classicalParentGenreIsNotMarkedClassical() = runTest {
        val processor = createProcessor(genreMappingDao = createSeededGenreMappingDao())
        processor.setUpClassicalMappings()
        val result = processor.process("Classical")
        assertFalse(result.isClassical)
    }

    @Test
    fun secondCallWithSameGenreReturnsCachedId() = runTest {
        val dao = FakeGenreDao()
        val processor = createProcessor(genreDao = dao)
        val first = processor.process("Jazz")
        val second = processor.process("Jazz")
        assertEquals(first.genreId, second.genreId)
        assertEquals(1, dao.insertCount)
    }

    @Test
    fun differentGenresGetDifferentIds() = runTest {
        val dao = FakeGenreDao()
        val processor = createProcessor(genreDao = dao)
        val rock = processor.process("Rock")
        val jazz = processor.process("Jazz")
        assertNotEquals(rock.genreId, jazz.genreId)
        assertEquals(2, dao.insertCount)
    }
}

class GenreProcessorSetUpClassicalMappingsTest {

    @Test
    fun insertsClassicalGenreIntoRepository() = runTest {
        val genreDao = FakeGenreDao()
        val processor = createProcessor(genreDao = genreDao, genreMappingDao = createSeededGenreMappingDao())
        processor.setUpClassicalMappings()
        assertNotNull(genreDao.genres["Classical"])
    }

    @Test
    fun classicalSubGenresMapToClassicalParentAfterSetup() = runTest {
        val processor = createProcessor(genreMappingDao = createSeededGenreMappingDao())
        processor.setUpClassicalMappings()

        val subGenres = listOf(
            "Solo Piano", "Symphony", "String Quartet",
            "Piano Concerto", "Ballet", "Violin Concerto",
        )
        for (name in subGenres) {
            val result = processor.process(name)
            assertTrue("$name should be classical", result.isClassical)
            assertNotNull("$name should have parentGenreId", result.parentGenreId)
        }
    }

    @Test
    fun callingSetupTwiceDoesNotDuplicateClassicalGenre() = runTest {
        val genreDao = FakeGenreDao()
        val processor = createProcessor(genreDao = genreDao, genreMappingDao = createSeededGenreMappingDao())
        processor.setUpClassicalMappings()
        processor.setUpClassicalMappings()
        assertEquals(1, genreDao.insertCount)
    }

    @Test
    fun loadsMappingsFromRepository() = runTest {
        val mappingDao = FakeGenreMappingDao()
        mappingDao.mappings.add(
            com.chaddy50.froh.data.entity.GenreMapping(subGenreName = "TestGenre", parentGenreName = "Classical")
        )
        val processor = createProcessor(genreMappingDao = mappingDao)
        processor.setUpClassicalMappings()
        val result = processor.process("TestGenre")
        assertTrue(result.isClassical)
        assertNotNull(result.parentGenreId)
    }

    @Test
    fun emptyMappingsResultsInNoClassicalGenres() = runTest {
        val processor = createProcessor(genreMappingDao = FakeGenreMappingDao())
        processor.setUpClassicalMappings()
        val result = processor.process("Symphony")
        assertFalse(result.isClassical)
        assertNull(result.parentGenreId)
    }
}

class ShouldFetchArtistArtworkForGenreTest {

    private val denyList = listOf("Anime", "Movie", "Video Game")

    @Test
    fun nullReturnsFalse() {
        assertFalse(shouldFetchArtistArtworkForGenre(null, denyList))
    }

    @Test
    fun genreInDenyListReturnsFalse() {
        for (genre in denyList) {
            assertFalse(shouldFetchArtistArtworkForGenre(genre, denyList))
        }
    }

    @Test
    fun genreNotInDenyListReturnsTrue() {
        assertTrue(shouldFetchArtistArtworkForGenre("Classical", denyList))
        assertTrue(shouldFetchArtistArtworkForGenre("Rock", denyList))
    }

    @Test
    fun emptyStringReturnsTrue() {
        assertTrue(shouldFetchArtistArtworkForGenre("", denyList))
    }

    @Test
    fun matchIsCaseSensitive() {
        assertTrue(shouldFetchArtistArtworkForGenre("anime", denyList))
        assertTrue(shouldFetchArtistArtworkForGenre("MOVIE", denyList))
    }

    @Test
    fun emptyDenyListAlwaysReturnsTrue() {
        assertTrue(shouldFetchArtistArtworkForGenre("Anime", emptyList()))
    }
}
