package com.chaddy50.froh.data.repository

import com.chaddy50.froh.data.dao.GenreDao
import com.chaddy50.froh.data.entity.Genre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GenreRepository(private val genreDao: GenreDao) {

    suspend fun insert(genre: Genre) {
        genreDao.insert(genre)
    }

    suspend fun update(genre: Genre) {
        genreDao.update(genre)
    }

    suspend fun delete(genre: Genre) {
        genreDao.delete(genre)
    }

    fun getAllGenres() = genreDao.getAllGenres()

    suspend fun getGenreName(genreId: Long) = genreDao.getGenreName(genreId)

    fun getGenreById(id: Long) = genreDao.getGenreById(id)

    suspend fun getGenreByName(name: String) = genreDao.getGenreByName(name)

    suspend fun getParentGenreId(genreId: Long) = genreDao.getParentGenreId(genreId)

    fun getAllTopLevelGenres() = genreDao.getAllTopLevelGenres()

    suspend fun findOrInsertGenreByName(name: String, parentGenreId: Long? = null): Long {
        val existingGenre = genreDao.getGenreByName(name)
        if (existingGenre != null) {
            if (existingGenre.parentGenreId != parentGenreId) {
                genreDao.update(existingGenre.copy(parentGenreId = parentGenreId))
            }
            return existingGenre.id
        }

        val newGenre = Genre(name = name, parentGenreId = parentGenreId)
        val newGenreId = genreDao.insert(newGenre)
        return if (newGenreId != -1L) {
            newGenreId
        } else {
            genreDao.getGenreByName(name)!!.id
        }
    }

    fun getSubGenresForAlbumArtist(parentGenreId: Long, albumArtistId: Long): Flow<List<Genre>> {
        return flow {
            emitAll(genreDao.getSubGenresForAlbumArtist(parentGenreId, albumArtistId))
        }.flowOn(Dispatchers.IO)
    }

    fun getSubGenres(parentGenreId: Long): Flow<List<Genre>> {
        return flow {
            emitAll(genreDao.getSubGenres(parentGenreId))
        }.flowOn(Dispatchers.IO)
    }
}
