package com.chaddy50.froh.data.repository

import com.chaddy50.froh.data.dao.GenreDao
import com.chaddy50.froh.data.entity.Genre
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FindOrInsertGenreByNameTest {

    @Test
    fun returnsExistingGenreId() = runTest {
        val dao = FakeGenreDao()
        dao.genres["Classical"] = Genre(id = 42, name = "Classical")
        val repository = GenreRepository(dao)

        val result = repository.findOrInsertGenreByName("Classical")

        assertEquals(42L, result)
        assertEquals(0, dao.insertCount)
    }

    @Test
    fun insertsNewGenreAndReturnsId() = runTest {
        val dao = FakeGenreDao()
        dao.nextInsertId = 7L
        val repository = GenreRepository(dao)

        val result = repository.findOrInsertGenreByName("Rock")

        assertEquals(7L, result)
        assertEquals(1, dao.insertCount)
    }

    @Test
    fun passesParentGenreIdToInsert() = runTest {
        val dao = FakeGenreDao()
        dao.nextInsertId = 10L
        val repository = GenreRepository(dao)

        repository.findOrInsertGenreByName("Symphony", parentGenreId = 42L)

        assertEquals(42L, dao.lastInsertedGenre?.parentGenreId)
    }

    @Test
    fun concurrentInsertConflictFallsBackToQuery() = runTest {
        val dao = FakeGenreDao()
        dao.nextInsertId = -1L // simulate conflict
        dao.genreAvailableAfterConflict = Genre(id = 5, name = "Jazz")
        val repository = GenreRepository(dao)

        val result = repository.findOrInsertGenreByName("Jazz")

        assertEquals(5L, result)
        assertEquals(1, dao.insertCount)
    }
}

private class FakeGenreDao : GenreDao {
    val genres = mutableMapOf<String, Genre>()
    var nextInsertId = 1L
    var insertCount = 0
    var lastInsertedGenre: Genre? = null
    var genreAvailableAfterConflict: Genre? = null

    override suspend fun insert(genre: Genre): Long {
        insertCount++
        lastInsertedGenre = genre
        val id = nextInsertId
        if (id != -1L) {
            genres[genre.name] = genre.copy(id = id)
        } else if (genreAvailableAfterConflict != null) {
            // Simulate another thread having inserted it
            genres[genreAvailableAfterConflict!!.name] = genreAvailableAfterConflict!!
        }
        return id
    }

    override suspend fun getGenreByName(name: String): Genre? = genres[name]

    // Unused methods — required by interface
    override suspend fun update(genre: Genre) = TODO()
    override suspend fun delete(genre: Genre) = TODO()
    override fun getGenreById(id: Long): Flow<Genre?> = TODO()
    override fun getAllGenres(): Flow<List<Genre>> = TODO()
    override suspend fun getGenreName(genreId: Long): String? = TODO()
    override fun getAllTopLevelGenres(): Flow<List<Genre>> = TODO()
    override fun getSubGenres(parentGenreId: Long): Flow<List<Genre>> = TODO()
    override fun getSubGenresForAlbumArtist(parentGenreId: Long, albumArtistId: Long): Flow<List<Genre>> = TODO()
    override suspend fun getParentGenreId(genreId: Long): Long? = TODO()
}