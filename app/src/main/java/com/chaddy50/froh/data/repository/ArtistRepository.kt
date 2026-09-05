package com.chaddy50.froh.data.repository

import com.chaddy50.froh.data.dao.ArtistDao
import com.chaddy50.froh.data.entity.Artist
import kotlinx.coroutines.flow.Flow

interface IArtistRepository {
    suspend fun insert(artist: Artist)
    suspend fun findOrInsertArtist(artistName: String): Long
}

class ArtistRepository(private val artistDao: ArtistDao) : IArtistRepository {

    fun getAllArtists(): Flow<List<Artist>> = artistDao.getAllArtists()

    fun getArtistById(id: Int): Flow<Artist?> = artistDao.getArtistById(id)

    override suspend fun insert(artist: Artist) {
        artistDao.insert(artist)
    }

    override suspend fun findOrInsertArtist(artistName: String): Long {
        val existingArtist = artistDao.getArtistByName(artistName)
        if (existingArtist != null) return existingArtist.id

        artistDao.insert(Artist(name = artistName))
        return artistDao.getArtistByName(artistName)?.id ?: -1
    }

    suspend fun update(artist: Artist) {
        artistDao.update(artist)
    }

    suspend fun delete(artist: Artist) {
        artistDao.delete(artist)
    }
}
