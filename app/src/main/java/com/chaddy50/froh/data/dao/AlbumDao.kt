package com.chaddy50.froh.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chaddy50.froh.data.entity.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(album: Album)

    @Update
    suspend fun update(album: Album)

    @Delete
    suspend fun delete(album: Album)

    @Query("SELECT * FROM albums WHERE id = :id")
    fun getAlbumById(id: Long): Flow<Album?>

    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<Album>>

    @Query("""
        SELECT * FROM albums 
        WHERE artistId = :artistId 
        ORDER BY
            CASE WHEN :shouldSortByCatalogueSortIndex = 1 THEN albums.catalogueSortIndex END ASC,
            CASE WHEN :shouldSortByCatalogueSortIndex = 0 THEN albums.year END DESC
    """)
    fun getAlbumsForArtist(artistId: Long, shouldSortByCatalogueSortIndex: Boolean): Flow<List<Album>>

    @Query("""
        SELECT DISTINCT albums.* FROM albums 
        INNER JOIN tracks ON tracks.albumId = albums.id
        WHERE albums.artistId = :albumArtistId
        AND (tracks.genreId = :genreId OR tracks.parentGenreId = :genreId)
        ORDER BY 
            CASE WHEN :shouldSortByCatalogueSortIndex = 1 THEN albums.catalogueSortIndex END ASC,
            CASE WHEN :shouldSortByCatalogueSortIndex = 0 THEN albums.year END DESC
    """)
    fun getAlbumsForArtistInGenre(
        albumArtistId: Long,
        genreId: Long,
        shouldSortByCatalogueSortIndex: Boolean,
    ): Flow<List<Album>>

    @Query("""
        SELECT COUNT(DISTINCT(albums.id)) FROM albums
        INNER JOIN tracks ON tracks.albumId = albums.id
        WHERE albums.artistId = :albumArtistId
        AND (tracks.genreId = :genreId OR tracks.parentGenreId = :genreId)
    """)
    fun getNumberOfAlbumsForAlbumArtistInGenre(albumArtistId: Long, genreId: Long): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT albums.id) FROM albums
        INNER JOIN tracks ON tracks.albumId = albums.id
        WHERE tracks.genreId = :genreId OR tracks.parentGenreId = :genreId
    """)
    fun getNumberOfAlbumsForGenre(genreId: Long): Flow<Int>
}
