package com.chaddy50.froh.fakes

import com.chaddy50.froh.data.dao.GenreDao
import com.chaddy50.froh.data.entity.Genre
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeGenreDao(
    private val topLevelGenresFlow: MutableStateFlow<List<Genre>> = MutableStateFlow(emptyList()),
    val allGenres: MutableStateFlow<List<Genre>> = MutableStateFlow(emptyList()),
) : GenreDao {
    val genres = mutableMapOf<String, Genre>()
    var nextInsertId = 1L
    var insertCount = 0

    override suspend fun insert(genre: Genre): Long {
        insertCount++
        if (genres.containsKey(genre.name)) return -1L
        val id = nextInsertId++
        genres[genre.name] = genre.copy(id = id)
        return id
    }

    override suspend fun getGenreByName(name: String): Genre? = genres[name]

    override fun getAllTopLevelGenres(): Flow<List<Genre>> = topLevelGenresFlow

    override fun getGenreById(id: Long): Flow<Genre?> =
        allGenres.map { list -> list.find { it.id == id } }

    override suspend fun getGenreName(genreId: Long): String? =
        allGenres.value.find { it.id == genreId }?.name

    override fun getSubGenres(parentGenreId: Long): Flow<List<Genre>> =
        allGenres.map { list -> list.filter { it.parentGenreId == parentGenreId } }

    override fun getSubGenresForAlbumArtist(parentGenreId: Long, albumArtistId: Long): Flow<List<Genre>> =
        allGenres.map { list -> list.filter { it.parentGenreId == parentGenreId } }

    override suspend fun getParentGenreId(genreId: Long): Long? =
        allGenres.value.find { it.id == genreId }?.parentGenreId

    override suspend fun update(genre: Genre) {
        genres[genre.name] = genre
    }
    override suspend fun delete(genre: Genre) = TODO()
    override fun getAllGenres(): Flow<List<Genre>> = allGenres
}
