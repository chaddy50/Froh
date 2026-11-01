package com.chaddy50.froh.data.repository

import com.chaddy50.froh.data.dao.AlbumDao
import com.chaddy50.froh.data.entity.Album

interface IAlbumRepository {
    suspend fun insert(album: Album)
}

class AlbumRepository(private val albumDao: AlbumDao) : IAlbumRepository {
    override suspend fun insert(album: Album) {
        albumDao.insert(album)
    }

    suspend fun update(album: Album) {
        albumDao.update(album)
    }

    suspend fun delete(album: Album) {
        albumDao.delete(album)
    }

    fun getAllAlbums() = albumDao.getAllAlbums()

    fun getAlbumsForArtist(
        albumArtistId: Long,
        shouldSortByCatalogueSortIndex: Boolean,
    ) = albumDao.getAlbumsForArtist(albumArtistId, shouldSortByCatalogueSortIndex)

    fun getAlbumsForArtistInGenre(
        albumArtistId: Long,
        genreId: Long,
        shouldSortByCatalogueSortIndex: Boolean,
    ) = albumDao.getAlbumsForArtistInGenre(albumArtistId, genreId, shouldSortByCatalogueSortIndex)

    fun getAlbumById(id: Long) = albumDao.getAlbumById(id)

    fun getNumberOfAlbumsForAlbumArtistInGenre(albumArtistId: Long, genreId: Long) = albumDao.getNumberOfAlbumsForAlbumArtistInGenre(albumArtistId, genreId)

    fun getNumberOfAlbumsForGenre(genreId: Long) = albumDao.getNumberOfAlbumsForGenre(genreId)
}
