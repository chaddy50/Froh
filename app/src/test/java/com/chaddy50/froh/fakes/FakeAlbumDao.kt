package com.chaddy50.froh.fakes

import com.chaddy50.froh.data.dao.AlbumDao
import com.chaddy50.froh.data.entity.Album
import com.chaddy50.froh.data.entity.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FakeAlbumDao(
    private val albums: MutableStateFlow<List<Album>> = MutableStateFlow(emptyList()),
    private val tracks: MutableStateFlow<List<Track>> = MutableStateFlow(emptyList()),
) : AlbumDao {
    override fun getAlbumById(id: Long): Flow<Album?> =
        albums.map { list -> list.find { it.id == id } }

    override fun getAllAlbums(): Flow<List<Album>> = albums

    override fun getAlbumsForArtist(artistId: Long, shouldSortByCatalogueSortIndex: Boolean): Flow<List<Album>> =
        albums.map { list -> list.filter { it.artistId == artistId } }

    override fun getAlbumsForArtistInGenre(
        albumArtistId: Long,
        genreId: Long,
        shouldSortByCatalogueSortIndex: Boolean,
    ): Flow<List<Album>> =
        albums.map { list -> list.filter { it.artistId == albumArtistId } }

    override fun getNumberOfAlbumsForAlbumArtistInGenre(albumArtistId: Long, genreId: Long): Flow<Int> =
        combine(albums, tracks) { albumList, trackList ->
            val albumIdsInGenre = trackList
                .filter { it.genreId == genreId || it.parentGenreId == genreId }
                .map { it.albumId }
                .toSet()
            albumList.count { it.artistId == albumArtistId && it.id in albumIdsInGenre }
        }

    override fun getNumberOfAlbumsForGenre(genreId: Long): Flow<Int> =
        combine(albums, tracks) { albumList, trackList ->
            val albumIdsInGenre = trackList
                .filter { it.genreId == genreId || it.parentGenreId == genreId }
                .map { it.albumId }
                .toSet()
            albumList.count { it.id in albumIdsInGenre }
        }

    override suspend fun insert(album: Album) = Unit
    override suspend fun update(album: Album) = Unit
    override suspend fun delete(album: Album) = Unit
}
